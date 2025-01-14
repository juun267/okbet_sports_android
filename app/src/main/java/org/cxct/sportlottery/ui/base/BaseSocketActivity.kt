package org.cxct.sportlottery.ui.base

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.viewbinding.ViewBinding
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.showPromptDialog
import org.cxct.sportlottery.common.extentions.showTokenPromptDialog
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.service.BackService
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.service.dispatcher.OrderSettlementDispatcher
import org.cxct.sportlottery.service.dispatcher.SysMaintenanceDispatcher
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.ui.splash.LaunchActivity
import org.cxct.sportlottery.ui.splash.SplashActivity
import org.cxct.sportlottery.ui.thirdGame.ThirdGameActivity
import org.cxct.sportlottery.util.GameConfigManager
import org.cxct.sportlottery.util.ToastUtil
import kotlin.reflect.KClass

abstract class BaseSocketActivity<VM : BaseSocketViewModel, VB : ViewBinding>(clazz: KClass<VM>? = null) :
    BaseActivity<VM,VB>(clazz) {

    val receiver = ServiceBroadcastReceiver
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onTokenStateChanged()
        onNetworkException()

        viewModel.isLogin.observe(this) {
            if (LoginRepository.isLogined()) {
                viewModel.getFavorite()
            } else {
                viewModel.clearFavorite()
            }
        }
        SysMaintenanceDispatcher.observe(this) {
            if(it && this.javaClass.simpleName != MaintenanceActivity::class.java.simpleName){
                startActivity(Intent(this, MaintenanceActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
            }
        }

        receiver.serviceConnectStatus.observe(this) { status ->
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
        }

        receiver.userMoney.observe(this) {
            viewModel.updateMoney(it)
        }

        receiver.lockMoney.observe(this) {
            viewModel.updateLockMoney(it)
        }

        OrderSettlementDispatcher.observe(this) {
            viewModel.getSettlementNotification(it)
        }

        receiver.userDiscountChange.observe(this) {
            viewModel.updateDiscount(it?.discountByGameTypeListList)
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


    fun subscribeChannelEvent(
        eventId: String?,
        gameType: String?=null
    ) {
        BackService.subscribeEventChannel(eventId,gameType)
    }


    fun unSubscribeChannelEvent(eventId: String?) {
        BackService.unsubscribeEventChannel(eventId)
    }

    fun unSubscribeChannelHallAll() {
        BackService.unsubscribeAllHallChannel()
    }

    fun unSubscribeChannelEventAll() {
        BackService.unsubscribeAllEventChannel()
    }

    override fun onStart() {
        super.onStart()
        BackService.connect(
            LoginRepository.token,
            LoginRepository.userId,
            LoginRepository.platformId
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
    private fun onTokenStateChanged() {
        viewModel.errorResultToken.observe(this) {

            if (this.javaClass.simpleName == MaintenanceActivity::class.java.simpleName) return@observe
            val result = it.getContentIfNotHandled() ?: return@observe
            if (result.code == HttpError.BALANCE_IS_LOW.code) {
                ToastUtil.showToast(this, result.msg)
            } else {
                toMaintenanceOrShowDialog(result)
            }
        }
    }
    private fun onNetworkException() {
        viewModel.networkExceptionUnavailable.observe(this) { netError(it) }

        viewModel.isKickedOut.observe(this) {
            hideLoading()
            it.getContentIfNotHandled()?.let { msg ->
                if (this.javaClass.simpleName == MaintenanceActivity::class.java.simpleName ||
                    this.javaClass.simpleName == ThirdGameActivity::class.java.simpleName
                ) return@observe
                viewModel.clearAll()
                showTokenPromptDialog(msg) {
                    viewModel.doLogoutCleanUser {
                        run {
                            MainTabActivity.reStart(this)
                        }
                    }
                }
            }
        }
    }
    private fun toMaintenanceOrShowDialog(result: BaseResult) {
        when (result.code) {
//            HttpError.KICK_OUT_USER.code,
//            HttpError.UNAUTHORIZED.code,
//            HttpError.DO_NOT_HANDLE.code -> { // 鉴权失败、token过期
//
//            }

            HttpError.MAINTENANCE.code -> {
                startActivity(Intent(this, MaintenanceActivity::class.java))
                finish()
            }

            else -> {
                if (this.javaClass.simpleName == MaintenanceActivity::class.java.simpleName
                    || !LoginRepository.isLogined()) {
                    return
                }

                viewModel.clearAll()
                showTokenPromptDialog(result.msg) {
                    viewModel.doLogoutCleanUser {
                        if (isErrorTokenToMainActivity()) {
                            MainTabActivity.reStart(this)
                        }
                    }
                }

            }
        }
    }

    private fun isErrorTokenToMainActivity(): Boolean {
        return when(this.javaClass.simpleName){
            MaintenanceActivity::class.java.simpleName,
            SplashActivity::class.java.simpleName,
            LaunchActivity::class.java.simpleName->false
            else->true
        }
    }

    private fun netError(errorMessage: String) {
        hideLoading()
        showPromptDialog(
            getString(R.string.prompt),
            errorMessage,
            buttonText = null,
            {  },
            isError = true,
            hasCancle = false
        )
    }



    fun onNetworkUnavailable() {
        Toast.makeText(applicationContext, R.string.connect_first, Toast.LENGTH_SHORT).show()
    }

}