package org.cxct.sportlottery.util

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.money.RedEnvelopeResult
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.common.RedEnvelopeFloatingButton
import org.cxct.sportlottery.ui.dialog.RedEnvelopeReceiveDialog
import java.util.*

class RedEnvelopeManager() {
    companion object{
        val instance by lazy(LazyThreadSafetyMode.NONE){
            RedEnvelopeManager()
        }
    }
    private var redenpId: Int = 0
    private var redenpStartTime: Long? = null
    private var redenpEndTime: Long? = null
    private var count = 0
    private var countdownTimer: Timer? = null

    var showedRedenpId = -1 //顯示過的紅包id
    private var viewModel:BaseViewModel?=null


    open fun bindViewModel(viewModel:BaseViewModel ){
        this.viewModel=viewModel
        startTimer()
    }

    private val logRedEnvelopeReceiveDialog by lazy {
        RedEnvelopeReceiveDialog(MultiLanguagesApplication.appContext, redenpId)
    }
    private fun startTimer() {
        if (countdownTimer!=null){
            return
        }
        countdownTimer = Timer()
        countdownTimer?.schedule(object : TimerTask() {
            override fun run() {
                if (LoginRepository(MultiLanguagesApplication.appContext).isLogin.value == false) {
                    return
                }
                if (count % 10 == 0) {
                    getRain()
                    count = 0
                }
                count++
                if (logRedEnvelopeReceiveDialog.dialog==null||logRedEnvelopeReceiveDialog.dialog?.isShowing == false) {
                    if (showedRedenpId == redenpId) return
                    val startTimeDiff =
                        ((redenpStartTime ?: 0) - System.currentTimeMillis()) / 1000
                    val endTimeDiff = ((redenpEndTime ?: 0) - System.currentTimeMillis()) / 1000
                    if (startTimeDiff in 1..180) {
                        GlobalScope.launch(Dispatchers.Main) {
                            if (showedRedenpId != redenpId) {
                                showRedEnvelopeBtn(startTimeDiff)
                            }
                            //180s 倒计时
                            showRedEnvelopeBtn(startTimeDiff)
                        }
                    } else if (startTimeDiff <= 0 && endTimeDiff >= 0) {
                            showedRedenpId = redenpId
                            logRedEnvelopeReceiveDialog.redenpId=redenpId
                            logRedEnvelopeReceiveDialog.show(
                                (AppManager.currentActivity() as AppCompatActivity).supportFragmentManager,
                                AppManager.currentActivity()::class.java.simpleName
                            )
                            GlobalScope.launch(Dispatchers.Main) {
                                removeRedEnvelopeBtn()
                            }
                    }
                } else  {
                    val endTimeDiff = ((redenpEndTime ?: 0) - System.currentTimeMillis()) / 1000
                    if (endTimeDiff < 0) {
                        if (logRedEnvelopeReceiveDialog.dialog?.isShowing == true) {
                            logRedEnvelopeReceiveDialog.dismiss()
                        }
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
            it.viewModelScope?.launch {
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
        var viewGroup = AppManager.currentActivity().findViewById<ViewGroup>(android.R.id.content)
        val targetView=viewGroup.getChildAt(viewGroup.childCount-1)
        if (targetView is RedEnvelopeFloatingButton){
            floatRootView= targetView
            floatRootView?.setCountdown(countTime)
        }else{
            if (floatRootView==null) {
                floatRootView = RedEnvelopeFloatingButton(AppManager.currentActivity())
            }else{
                (floatRootView?.parent as ViewGroup).removeView(floatRootView)
            }
            floatRootView?.setCountdown(countTime)
            viewGroup.addView(floatRootView)
        }
    }
    fun removeRedEnvelopeBtn(){
        if (floatRootView!=null){
            var viewGroup = AppManager.currentActivity().findViewById<ViewGroup>(android.R.id.content)
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
        commonTwoButtonDialog(
            context = AppManager.currentActivity(),
            fm = (AppManager.currentActivity() as AppCompatActivity).supportFragmentManager,
            isError = false,
            isShowDivider = true,
            buttonText = null,
            cancelText = null,
            positiveClickListener = positiveClickListener,
            negativeClickListener = negativeClickListener,
            title = LocalUtils.getString(R.string.prompt),
            errorMessage = LocalUtils.getString(R.string.redenvelope_close_hint)
        )

    }
}