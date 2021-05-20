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
class CustomAlertDialog(context: Context) : AlertDialog(context) {

    private var mTitle: String? = null
    private var mMessage: String? = null
    private var mSpannedMessage: Spanned? = null
    private var mPositiveText: String? = context.getString(R.string.btn_determine)
    private var mNegativeText: String? = context.getString(R.string.btn_cancel)
    private var mPositiveClickListener: View.OnClickListener = View.OnClickListener { dismiss() }
    private var mNegativeClickListener: View.OnClickListener = View.OnClickListener { dismiss() }
    private var mGravity = Gravity.CENTER
    private var mTextColor = R.color.gray6
    private var isShowDivider: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_custom_alert)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        initView()
    }

    private fun initView() {
        when (mTitle) {
            null -> tv_title.visibility = View.GONE
            else -> tv_title.text = mTitle
        }

        tv_message.gravity = mGravity
        when {
            mSpannedMessage != null -> tv_message.text = mSpannedMessage
            mMessage == null -> sv_block_content.visibility = View.GONE
            else -> tv_message.text = mMessage
        }

        if (mPositiveText == null) {
            btn_positive.visibility = View.GONE
        } else
            btn_positive.text = mPositiveText

        if (mNegativeText == null) {
            btn_negative.visibility = View.GONE
        } else
            btn_negative.text = mNegativeText


        tv_message.setTextColor(ContextCompat.getColor(context, mTextColor))

        divider2.visibility = if (isShowDivider) View.VISIBLE else View.GONE

        btn_positive.setOnClickListener(mPositiveClickListener)
        btn_negative.setOnClickListener(mNegativeClickListener)
    }


    ////
    //以下設定要在 dialog.show() 之前才有效果
    ////
    fun setTitle(title: String?) {
        mTitle = title
    }

    fun setShowDivider(show: Boolean?) {
        isShowDivider = show?:false
    }

    fun setMessage(message: String?) {
        mMessage = message
    }

    fun setTextColor(@ColorRes colorResource: Int) {
        mTextColor = colorResource
    }

    //set .html 語法文字
    fun setMessage(spanned: Spanned) {
        mSpannedMessage = spanned
    }

    fun setGravity(gravity: Int) {
        mGravity = gravity
    }

    /**
     * @param positiveText: Positive 按鈕文字，若給 null 則隱藏按鈕
     */
    fun setPositiveButtonText(positiveText: String?) {
        mPositiveText = positiveText
    }

    /**
     * @param negativeText: Negative 按鈕文字，若給 null 則隱藏按鈕
     */
    fun setNegativeButtonText(negativeText: String?) {
        mNegativeText = negativeText
    }

    fun setPositiveClickListener(positiveClickListener: View.OnClickListener) {
        mPositiveClickListener = positiveClickListener
    }

    fun setNegativeClickListener(negativeClickListener: View.OnClickListener) {
        mNegativeClickListener = negativeClickListener
    }
}