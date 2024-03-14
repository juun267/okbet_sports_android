package org.cxct.sportlottery.util

import org.cxct.sportlottery.application.MultiLanguagesApplication

object DisplayUtil {
    /*
     * dp to px
     * ex : view.height = 15.dp
     */
    val Int.dp: Int
        get() = (this * MultiLanguagesApplication.appContext.resources.displayMetrics.density).toInt()
    val Float.dpToPx: Float
        get() = (this * MultiLanguagesApplication.appContext.resources.displayMetrics.density)
    val Int.pxToDp: Int
        get() = (this / MultiLanguagesApplication.appContext.resources.displayMetrics.density).toInt()
    val Float.px: Float
        get() = (this / MultiLanguagesApplication.appContext.resources.displayMetrics.density)
    val Float.dp: Int
        get() = (this * MultiLanguagesApplication.appContext.resources.displayMetrics.density).toInt()

    val screenWith: Int
        get() =  MultiLanguagesApplication.appContext.resources.displayMetrics.widthPixels
    val screenHeight: Int
        get() =  MultiLanguagesApplication.appContext.resources.displayMetrics.heightPixels
}