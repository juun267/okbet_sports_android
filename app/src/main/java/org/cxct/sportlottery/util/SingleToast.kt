package org.cxct.sportlottery.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.util.DisplayUtil.dp

object SingleToast {

    private var mToast: Toast? = null

    fun getInstance(context: Context): Toast {
        if (mToast == null) {
            mToast = Toast(context)
        }
        return mToast!!
    }

    /**
     * @param context  上下文
     * @param isSuccess  成功还是失败的toast
     * @param toastMassage 消息文本//建议四个字
     * @param duration  1=Toast.LENGTH_LONG 0=Toast.LENGTH_SHORT
     */
    fun showSingleToast(
        context: Context,
        isSuccess: Boolean,
        toastMassage: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        val singleToast = getInstance(context)
        val customView: View = LayoutInflater.from(context).inflate(R.layout.single_toast, null)
        val imageView: ImageView = customView.findViewById(R.id.iv_success_error)
        val text = customView.findViewById<TextView>(R.id.tv_massage)
        if (isSuccess) {
            imageView.setImageResource(R.drawable.icon_toast_success)
        } else {
            imageView.setImageResource(R.drawable.icon_toast_error)
        }
        text.text = toastMassage
        singleToast.view = customView
        singleToast.setGravity(Gravity.CENTER, 0, 0)
        singleToast.duration = duration
        singleToast.show()
    }

    fun showSingleToastNoImage(
        context: Context,
        toastMassage: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        val singleToast = getInstance(context)
        val customView: View = LayoutInflater.from(context).inflate(R.layout.single_toast_bet, null)
        val text = customView.findViewById<TextView>(R.id.tv_massage)

        text.text = toastMassage
        singleToast.view = customView
        singleToast.duration = duration
        singleToast.show()
    }
}