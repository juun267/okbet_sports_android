package org.cxct.sportlottery.util

import android.content.res.Resources

object DisplayUtil {
    /*
     * dp to px
     * ex : view.height = 15.dp
     */
    @Deprecated("未适配像素密度，建议使用context.dp2px来替代", ReplaceWith(
        "(this * Resources.getSystem().displayMetrics.density).toInt()",
        "org.cxct.sportlottery.util.DisplayUtil.dp",
        "android.content.res.Resources"
    )
    )
    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
    val Float.dpToPx: Float
        get() = (this * Resources.getSystem().displayMetrics.density)
    val Int.pxToDp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Float.px: Float
        get() = (this / Resources.getSystem().displayMetrics.density)
    val Float.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}