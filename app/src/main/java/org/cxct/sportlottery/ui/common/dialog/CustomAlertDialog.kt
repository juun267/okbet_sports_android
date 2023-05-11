package org.cxct.sportlottery.ui.common.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Spanned
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.android.synthetic.main.dialog_custom_alert.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.ui.base.BaseDialogFragment
import org.cxct.sportlottery.util.DisplayUtil.dp

/**
 * 常用提示對話框
 * 預設按鈕: 取消 / 確定
 *
 * memo:
 * this.setCanceledOnTouchOutside(false) //disable 點擊外部關閉 dialog
 * this.setCancelable(false) //disable 按實體鍵 BACK 關閉 dialog
 */
class CustomAlertDialog(private val mContext: Context? = null) : BaseDialogFragment() {

    private var mTitle: String? = null
    private var mMessage: String? = null
    private var mSpannedMessage: Spanned? = null
    private var mPositiveText: String? = mContext?.getString(R.string.btn_confirm)
    private var mNegativeText: String? = mContext?.getString(R.string.btn_cancel)
    private var mPositiveClickListener: View.OnClickListener = View.OnClickListener { dismiss() }
    private var mNegativeClickListener: View.OnClickListener = View.OnClickListener { dismiss() }
    private var mGravity = Gravity.CENTER
    private var mTextColor = R.color.color_9FADC6_535D76
    private var mNegateTextColor = R.color.color_FFFFFF_414655
    private var isShowDivider: Boolean = false
    private var isShowDividerBottom: Boolean = true
    var dissmisCallback: ((CustomAlertDialog) -> Unit)? = null

    var isShowing = dialog?.isShowing
    var mScrollViewMarginHorizon: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return inflater.inflate(R.layout.dialog_custom_alert, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(view: View) {
        when (mTitle) {
            null -> tv_title.visibility = View.GONE
            else -> tv_title.text = mTitle
        }

        val params = (view.layoutParams as MarginLayoutParams?) ?: MarginLayoutParams(-2, -2)
        val margin = 26.dp
        params.leftMargin = margin
        params.topMargin = margin
        params.rightMargin = margin
        params.bottomMargin = margin
        view.layoutParams = params

        tv_message.gravity = mGravity
        when {
            mSpannedMessage != null -> tv_message.text = mSpannedMessage
            mMessage == null -> sv_block_content.visibility = View.GONE
            else -> tv_message.text = mMessage
        }

        if (mPositiveText == null) {
            btn_positive.visibility = View.GONE
        } else btn_positive.text = mPositiveText

        if (mNegativeText == null) {
            btn_negative.visibility = View.GONE
        } else btn_negative.text = mNegativeText

        if (mPositiveText == null || mNegativeText == null) {
            view_line.visibility = View.GONE
        }

        if (mPositiveText == null && mNegativeText == null) {
            block_bottom_bar.visibility = View.GONE
        }

        val contentParams = sv_block_content.layoutParams as ConstraintLayout.LayoutParams
        if (mScrollViewMarginHorizon != 0) {
            contentParams.leftMargin = mScrollViewMarginHorizon.dp
            contentParams.rightMargin = mScrollViewMarginHorizon.dp
        }

        tv_message.setTextColor(ContextCompat.getColor(context ?: requireContext(), mTextColor))
        btn_negative.setTextColor(
            ContextCompat.getColor(
                context ?: requireContext(), mNegateTextColor
            )
        )
        divider2.visibility = if (isShowDivider) View.VISIBLE else View.GONE
        divider.visibility = if (isShowDividerBottom) View.VISIBLE else View.GONE

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
        isShowDivider = show ?: false
    }

    fun setShowDividerBottom(show: Boolean?) {
        isShowDividerBottom = show ?: true
    }

    fun setMessage(message: String?) {
        mMessage = message
    }

    fun setTextColor(@ColorRes colorResource: Int) {
        mTextColor = colorResource
    }

    fun setNegativeTextColor(@ColorRes colorResource: Int) {
        mNegateTextColor = colorResource
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

    fun setCanceledOnTouchOutside(boolean: Boolean) {
        dialog?.setCanceledOnTouchOutside(boolean)
    }

    private fun getMessageTag(): String {
        if (mSpannedMessage != null) {
            return mSpannedMessage.toString()
        }

        if (TextUtils.isEmpty(mMessage)) {
            return "$mTitle"
        }

        return "$mMessage"
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dissmisCallback?.invoke(this)
        removeDialogTag(mContext, getMessageTag())
    }

    override fun show(manager: FragmentManager, tag: String?) = runWithCatch {
        if (manager.isDestroyed) {
            return@runWithCatch
        }

        if (isAdded) { runWithCatch { dismissAllowingStateLoss() } }
        modifyPrivateField("mDismissed", false)
        modifyPrivateField("mShownByMe", true)
        val ft = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()

        addDialogTag(mContext, getMessageTag())
    }


    companion object {

        val dialogs = mutableMapOf<String, MutableSet<String>>()

        fun checkDialogIsShowing(context: Context, tag: String): Boolean {
            if (context !is LifecycleOwner) {
                return false
            }

            return dialogs[context.toString()]?.contains(tag) ?: false
        }

        private fun removeDialogTag(mContext: Context?, messageTag: String?) {
            if (mContext !is LifecycleOwner || TextUtils.isEmpty(messageTag)) {
                return
            }

            val owner = mContext.toString()
            val tags = dialogs[owner]
            if (tags.isNullOrEmpty()) {
                dialogs.remove(owner)
                return
            }

            tags.remove(messageTag)

            if (tags.isNullOrEmpty()) {
                dialogs.remove(owner)
                return
            }
        }

        private fun addDialogTag(mContext: Context?, messageTag: String?) {
            if (mContext !is LifecycleOwner || TextUtils.isEmpty(messageTag)) {
                return
            }

            val owner = mContext.toString()
            val lifecycle = (mContext as LifecycleOwner).lifecycle
            if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
                dialogs.remove(owner)
                return
            }

            var tags = dialogs[owner]
            if (tags == null) {
                tags = mutableSetOf()
                dialogs[owner] = tags
                lifecycle.addObserver(object : LifecycleEventObserver {
                    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                        if (event.targetState == Lifecycle.State.DESTROYED) {
                            dialogs.remove(owner)
                        }
                    }
                })
            }

            tags.add(messageTag!!)
        }
    }
}