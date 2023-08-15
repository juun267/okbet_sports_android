package org.cxct.sportlottery.ui.base

import android.app.ActivityManager
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.text.SpannableStringBuilder
import androidx.lifecycle.Observer
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.repository.KEY_USER_LEVEL_ID
import org.cxct.sportlottery.repository.NAME_LOGIN
import org.cxct.sportlottery.service.BackService
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.util.GameConfigManager
import timber.log.Timber
import kotlin.reflect.KClass

abstract class BaseSocketActivity<T : BaseSocketViewModel>(clazz: KClass<T>) :
    BaseFavoriteActivity<T>(clazz) {

    private val sharedPref: SharedPreferences by lazy {
        this.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    val receiver = ServiceBroadcastReceiver

    private var backService: BackService = BackService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        receiver.sysMaintenance.observe(this, Observer {
            if ((it?.status ?: 0) == MaintenanceActivity.MaintainType.FIXING.value) {
                when (this) {
                    !is MaintenanceActivity -> startActivity(
                        Intent(
                            this,
                            MaintenanceActivity::class.java
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        })
                }
            }
        })

        receiver.serviceConnectStatus.observe(this, Observer { status ->
            when (status) {
                ServiceConnectStatus.RECONNECT_FREQUENCY_LIMIT -> {
                    hideLoading()
                    backService?.doReconnect()
                    //不需要弹窗提示，直接无限重连
//                    showPromptDialog(
//                        getString(R.string.prompt),
//                        getString(R.string.message_socket_connect),
//                        buttonText = null,
//                        { backService?.doReconnect() },
//                        isError = true,
//                        hasCancle = false
//                    )
                }
                ServiceConnectStatus.CONNECTING -> {
//                    loading()
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
            dataSourceChangeEven?.let {
                showErrorPromptDialog(
                    title = getString(R.string.prompt),
                    message = SpannableStringBuilder().append(getString(R.string.message_source_change)),
                    hasCancel = false
                ) { it.invoke() }
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

    private var dataSourceChangeEven: (() -> Unit)? = null

    /**
     * 设置有新赛事数据监听回调。
     *  重点!!!
     *  页面加载完成后再调用该方法就行设置回调，
     *  不然由于LiveData粘性事件的原因，在页面初始化的时候就有可能弹窗
      */
    fun setDataSourceChangeEvent(dataSourceChangeEven: () -> Unit) {
        this.dataSourceChangeEven = dataSourceChangeEven
    }

    fun subscribeSportChannelHall() {
        backService?.subscribeSportChannelHall()
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
        eventId: String?,
    ) {
        backService?.unsubscribeHallChannel(gameType, eventId)
    }

    fun unSubscribeChannelHall(
        eventId: String?,
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

    fun unSubscribeChannelHallSport() {
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
        backService.connect(
            viewModel.loginRepository.token,
            viewModel.loginRepository.userId,
            viewModel.loginRepository.platformId
        )
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

}