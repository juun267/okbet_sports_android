package org.cxct.sportlottery.ui.base

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.lifecycle.Observer
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.showErrorPromptDialog
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.repository.NAME_LOGIN
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.service.BackService
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.util.GameConfigManager
import kotlin.reflect.KClass

abstract class BaseSocketActivity<T : BaseSocketViewModel>(clazz: KClass<T>) :
    BaseActivity<T>(clazz) {

    private val sharedPref: SharedPreferences by lazy {
        this.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    val receiver = ServiceBroadcastReceiver
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.isLogin.observe(this) {
            if (it == true) {
                viewModel.getFavorite()
            } else {
                viewModel.clearFavorite()
            }
        }
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
                    BackService.doReconnect()
                    //不需要弹窗提示，直接无限重连
//                    showPromptDialog(
//                        getString(R.string.prompt),
//                        getString(R.string.message_socket_connect),
//                        buttonText = null,
//                        { BackService.doReconnect() },
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

        receiver.userDiscountChange.observe(this) {
            viewModel.updateDiscount(it?.discountByGameTypeListList)
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
            if (viewModel.isLogin.value == true &&
                UserInfoRepository.getUserLevelId().toString() == it?.userLevelConfigListList?.firstOrNull()?.id.toString()) {

                GameConfigManager.maxBetMoney =
                    it?.userLevelConfigListList?.firstOrNull()?.maxBetMoney ?: 9999999
                GameConfigManager.maxParlayBetMoney =
                    it?.userLevelConfigListList?.firstOrNull()?.maxParlayBetMoney ?: 99999
                GameConfigManager.maxCpBetMoney =
                    it?.userLevelConfigListList?.firstOrNull()?.maxCpBetMoney ?: 99999
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
        BackService.subscribeSportChannelHall()
    }

    fun subscribeChannelHall(
        gameType: String?,
        eventId: String?
    ) {
        BackService.subscribeHallChannel(gameType, eventId)
    }

    fun subscribeChannelEvent(
        eventId: String?,
        gameType: String?=null
    ) {
        BackService.subscribeEventChannel(eventId,gameType)
    }

    fun unSubscribeChannelHall(
        gameType: String?,
        eventId: String?,
    ) {
        BackService.unsubscribeHallChannel(gameType, eventId)
    }

    fun unSubscribeChannelHall(
        eventId: String?,
    ) {
        BackService.unsubscribeHallChannel(eventId)
    }

    fun unSubscribeChannelEvent(eventId: String?) {
        BackService.unsubscribeEventChannel(eventId)
    }

    fun unsubscribeHallChannel(eventId: String?) {
        BackService.unsubscribeHallChannel(eventId)
    }

    fun unSubscribeChannelHallAll() {
        BackService.unsubscribeAllHallChannel()
    }

    fun unSubscribeChannelHallSport() {
        BackService.unsubscribeSportHallChannel()
    }

    fun unSubscribeChannelEventAll() {
        BackService.unsubscribeAllEventChannel()
    }

    fun betListPageSubscribeEvent() {
        BackService.betListPageSubscribeEvent()
    }

    fun betListPageUnSubScribeEvent() {
        BackService.betListPageUnSubScribeEvent()
    }

    override fun onStart() {
        super.onStart()
        BackService.connect(
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