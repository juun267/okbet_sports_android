package org.cxct.sportlottery.util

import android.content.Context
import android.content.res.Resources
import org.cxct.sportlottery.util.DisplayUtil.dp
import kotlin.math.max

object MetricsUtil {

    /**
     * Covert dp to px
     * @param dp
     * @param context
     * @return pixel
     */
    fun convertDpToPixel(dp: Float, context: Context?): Float {
        return dp * getDensity(context)
    }

    /**
     * Covert px to dp
     * @param px
     * @param context
     * @return dp
     */
    fun convertPixelToDp(px: Float, context: Context?): Float {
        return px / getDensity(context)
    }

    /**
     * 取得螢幕密度
     * 120dpi = 0.75
     * 160dpi = 1 (default)
     * 240dpi = 1.5
     * @param context
     * @return
     */
    fun getDensity(context: Context?): Float {
        val metrics = if (context != null)
            context.resources.displayMetrics
        else
            Resources.getSystem().displayMetrics
        return metrics.density
    }

    fun getScreenWidth(): Int {
        val metrics = Resources.getSystem().displayMetrics
        return metrics.widthPixels // 螢幕寬度（畫素
    }

    fun getScreenHeight(): Int {
        val metrics = Resources.getSystem().displayMetrics
        return metrics.heightPixels // 螢幕寬度（畫素
    }

    //側邊欄寬度(依螢幕寬比例)
    fun getMenuWidth(): Int {
        return max((getScreenWidth() * 0.68).toInt(), 255.dp)
    }

    fun getStatusBarHeight(): Int {
        val rid = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
        return if (rid > 0) Resources.getSystem().getDimensionPixelSize(rid) else 0

    }

}