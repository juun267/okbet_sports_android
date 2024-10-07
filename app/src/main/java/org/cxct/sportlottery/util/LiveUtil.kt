package org.cxct.sportlottery.util

import org.cxct.sportlottery.util.DisplayUtil.px
import org.cxct.sportlottery.util.DisplayUtil.dpToPx

object LiveUtil {
    /**
     * 依照動畫寬高比例從寬度獲取高度
     * 後端提供的寬高公式：
     * (404/800 * width) + 92
     */
    fun getAnimationHeightFromWidth(width: Number): Float {
        val widthDP = width.toFloat().px
        return ((0.505 * widthDP) + 92).toFloat().dpToPx
    }
}