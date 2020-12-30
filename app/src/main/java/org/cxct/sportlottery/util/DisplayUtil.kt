package org.cxct.sportlottery.util

import android.content.res.Resources

object DisplayUtil {

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()

}