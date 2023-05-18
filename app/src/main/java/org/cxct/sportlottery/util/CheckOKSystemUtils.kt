package org.cxct.sportlottery.util

import android.content.Context
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData

object CheckOKSystemUtils {
}

/**
 * 检查是否关闭体育入口
 */
fun checkSportStatus(context: Context, block: () -> Unit) {
    if(getSportEnterIsClose()){
        ToastUtil.showToast(context, context.getString(R.string.N969))
    }else{
        block()
    }
}

/**
 * 是否关闭体育入口  return true 是关闭
 */
fun getSportEnterIsClose():Boolean{
//    status=0关闭  1开启
//    val status = sConfigData?.sportMaintainStatus
//    return "1"==status
    return false
}