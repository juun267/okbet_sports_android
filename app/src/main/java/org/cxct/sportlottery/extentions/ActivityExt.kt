package org.cxct.sportlottery.extentions

import android.view.View
import org.cxct.sportlottery.util.ScreenUtil

//顶部偏移状态栏高度
fun View.fitsSystemStatus() {

    val statuHeight = ScreenUtil.getStatusBarHeight(context)
    if (layoutParams.height > 0) {
        layoutParams.height = layoutParams.height + statuHeight
    }
    setPadding(paddingLeft, paddingTop + statuHeight, paddingRight, paddingBottom)
}