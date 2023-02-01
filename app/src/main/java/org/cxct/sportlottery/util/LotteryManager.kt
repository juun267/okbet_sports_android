package org.cxct.sportlottery.util

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.lottery.LotteryInfo
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.common.LotteryFloatingButton
import org.cxct.sportlottery.ui.maintab.lottery.LotteryActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.ui.splash.SplashActivity
import org.cxct.sportlottery.ui.thirdGame.ThirdGameActivity
import java.util.*

class LotteryManager {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.NONE) {
            LotteryManager()
        }
    }

    private var viewModel: BaseViewModel? = null
    private var activity: BaseActivity<BaseViewModel>? = null
    private var floatRootView: LotteryFloatingButton? = null
    private var countdownTimer: Timer? = null
    private var showStartTime: Long = 0
    private var showEndTime: Long = 0
    private var lotteryInfo: LotteryInfo? = null

    /**
     * 绑定activity和viewmodel
     * activity 主要用来处理相关布局
     * viewmodel 用来获取网络数据
     * 注意：若viewmodel被回收，则无法请求网络
     *
     */
    open fun bind(activity: BaseActivity<BaseViewModel>) {
        this.viewModel = activity.viewModel
        this.activity = activity
        startShow()
    }

    /**
     * 限定指定页面不能显示
     */
    fun allowdShowRedEnvelope(): Boolean = when (activity!!::class) {
        SplashActivity::class -> false
        MaintenanceActivity::class -> false
        ThirdGameActivity::class -> false
        LotteryActivity::class -> false
        else -> true
    }
    private fun startTimer() {
        if (countdownTimer != null) {
            return
        }
        countdownTimer = Timer()
        countdownTimer?.schedule(object : TimerTask() {
            override fun run() {
                GlobalScope.launch(Dispatchers.Main) {
                    val currentTimeStamp = System.currentTimeMillis()
                    if (currentTimeStamp in showStartTime..showEndTime) {
                        setUpFloatButton()
                    } else {
                        countdownTimer?.cancel()
                        countdownTimer = null
                        removeFloateBtn()
                    }
                }
            }
        }, 1000, 1000)
    }
    private fun startShow() {
        if (!allowdShowRedEnvelope() || lotteryInfo == null) {
            GlobalScope.launch(Dispatchers.Main) {
                removeFloateBtn()
            }
            return
        }
        if (System.currentTimeMillis() in showStartTime..showEndTime) {
            GlobalScope.launch(Dispatchers.Main) {
                showFloatBtn()
            }
        } else {
            GlobalScope.launch(Dispatchers.Main) {
                removeFloateBtn()
            }
        }
    }

    fun showFloatBtn() {
        var viewGroup = activity!!.findViewById<ViewGroup>(android.R.id.content)
        var targetView: View? = null
        for (index in 0..viewGroup.childCount) {
            if (viewGroup.getChildAt(index) is LotteryFloatingButton) {
                targetView = viewGroup.getChildAt(index)
                break
            }
        }
        if (targetView is LotteryFloatingButton) {
            floatRootView = targetView
            setUpFloatButton()
        } else {
            if (floatRootView == null) {
                floatRootView = LotteryFloatingButton(activity!!)
            } else {
                (floatRootView?.parent as ViewGroup).removeView(floatRootView)
            }
            setUpFloatButton()
            viewGroup.addView(floatRootView)
        }
    }

    fun setUpFloatButton() {
        lotteryInfo?.let {
            val nowMoment = System.currentTimeMillis()
            val nextDrawTime = it.nextDrawTime ?: 0
            val nextCloseTime = nextDrawTime - 15 * 60 * 1000
            val isSameDay =
                TimeUtil.timeFormat(nextDrawTime, TimeUtil.YMD_FORMAT) == TimeUtil.timeFormat(
                    nowMoment,
                    TimeUtil.YMD_FORMAT)
            var countdownTitle = ""
            var countdownTime = ""
            if (nextDrawTime == 0L) {
                countdownTitle = LocalUtils.getString(R.string.end_time)
                countdownTime = TimeUtil.timeFormat(showEndTime, TimeUtil.YMD_HMS_FORMAT)
            } else if (!isSameDay) {
                // 1 非当天 显示 开奖时间（抽奖前 抽奖后）
                countdownTitle = LocalUtils.getString(R.string.draw_time)
                countdownTime = TimeUtil.timeFormat(nextDrawTime, TimeUtil.YMD_HMS_FORMAT)
            } else if (nowMoment < nextCloseTime || nowMoment > nextDrawTime) {
                // 2 当天 入口未关闭（15分钟）
                countdownTitle = LocalUtils.getString(R.string.closing_time)
                countdownTime = TimeUtil.timeFormat(nextCloseTime, TimeUtil.YMD_HMS_FORMAT)
            } else {
                // 3 入口关闭 进入倒计时
                countdownTitle = LocalUtils.getString(R.string.draw_countdown)
                var diff = nextDrawTime - nowMoment
                countdownTime = TimeUtil.timeFormat(diff, TimeUtil.HM_FORMAT_MS)
                if (countdownTime == "00:00") {
                    Handler(Looper.getMainLooper()).postDelayed({
                        getLotteryInfo()
                    }, 1000)
                }
            }
            floatRootView?.setTime(countdownTitle, countdownTime)
        }
    }

    fun removeFloateBtn() {
        if (floatRootView != null) {
            var viewGroup = activity!!.findViewById<ViewGroup>(android.R.id.content)
            viewGroup.removeView(floatRootView)
            floatRootView = null
        }
    }

    fun clickOpenFloatBtn() {
        activity?.let {
            JumpUtil.toLottery(it, Constants.getLotteryH5Url(it, viewModel!!.loginRepository.token))
        }
    }

    fun clickCloseFloatBtn() {
        removeFloateBtn()
        lotteryInfo = null
    }

    fun isLogin(): Boolean {
        return viewModel?.isLogin?.value == true
    }
    fun getLotteryInfo() {
        viewModel?.let {
            it.viewModelScope.launch {
                it.doNetwork(MultiLanguagesApplication.appContext) {
                    OneBoSportApi.lotteryService.getLotteryResult()
                }?.let { result ->
                    lotteryInfo = result.t
                    lotteryInfo?.let {
                        showStartTime = it.showStartTime ?: 0
                        showEndTime = it.showEndTime ?: 0
                        startTimer()
                        startShow()
                    }
                }
            }
        }
    }
}