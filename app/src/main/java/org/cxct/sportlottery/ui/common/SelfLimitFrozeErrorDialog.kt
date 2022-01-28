package org.cxct.sportlottery.ui.common

import android.content.Context
import android.os.Bundle
import android.text.Spanned
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.dialog_custom_alert.*
import org.cxct.sportlottery.R

/**
 * 常用提示對話框
 * 預設按鈕: 取消 / 確定
 *
 * memo:
 * this.setCanceledOnTouchOutside(false) //disable 點擊外部關閉 dialog
 * this.setCancelable(false) //disable 按實體鍵 BACK 關閉 dialog
 */
class SelfLimitFrozeErrorDialog(context: Context) : AlertDialog(context) {

    private var mMessage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_self_limit_froze)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        initView()
    }

    private fun initView() {
        when (mMessage) {
            null -> sv_block_content.visibility = View.GONE
            else -> tv_message.text = mMessage
        }

        btn_positive.setOnClickListener{
            dismiss()
        }
    }

    fun setMessage(message: String?) {
        mMessage = message
    }
}