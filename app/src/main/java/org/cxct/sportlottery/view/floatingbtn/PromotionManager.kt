package org.cxct.sportlottery.view.floatingbtn

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.callApiWithNoCancel
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.net.user.UserRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.common.WebActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.ui.promotion.LuckyWheelActivity
import org.cxct.sportlottery.ui.promotion.PromotionDetailActivity
import org.cxct.sportlottery.ui.promotion.PromotionListActivity
import org.cxct.sportlottery.ui.splash.LaunchActivity
import org.cxct.sportlottery.ui.splash.SplashActivity
import org.cxct.sportlottery.ui.thirdGame.ThirdGameActivity
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LogUtil

class PromotionManager {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.NONE) {
            PromotionManager()
        }
    }
    private var activity: BaseActivity<BaseViewModel>? = null
    private var floatRootView: PromotionFloatingButton? = null
    private var clickClose = false

    /**
     * 绑定activity和viewmodel
     * activity 主要用来处理相关红包相关布局
     * viewmodel 用来获取红包网络数据
     * 注意：若viewmodel被回收，则无法请求网络
     *
     */
    open fun bind(activity: BaseActivity<BaseViewModel>) {
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
        LuckyWheelActivity::class,
        PromotionListActivity::class,
        PromotionDetailActivity::class -> false
        WebActivity::class -> {
            WebActivity.currentTag.isNullOrBlank()
        }
        else -> true
    }

    private fun bindview() {
        if (!allowdShow()||clickClose){
            return
        }
        showFloatingBtn()
    }

    private fun showFloatingBtn() {
        var viewGroup = activity!!.findViewById<ViewGroup>(android.R.id.content)
        var targetView: View? = null
        for (index in 0..viewGroup.childCount) {
            if (viewGroup.getChildAt(index) is PromotionFloatingButton) {
                targetView = viewGroup.getChildAt(index)
                break
            }
        }
        if (targetView is PromotionFloatingButton) {
            floatRootView = targetView
        } else {
            if (floatRootView == null) {
                floatRootView = PromotionFloatingButton(activity!!)
            } else {
                (floatRootView?.parent as ViewGroup).removeView(floatRootView)
            }
            viewGroup.addView(floatRootView)
        }
        kotlin.runCatching {
            floatRootView?.startAnim()
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
        activity?.startActivity(PromotionListActivity::class.java)
        removeFloatingBtn()
    }
}