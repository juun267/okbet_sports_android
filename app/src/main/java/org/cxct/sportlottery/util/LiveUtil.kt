package org.cxct.sportlottery.util

object LiveUtil {
    /**
     * 依照動畫寬高比例從寬度獲取高度
     * 175 + width / 16 * 9
     */
    fun getAnimationHeightFromWidth(width: Number): Float {
        return (width.toFloat() / 16 * 9) + 175
    }
}