package org.cxct.sportlottery.view.floatingbtn

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.lottery.LotteryInfo
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.maintab.lottery.LotteryActivity
import org.cxct.sportlottery.ui.maintab.menu.ScannerActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.ui.promotion.LuckyWheelActivity
import org.cxct.sportlottery.ui.splash.LaunchActivity
import org.cxct.sportlottery.ui.splash.SplashActivity
import org.cxct.sportlottery.ui.common.WebActivity
import org.cxct.sportlottery.ui.thirdGame.ThirdGameActivity
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.getMarketSwitch
import java.util.*

class LotteryManager {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.NONE) {
            LotteryManager()
        }
    }

    private var viewModel: BaseViewModel? = null
    private var activity: BaseActivity<*,*>? = null
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
    fun bind(activity: BaseActivity<*,*>) {
        this.viewModel = activity.viewModel
        this.activity = activity
        startShow()
    }

    fun onDestroy(activity: BaseActivity<*,*>) {
        if (this.activity == activity) {
            this.activity = null
            this.viewModel = null
            floatRootView = null
        }
    }

    /**
     * 限定指定页面不能显示
     */
    private fun allowdShow(): Boolean =
        if (activity == null || getMarketSwitch())
            false
        else
            when (activity!!::class) {
                SplashActivity::class,
                LaunchActivity::class,
                MaintenanceActivity::class,
                ThirdGameActivity::class,
                LotteryActivity::class,
                ScannerActivity::class,
                LuckyWheelActivity::class -> false
                WebActivity::class -> {
                    WebActivity.currentTag.isNullOrBlank()
                }
                else -> true
            }

    private fun startTimer() {
        if (countdownTimer != null) {
            return
        }
        countdownTimer = Timer()
        countdownTimer?.schedule(object : TimerTask() {
            override fun run() {
                viewModel?.viewModelScope?.launch(Dispatchers.Main) {
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
        if (!allowdShow() || lotteryInfo == null) {
            activity?.lifecycleScope?.launch(Dispatchers.Main) {
                removeFloateBtn()
            }
            return
        }
        if (System.currentTimeMillis() in showStartTime..showEndTime) {
            activity?.lifecycleScope?.launch(Dispatchers.Main) {
                showFloatBtn()
            }
        } else {
            activity?.lifecycleScope?.launch(Dispatchers.Main) {
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
        lotteryInfo?.let { it ->
            floatRootView?.let { button ->
                val nowMoment = System.currentTimeMillis()
                val nextDrawTime = it.nextDrawTime ?: 0
                val nextCloseTime = nextDrawTime - 15 * 60 * 1000
                val isSameDay =
                    TimeUtil.timeFormat(nextDrawTime, TimeUtil.YMD_FORMAT) == TimeUtil.timeFormat(
                        nowMoment,
                        TimeUtil.YMD_FORMAT
                    )
                var countdownTitle = ""
                var countdownTime = ""
                if (nextDrawTime == 0L) {
                    countdownTitle = LocalUtils.getString(R.string.N577)
                    countdownTime = TimeUtil.timeFormat(it.endTime, TimeUtil.YMD_HMS_FORMAT)
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
                        activity?.lifecycleScope?.launch(Dispatchers.Main) {
                            delay(1000)
                            getLotteryInfo()
                        }
                    }
                }
                button.setTime(countdownTitle, countdownTime)
            }
        }
    }

    fun removeFloateBtn() {
        if (floatRootView != null && activity != null) {
            var viewGroup = activity!!.findViewById<ViewGroup>(android.R.id.content)
            viewGroup.removeView(floatRootView)
        }
        floatRootView = null
    }

    fun clickOpenFloatBtn() {
        activity?.let {
            JumpUtil.toLottery(it, Constants.getLotteryH5Url(it, LoginRepository.token))
        }
    }

    fun clickCloseFloatBtn() {
        removeFloateBtn()
        lotteryInfo = null
    }

    fun isLogin(): Boolean {
        return LoginRepository.isLogin?.value == true
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