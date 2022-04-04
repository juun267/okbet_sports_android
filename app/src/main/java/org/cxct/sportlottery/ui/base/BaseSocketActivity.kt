package org.cxct.sportlottery.ui.base

import android.app.ActivityManager
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.service.BackService
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.util.GameConfigManager
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.reflect.KClass

abstract class BaseSocketActivity<T : BaseSocketViewModel>(clazz: KClass<T>) :
    BaseFavoriteActivity<T>(clazz) {

    private val sharedPref: SharedPreferences by lazy {
        this.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    val receiver: ServiceBroadcastReceiver by inject()

    private var backService: BackService? = null
    private var isServiceBound = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Timber.e(">>> onServiceConnected")
            val binder = service as BackService.MyBinder //透過Binder調用Service內的方法
            backService = binder.service

            binder.connect(
                viewModel.loginRepository.token,
                viewModel.loginRepository.userId,
                viewModel.loginRepository.platformId
            )

            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Timber.e(">>> onServiceDisconnected")
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        receiver.sysMaintenance.observe(this, Observer {
            if ((it?.status ?: 0) == MaintenanceActivity.MaintainType.FIXING.value) {
                startActivity(Intent(this, MaintenanceActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
            }
        })

        receiver.serviceConnectStatus.observe(this, Observer { status ->
            when (status) {
                ServiceConnectStatus.RECONNECT_FREQUENCY_LIMIT -> {
                    hideLoading()
                    showErrorPromptDialog(getString(R.string.message_socket_connect)) { backService?.doReconnect() }
                }
                ServiceConnectStatus.CONNECTING -> {
                    loading()
                }
                ServiceConnectStatus.CONNECTED -> {
                    hideLoading()
                }
                else -> {
                    hideLoading()
                    //do nothing
                }
            }
        })

        receiver.playQuotaChange.observe(this) {
            it?.playQuotaComData?.let { playQuotaComData ->
                viewModel.updatePlayQuota(playQuotaComData)
            }
        }

        receiver.userMoney.observe(this) {
            viewModel.updateMoney(it)
        }

        receiver.lockMoney.observe(this) {
            viewModel.updateLockMoney(it)
        }

        receiver.orderSettlement.observe(this) {
            viewModel.getSettlementNotification(it)
        }

        receiver.userNotice.observe(this, Observer {
            it?.userNoticeList?.let { list ->
                viewModel.setUserNoticeList(list)
            }
        })

        receiver.userDiscountChange.observe(this) {
            viewModel.updateDiscount(it?.discount)
        }

        receiver.dataSourceChange.observe(this) {
            this.run {
                fun reStart() = if (sConfigData?.thirdOpen == FLAG_OPEN)
                    MainActivity.reStart(this)
                else
                    GameActivity.reStart(this)
                showErrorPromptDialog(
                    title = getString(R.string.prompt),
                    message = getString(R.string.message_source_change)
                ) { reStart() }
            }
        }

        receiver.userInfoChange.observe(this) {
            if (viewModel.isLogin.value == true) {
                viewModel.updateDiscount(null)
            }
        }

        receiver.userMaxBetMoneyChange.observe(this) {
            if (viewModel.isLogin.value == true && sharedPref.getInt(
                    KEY_USER_LEVEL_ID,
                    -1
                ) == it?.userLevelConfigList?.firstOrNull()?.id
            ) {
                GameConfigManager.maxBetMoney =
                    it.userLevelConfigList.firstOrNull()?.maxBetMoney ?: 9999999
                GameConfigManager.maxParlayBetMoney =
                    it.userLevelConfigList.firstOrNull()?.maxParlayBetMoney ?: 99999
                GameConfigManager.maxCpBetMoney =
                    it.userLevelConfigList.firstOrNull()?.maxCpBetMoney ?: 99999
            }
        }
    }

    fun subscribeSportChannelHall(gameTypeCode: String ?= null){
        backService?.subscribeSportChannelHall(gameTypeCode)
    }

    fun subscribeChannelHall(
        gameType: String?,
        eventId: String?
    ) {
        backService?.subscribeHallChannel(gameType, eventId)
    }

    fun subscribeChannelEvent(
        eventId: String?
    ) {
        backService?.subscribeEventChannel(eventId)
    }

    fun unSubscribeChannelHall(
        gameType: String?,
        cateMenuCode: String?,
        eventId: String?
    ) {
        backService?.unsubscribeHallChannel(gameType, cateMenuCode, eventId)
    }

    fun unSubscribeChannelHall(
        eventId: String?
    ) {
        backService?.unsubscribeHallChannel(eventId)
    }

    fun unSubscribeChannelEvent(eventId: String?) {
        backService?.unsubscribeEventChannel(eventId)
    }

    fun unsubscribeHallChannel(eventId: String?) {
        backService?.unsubscribeHallChannel(eventId)
    }

    fun unSubscribeChannelHallAll() {
        backService?.unsubscribeAllHallChannel()
    }

    fun unSubscribeChannelHallSport(){
        backService?.unsubscribeSportHallChannel()
    }

    fun unSubscribeChannelEventAll() {
        backService?.unsubscribeAllEventChannel()
    }

    fun betListPageSubscribeEvent() {
        backService?.betListPageSubscribeEvent()
    }

    fun betListPageUnSubScribeEvent() {
        backService?.betListPageUnSubScribeEvent()
    }

    override fun onStart() {
        super.onStart()

        subscribeBroadCastReceiver()
        bindService()
    }

    override fun onStop() {
        super.onStop()

        removeBroadCastReceiver()
        unBindService()
    }

    private fun bindService() {
        if (isServiceBound) return

        val serviceIntent = Intent(this, BackService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        isServiceBound = true
    }

    private fun unBindService() {
        if (!isServiceBound) return

        unbindService(serviceConnection)
        isServiceBound = false
    }

    private fun checkServiceRunning(): Boolean {
        val manager: ActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager

        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (BackService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun subscribeBroadCastReceiver() {
        val filter = IntentFilter().apply {
            addAction(BackService.SERVICE_SEND_DATA)
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

    private fun removeBroadCastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    /**
     * 賠率排序
     * copy from GameV3Fragment
     */
    protected fun OddsChangeEvent.sortOddsMap() {
        this.odds?.forEach { (key, value) ->
            if (value?.size ?: 0 > 3 && value?.first()?.marketSort != 0 && (value?.first()?.odds != value?.first()?.malayOdds)) {
                value?.sortBy {
                    it?.marketSort
                }
            }
        }
    }
}