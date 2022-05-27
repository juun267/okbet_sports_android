package org.cxct.sportlottery.util

import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.DisplayUtil.px

object LiveUtil {
    /**
     * 依照動畫寬高比例從寬度獲取高度
     * 175 + width / 16 * 9
     */
    fun getAnimationHeightFromWidth(width: Number): Float {
        return when {
            width.toFloat().px < 460 -> (200+160).dp.toFloat()
            width.toFloat().px < 720 -> (200+180).dp.toFloat()
            else -> (200+195).dp.toFloat()
        }
    }
}