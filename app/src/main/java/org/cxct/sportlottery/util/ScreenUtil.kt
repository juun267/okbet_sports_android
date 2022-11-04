package org.cxct.sportlottery.util

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

object ScreenUtil {

    private var sWidth = 0
    private var sHeight = 0
    private var statusBarH = 0

    fun getScreenWidth(context: Context): Int {
        if (sWidth > 0) {
            return sWidth
        }
        val dm = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        display.getMetrics(dm)
        sWidth = dm.widthPixels
        return dm.widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        if (sHeight > 0) {
            return sHeight
        }
        val dm = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        display.getMetrics(dm)
        sHeight = dm.heightPixels
        return dm.heightPixels
    }

    fun getStatusBarHeight(context: Context): Int {
        if (statusBarH > 0) {
            return statusBarH
        }
        val resourceId: Int = context.getResources().getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarH = context.getResources().getDimensionPixelSize(resourceId)
        }
        return statusBarH
    }
}