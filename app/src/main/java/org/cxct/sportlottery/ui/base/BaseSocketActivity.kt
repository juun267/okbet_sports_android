package org.cxct.sportlottery.ui.base

import android.app.ActivityManager
import android.content.*
import android.os.Bundle
import android.os.IBinder
import androidx.lifecycle.Observer
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.service.BackService
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import timber.log.Timber
import kotlin.reflect.KClass

abstract class BaseSocketActivity<T : BaseSocketViewModel>(clazz: KClass<T>) :
    BaseFavoriteActivity<T>(clazz) {

    val receiver by lazy {
        ServiceBroadcastReceiver()
    }

    private lateinit var backService: BackService

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
            startActivity(Intent(this, MaintenanceActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
        })

        receiver.serviceConnectStatus.observe(this, Observer { status ->
            when (status) {
                ServiceConnectStatus.RECONNECT_FREQUENCY_LIMIT -> {
                    hideLoading()
                    showErrorPromptDialog(getString(R.string.message_socket_connect)) { backService.doReconnect() }
                }
                else -> {
                    //do nothing
                }
            }
        })

        receiver.playQuotaChange.observe(this, {
            it?.playQuotaComData?.let { playQuotaComData ->
                viewModel.updatePlayQuota(playQuotaComData)
            }
        })

        receiver.userMoney.observe(this, {
            viewModel.updateMoney(it)
        })

        receiver.orderSettlement.observe(this, {
            viewModel.getSettlementNotification(it)
        })

        receiver.userNotice.observe(this, Observer {
            it?.userNoticeList?.let { list ->
                viewModel.setUserNoticeList(list)
            }
        })
    }

    fun subscribeSportChannelHall(gameTypeCode: String?){
        backService.subscribeSportChannelHall(gameTypeCode)
    }

    fun subscribeChannelHall(
        gameType: String?,
        cateMenuCode: String?,
        eventId: String?
    ) {
        backService.subscribeHallChannel(gameType, cateMenuCode, eventId)
    }

    fun subscribeChannelEvent(
        eventId: String?
    ) {
        backService.subscribeEventChannel(eventId)
    }

    fun unSubscribeChannelHall(
        gameType: String?,
        cateMenuCode: String?,
        eventId: String?
    ) {
        backService.unsubscribeHallChannel(gameType, cateMenuCode, eventId)
    }

    fun unSubscribeChannelEvent(eventId: String?) {
        backService.unsubscribeEventChannel(eventId)
    }

    fun unSubscribeChannelHallAll() {
        backService.unsubscribeAllHallChannel()
    }

    fun unSubscribeChannelEventAll() {
        backService.unsubscribeAllEventChannel()
    }

    fun betListPageSubscribeEvent() {
        backService.betListPageSubscribeEvent()
    }

    fun betListPageUnSubScribeEvent() {
        backService.betListPageUnSubScribeEvent()
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

        registerReceiver(receiver, filter)
    }

    private fun removeBroadCastReceiver() {
        unregisterReceiver(receiver)
    }
}