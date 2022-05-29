package org.cxct.sportlottery.util

import org.cxct.sportlottery.util.DisplayUtil.px
import org.cxct.sportlottery.util.DisplayUtil.dpToPx

object LiveUtil {
    /**
     * 依照動畫寬高比例從寬度獲取高度
     * 175 + width / 16 * 9
     */
    fun getAnimationHeightFromWidth(width: Number): Float {
        val widthDP = width.toFloat().px
        return when {
            widthDP < 460 -> ((widthDP*0.699)+115).toFloat().dpToPx
            widthDP < 720 -> ((widthDP*0.699)+135).toFloat().dpToPx
            else -> ((widthDP*0.699)+145).toFloat().dpToPx
        }

//        return when {
//            widthPX < 460 -> (200+160).dp.toFloat()
//            widthPX < 720 -> (200+180).dp.toFloat()
//            else -> (200+195).dp.toFloat()
//        }
    }
}