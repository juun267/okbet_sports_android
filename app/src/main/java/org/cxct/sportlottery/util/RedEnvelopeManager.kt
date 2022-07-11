package org.cxct.sportlottery.util

import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.money.RedEnvelopeResult
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.RedEnvelopeFloatingButton
import org.cxct.sportlottery.ui.dialog.RedEnvelopeReceiveDialog
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.ui.splash.SplashActivity
import org.cxct.sportlottery.ui.thirdGame.ThirdGameActivity
import java.util.*

class RedEnvelopeManager {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.NONE) {
            RedEnvelopeManager()
        }
    }

    private var redenpId: Int = 0
    private var redenpStartTime: Long? = null
    private var redenpEndTime: Long? = null
    private var count = 0
    private var countdownTimer: Timer? = null

    var showedRedenpId = -1 //顯示過的紅包id
    private var viewModel: BaseViewModel? = null
    private var activity: BaseActivity<BaseViewModel>? = null
    private var closeDialog: CustomAlertDialog? = null
    private var redEnvelopeReceiveDialog: RedEnvelopeReceiveDialog? = null

    /**
     * 绑定activity和viewmodel
     * activity 主要用来处理相关红包相关布局
     * viewmodel 用来获取红包网络数据
     * 注意：若viewmodel被回收，则无法请求网络
     *
     */
    open fun bind(activity: BaseActivity<BaseViewModel>) {
        this.viewModel = activity.viewModel
        this.activity = activity
        startTimer()
    }

    /**
     * 限定指定页面不能显示红包相关的
     */
    fun allowdShowRedEnvelope(): Boolean = when (activity!!::class) {
        SplashActivity::class -> false
        MaintenanceActivity::class -> false
        ThirdGameActivity::class -> false
        else -> true
    }

    private fun startTimer() {
        if (countdownTimer != null) {
            return
        }
        countdownTimer = Timer()
        countdownTimer?.schedule(object : TimerTask() {
            override fun run() {
                if (!isLogin() || !allowdShowRedEnvelope()) {
                    removeRedEnvelopeBtn()
                    return
                }
                if (count % 10 == 0) {
                    getRain()
                    count = 0
                }
                count++
                if (redEnvelopeReceiveDialog == null) {
                    if (showedRedenpId == redenpId) return
                    val startTimeDiff = ((redenpStartTime ?: 0) - System.currentTimeMillis()) / 1000
                    val endTimeDiff = ((redenpEndTime ?: 0) - System.currentTimeMillis()) / 1000
                    if (startTimeDiff in 1..180) {
                        GlobalScope.launch(Dispatchers.Main) {
                            showRedEnvelopeBtn(startTimeDiff)
                        }
                    } else if (startTimeDiff <= 0 && endTimeDiff >= 0) {
                        showedRedenpId = redenpId
                        redEnvelopeReceiveDialog = RedEnvelopeReceiveDialog(activity, redenpId)
                        redEnvelopeReceiveDialog?.show(
                            activity!!.supportFragmentManager,
                            activity!!::class.java.simpleName
                        )
                        GlobalScope.launch(Dispatchers.Main) {
                            removeRedEnvelopeBtn()
                            closeDialog?.dismiss()
                        }
                    }
                } else  {
                    val endTimeDiff = ((redenpEndTime ?: 0) - System.currentTimeMillis()) / 1000
                    if (endTimeDiff < 0) {
                        redEnvelopeReceiveDialog?.dismiss()
                        redEnvelopeReceiveDialog = null
                    }
                }
            }
        }, 1000, 1000)
    }
    fun stop(){
        countdownTimer?.cancel()
        countdownTimer=null
        removeRedEnvelopeBtn()
    }


    fun getRain() {
        viewModel?.let{
            it.viewModelScope.launch {
                it.doNetwork(MultiLanguagesApplication.appContext) {
                    OneBoSportApi.moneyService.getRainInfo()
                }?.let { result ->
                    dealWithRainResult(result)
                }
            }
        }
    }
    private fun dealWithRainResult(result:RedEnvelopeResult) {
            val redEnvelopeInfo = result.redEnvelopeInfo
            if (redEnvelopeInfo != null) {
                val serverTime = redEnvelopeInfo.serverTime
                val difference = serverTime - System.currentTimeMillis()

                redenpId = redEnvelopeInfo.redenpId
                redenpStartTime = redEnvelopeInfo.redenpStartTime - difference
                redenpEndTime = redEnvelopeInfo.redenpEndTime - difference
            }

    }

    var floatRootView: RedEnvelopeFloatingButton? = null

    fun showRedEnvelopeBtn(countTime:Long){
        var viewGroup = activity!!.findViewById<ViewGroup>(android.R.id.content)
        val targetView = viewGroup.getChildAt(viewGroup.childCount - 1)
        if (targetView is RedEnvelopeFloatingButton){
            floatRootView= targetView
            floatRootView?.setCountdown(countTime)
        }else{
            if (floatRootView==null) {
                floatRootView = RedEnvelopeFloatingButton(activity!!)
            }else{
                (floatRootView?.parent as ViewGroup).removeView(floatRootView)
            }
            floatRootView?.setCountdown(countTime)
            viewGroup.addView(floatRootView)
        }
    }
    fun removeRedEnvelopeBtn(){
        if (floatRootView!=null){
            var viewGroup = activity!!.findViewById<ViewGroup>(android.R.id.content)
            viewGroup.removeView(floatRootView)
            floatRootView=null
        }
    }
    fun clickCloseFloatBtn(){
        val positiveClickListener = {
            //點選關閉，更新顯示過的紅包id
            showedRedenpId = redenpId
            removeRedEnvelopeBtn()

        }
        val negativeClickListener = {

        }
        closeDialog = commonTwoButtonDialog(
            context = activity!!,
            fm = activity!!.supportFragmentManager,
            isError = false,
            isShowDivider = true,
            buttonText = LocalUtils.getString(R.string.btn_sure),
            cancelText = LocalUtils.getString(R.string.btn_cancel_new),
            positiveClickListener = positiveClickListener,
            negativeClickListener = negativeClickListener,
            title = LocalUtils.getString(R.string.prompt),
            errorMessage = LocalUtils.getString(R.string.redenvelope_close_hint)
        )
    }

    fun isLogin(): Boolean {
        return viewModel?.isLogin?.value == true
    }
}