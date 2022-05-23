package org.cxct.sportlottery.util

object LiveUtil {
    /**
     * 賽事動畫寬高比 寬400 : 高385
     */
    const val ANIMATION_ASPECT_RATIO = 400f / 385f

    /**
     * 依照動畫寬高比例從寬度獲取高度
     */
    fun getAnimationHeightFromWidth(width: Number): Float {
        return width.toFloat() / ANIMATION_ASPECT_RATIO
    }
}