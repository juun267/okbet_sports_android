package org.cxct.sportlottery.extentions

import android.app.Activity
import android.content.Context
import android.content.Intent

//屏幕宽度(px)
inline val Context.screenWidth: Int
    get() = resources.displayMetrics.widthPixels

//屏幕高度(px)
inline val Context.screenHeight: Int
    get() = resources.displayMetrics.heightPixels

//屏幕的密度
inline val Context.density: Float
    get() = resources.displayMetrics.density

//dp 转为 px
inline fun Context.dp2px(value: Int): Int = (density * value).toInt()

//dp 转为 px
inline fun Context.dp2px(value: Float): Int = (density * value).toInt()

//px 转为 dp
inline fun Context.px2dp(value: Int): Float = value.toFloat() / density

//使用内联函数的泛型参数 reified 特性来实现
inline fun <reified T : Activity> Context.startActivity() {
    val intent = Intent(this, T::class.java)
    if (this !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}