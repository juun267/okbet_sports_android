package org.cxct.sportlottery.util

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_web.view.*
import org.cxct.sportlottery.R
import org.w3c.dom.Text

/**
 * Created by pengweiqiang on 16/3/15.
 * 问题：同时显示多个Toast信息提示框，会放在队列中，等前一个Toast关闭后才会显示下一个Toast。
 * 原因：Toast是Android用来显示信息的一种机制，跟Dialog不一样的是没有焦点，过一定的时间会自动消失
 * 解决方案：为了解决解决Toast重复显示问题，每次创建Toast我们先做下判断，如果有Toast显示，直接改变Toast里面的文字即可
 */
object ToastUtil {
    private var mToast: Toast? = null

    /**
     * @param context  上下文对象
     * @param text     提示信息
     * @param duration 显示时间
     */
    fun showToast(context: Context?, text: String?, duration: Int = Toast.LENGTH_LONG, isCenter: Boolean = false) {
        try {
            if (context != null && !text.isNullOrEmpty()) {
                //部分手機系統的Toast在UI上消失後並不會直接 = null，此情況下Toast不會顯示。
                if (mToast != null) mToast!!.cancel()
                mToast = Toast.makeText(context, text, duration)
                if (isCenter) mToast?.setGravity(Gravity.CENTER, 0, 0)
                mToast?.show()
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

    fun showBetResultToast(activity: Activity, msg: String, success: Boolean) {
        val contentView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
        val view = LayoutInflater.from(activity).inflate(R.layout.toast_top_bet_result, contentView, false)
        view.findViewById<TextView>(R.id.tv_message).text = if (success) activity.resources.getString(R.string.bet_info_add_bet_success) else msg

        val color = if (success) ContextCompat.getColor(activity, R.color.green_blue) else ContextCompat.getColor(activity, R.color.red2)
        view.findViewById<RelativeLayout>(R.id.rl_bg).setBackgroundColor(color)

        val height: Int? = activity.resources?.getDimensionPixelOffset(R.dimen.tool_bar_height)

        height?.let {
            val myToast = Toast(activity)
            myToast.duration = Toast.LENGTH_SHORT
            myToast.setGravity(Gravity.FILL_HORIZONTAL or Gravity.TOP, 0, it)
            myToast.view = view
            myToast.show()
        }
    }

}