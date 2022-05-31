package org.cxct.sportlottery.util

import org.cxct.sportlottery.util.DisplayUtil.px
import org.cxct.sportlottery.util.DisplayUtil.dpToPx

object LiveUtil {
    /**
     * 依照動畫寬高比例從寬度獲取高度
     * 40 + width / 16 * 9
     */
    fun getAnimationHeightFromWidth(width: Number): Float {
        val widthDP = width.toFloat().px
        return (widthDP*0.699+40.0).toFloat().dpToPx
    }
}