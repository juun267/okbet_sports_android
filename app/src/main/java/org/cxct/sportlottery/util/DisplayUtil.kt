package org.cxct.sportlottery.util

import android.content.res.Resources

object DisplayUtil {

    /*
     * dp to px
     * ex : view.height = 15.dp
     */
    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Float.px: Float
        get() = (this / Resources.getSystem().displayMetrics.density)

}