package org.cxct.sportlottery.util

import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity


/**
 * 检查是否关闭体育入口
 */
fun checkSportStatus(activity: BaseActivity<*>, block: () -> Unit) {
    if(getSportEnterIsClose()){
        activity.showPromptDialogNoCancel(message = activity.getString(R.string.N969)) { }
//        ToastUtil.showToast(context, context.getString(R.string.N969))
    }else{
        block()
    }
}

/**
 * 是否关闭体育入口  return true 是关闭体育入口
 */
 fun getSportEnterIsClose():Boolean{
//    status=0关闭维护  1开启维护
    val status = sConfigData?.sportMaintainStatus
    return "1"==status
//    return false
}

/**
 * 更新体育维护字段
 */
private fun updateSportStatus(status:Int?){
    sConfigData?.sportMaintainStatus="$status"
}


/**
 * 监听体育服务广播
 */
fun setupSportStatusChange(lifecycleOwner: LifecycleOwner, block: (isOpen:Boolean) -> Unit){
    ServiceBroadcastReceiver.sportMaintenance.observe(lifecycleOwner){
        it?.let {
            //更新体育开关字段
            updateSportStatus(it.status)
            block(getSportEnterIsClose())
        }
    }

}
/**
 * 如果mainActivity在体育相关页面，需要回退到首页
 */
fun MainTabActivity.checkMainPosition(position:Int):Boolean{
    //是否为体育页面
    val result=checkSportFragment(position)
    //在体育页面&&维护开启
    val flag=result&&getSportEnterIsClose()
    if(flag && lifecycle.currentState == androidx.lifecycle.Lifecycle.State.RESUMED){
        showPromptDialogNoCancel(message = getString(R.string.N969)) { }
//        ToastUtil.showToast(this, getString(R.string.N969))
    }
    return flag
}

fun BaseActivity<*>.bindSportMaintenance() {
    ServiceBroadcastReceiver.sportMaintenance.observe(this){
        if (it.status == 1) {
            showPromptDialogNoCancel(message = getString(R.string.N969)) { finish() }
        }
    }
}

/**
 * 开启维护，显示/隐藏view
 */
fun View.goneWithSportSwitch(isGone:Boolean=true){
    if(isGone){
        this.isVisible=!getSportEnterIsClose()
    }else{
        this.isVisible=getSportEnterIsClose()
    }
}
