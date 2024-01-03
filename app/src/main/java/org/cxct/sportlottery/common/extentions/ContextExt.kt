package org.cxct.sportlottery.common.extentions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

val handler = Handler(Looper.getMainLooper())
inline fun post(runnable: Runnable) {
    handler.post(runnable)
}
inline fun postDelayed(delayMillis: Long,runnable: Runnable) {
    handler.postDelayed(runnable,delayMillis)
}

//屏幕宽度(px)
inline val Context.screenWidth: Int
    get() = resources.displayMetrics.widthPixels

//屏幕高度(px)
inline val Context.screenHeight: Int
    get() = resources.displayMetrics.heightPixels

//屏幕的密度
inline val Context.density: Float
    get() = resources.displayMetrics.density

//使用内联函数的泛型参数 reified 特性来实现
inline fun <reified T : Activity> Context.startActivity() {
    val intent = Intent(this, T::class.java)
    if (this !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}


inline fun <reified T: Activity> Context.startActivity(
    vararg params: Pair<String?, String?>) {
    val intent = Intent(this, T::class.java)
    params.forEach { intent.putExtra(it.first, it.second) }
    startActivity(intent)
}

/**实例化 Fragment*/
inline fun <reified T : Fragment> Context.newInstanceFragment(args: Bundle?): T {
    val className = T::class.java.name;
    val clazz = FragmentFactory.loadFragmentClass(
        classLoader, className)
    val f = clazz.getConstructor().newInstance()
    if (args != null) {
        args.classLoader = f.javaClass.classLoader
        f.arguments = args
    }
    return f as T
}
