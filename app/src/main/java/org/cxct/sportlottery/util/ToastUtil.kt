package org.cxct.sportlottery.util

import android.content.Context
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.StringRes
import com.luck.picture.lib.utils.ToastUtils
import java.lang.ref.WeakReference

/**
 * Created by pengweiqiang on 16/3/15.
 * 问题：同时显示多个Toast信息提示框，会放在队列中，等前一个Toast关闭后才会显示下一个Toast。
 * 原因：Toast是Android用来显示信息的一种机制，跟Dialog不一样的是没有焦点，过一定的时间会自动消失
 * 解决方案：为了解决解决Toast重复显示问题，每次创建Toast我们先做下判断，如果有Toast显示，直接改变Toast里面的文字即可
 */
object ToastUtil {

    private var mToast: WeakReference<Toast>? = null

    /**
     * @param context  上下文对象
     * @param text     提示信息
     * @param duration 显示时间
     */
    fun showToast(context: Context?, text: String?, duration: Int = Toast.LENGTH_LONG, isCenter: Boolean = false) {
        try {
            if (context != null && !text.isNullOrEmpty()) {
                //部分手機系統的Toast在UI上消失後並不會直接 = null，此情況下Toast不會顯示。
                mToast?.get()?.cancel()
                val toast = Toast.makeText(context, text, duration)
                mToast = WeakReference(toast)
                if (isCenter) toast.setGravity(Gravity.CENTER, 0, 0)
                toast?.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showToast(context: Context?, @StringRes resId: Int, duration: Int = Toast.LENGTH_LONG, isCenter: Boolean = false) {
        val text = context?.resources?.getString(resId)
        showToast(context, text, duration, isCenter)
    }

    fun showToastInCenter(context: Context?, text: String?, duration: Int = Toast.LENGTH_LONG) {
        showToast(context, text, duration, true)
    }

    fun showToastInCenter(context: Context?, @StringRes resId: Int, duration: Int = Toast.LENGTH_LONG) {
        val text = context?.resources?.getString(resId)
        showToastInCenter(context, text, duration)
    }

    fun showToast(context: Context, @StringRes resId: Int) {
        ToastUtils.showToast(context, context.getString(resId))
    }

}