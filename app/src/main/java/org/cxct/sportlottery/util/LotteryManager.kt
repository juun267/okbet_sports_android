package org.cxct.sportlottery.util

import android.view.ViewGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.lottery.LotteryInfo
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.common.LotteryFloatingButton
import org.cxct.sportlottery.ui.maintab.lottery.LotteryActivity

class LotteryManager {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.NONE) {
            LotteryManager()
        }
    }

    private var viewModel: BaseViewModel? = null
    private var activity: BaseActivity<BaseViewModel>? = null
    private var floatRootView: LotteryFloatingButton? = null
    private var showStartTime: Long = 0
    private var showEndTime: Long = 0
    open var lotteryInfo: LotteryInfo? = null
        set(value) {
            field = value
            showStartTime = value?.showStartTime ?: 0
            showEndTime = value?.showEndTime ?: 0
        }

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
//        SplashActivity::class -> false
//        MaintenanceActivity::class -> false
        LotteryActivity::class -> false
        else -> true
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
                showFloatBtn(TimeUtil.timeFormat(lotteryInfo!!.startTime,
                    TimeUtil.YMD_HMS_FORMAT_CHANGE_LINE))
            }
        } else {
            GlobalScope.launch(Dispatchers.Main) {
                removeFloateBtn()
            }
        }
    }


    fun showFloatBtn(date: String) {
        var viewGroup = activity!!.findViewById<ViewGroup>(android.R.id.content)
        val targetView = viewGroup.getChildAt(viewGroup.childCount - 1)
        if (targetView is LotteryFloatingButton) {
            floatRootView = targetView
            floatRootView?.setTime(date)
        } else {
            if (floatRootView == null) {
                floatRootView = LotteryFloatingButton(activity!!)
            } else {
                (floatRootView?.parent as ViewGroup).removeView(floatRootView)
            }
            floatRootView?.setTime(date)
            viewGroup.addView(floatRootView)
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
}