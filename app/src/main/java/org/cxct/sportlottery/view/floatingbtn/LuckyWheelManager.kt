package org.cxct.sportlottery.view.floatingbtn

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.callApiWithNoCancel
import org.cxct.sportlottery.net.user.UserRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.common.WebActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.ui.promotion.LuckyWheelActivity
import org.cxct.sportlottery.ui.splash.LaunchActivity
import org.cxct.sportlottery.ui.splash.SplashActivity
import org.cxct.sportlottery.ui.thirdGame.ThirdGameActivity
import org.cxct.sportlottery.util.JumpUtil

class LuckyWheelManager {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.NONE) {
            LuckyWheelManager()
        }
    }
    private var startTime: Long=0
    private var endTime: Long=0
    private var activity: BaseActivity<*,*>? = null
    private var floatRootView: LuckyWheelFloatingButton? = null
    private var clickClose = false

    /**
     * 绑定activity和viewmodel
     * activity 主要用来处理相关红包相关布局
     * viewmodel 用来获取红包网络数据
     * 注意：若viewmodel被回收，则无法请求网络
     *
     */
    open fun bind(activity: BaseActivity<*,*>) {
        this.activity = activity
        bindview()
    }

    /**
     * 限定指定页面不能显示红包相关的
     */
    fun allowdShow(): Boolean =
        when (activity!!::class) {
        SplashActivity::class,
        LaunchActivity::class,
        MaintenanceActivity::class,
        ThirdGameActivity::class,
        LuckyWheelActivity::class -> false
        WebActivity::class -> {
            !WebActivity.currentTag.isNullOrBlank()
        }
        else -> true
    }

    private fun bindview() {
        if (!allowdShow()||clickClose){
            return
        }
        if (startTime==0L && endTime==0L){
            getTimeRange()
            return
        }
        val currentTime = System.currentTimeMillis()
        if (currentTime in startTime..endTime) {
            activity?.viewModel?.viewModelScope?.launch(Dispatchers.Main) {
                showFloatingBtn()
            }
        } else {
            activity?.viewModel?.viewModelScope?.launch(Dispatchers.Main) {
                removeFloatingBtn()
            }
        }
    }

    private fun showFloatingBtn() {
        var viewGroup = activity!!.findViewById<ViewGroup>(android.R.id.content)
        var targetView: View? = null
        for (index in 0..viewGroup.childCount) {
            if (viewGroup.getChildAt(index) is LuckyWheelFloatingButton) {
                targetView = viewGroup.getChildAt(index)
                break
            }
        }
        if (targetView is LuckyWheelFloatingButton) {
            floatRootView = targetView
        } else {
            if (floatRootView == null) {
                floatRootView = LuckyWheelFloatingButton(activity!!)
            } else {
                (floatRootView?.parent as ViewGroup).removeView(floatRootView)
            }
            viewGroup.addView(floatRootView)
        }
    }
    private fun removeFloatingBtn(){
        if (floatRootView!=null){
            var viewGroup = activity!!.findViewById<ViewGroup>(android.R.id.content)
            viewGroup.removeView(floatRootView)
            floatRootView=null
        }
    }

    fun clickCloseFloatBtn() {
        clickClose=true
        removeFloatingBtn()
    }
    fun clickContent() {
        activity?.let {
            JumpUtil.toInternalWeb(it,
                "/promo/christmas",
                it.getString(R.string.P169))
        }
        removeFloatingBtn()
    }
    fun getTimeRange(){
        callApiWithNoCancel({UserRepository.getWheelActivityInfo()}){
            it.getData()?.let {
                startTime = it.eventTimeStart
                endTime = it.eventTimeEnd
                bindview()
            }
        }
    }
}