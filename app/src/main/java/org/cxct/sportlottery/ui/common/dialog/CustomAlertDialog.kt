package org.cxct.sportlottery.ui.common.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Spanned
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.databinding.DialogCustomAlertBinding
import org.cxct.sportlottery.ui.base.BaseDialogFragment

/**
 * 常用提示對話框
 * 預設按鈕: 取消 / 確定
 *
 * memo:
 * this.setCanceledOnTouchOutside(false) //disable 點擊外部關閉 dialog
 * this.setCancelable(false) //disable 按實體鍵 BACK 關閉 dialog
 */
class CustomAlertDialog : BaseDialogFragment() {

    var isError = false
    private var mTitle: String? = null
    private var mMessage: String? = null
    private var mSpannedMessage: Spanned? = null
    private var mPositiveText: String? = MultiLanguagesApplication.appContext.getString(R.string.btn_confirm)
    private var mNegativeText: String? = null
    private var mPositiveClickListener: View.OnClickListener = View.OnClickListener { dismiss() }
    private var mNegativeClickListener: View.OnClickListener = View.OnClickListener { dismiss() }
    private var mTextColor = R.color.color_9FADC6_535D76
    private var mNegateTextColor = R.color.color_FFFFFF_414655
    var dissmisCallback: ((CustomAlertDialog) -> Unit)? = null

    var isShowing = dialog?.isShowing

    private lateinit var binding: DialogCustomAlertBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = DialogCustomAlertBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView(view)
    }

    private fun initView(view: View) = binding.run {
        tvTitle.text = mTitle
        tvTitle.isVisible = !mTitle.isEmptyStr()
        ivStatus.setImageResource(if (isError) R.drawable.img_dialog_negative else R.drawable.img_dialog_positive)

        (blockBottomBar.parent as View).setPadding(0, 0, 0, 0)

        if (mSpannedMessage != null) {
            tvMessage.text = mSpannedMessage
        } else {
            tvMessage.text = mMessage
        }

        if (mPositiveText == null) {
            btnPositive.visibility = View.GONE
        } else {
            btnPositive.text = mPositiveText
        }

        if (mNegativeText == null) {
            btnNegative.visibility = View.GONE
        } else {
            btnNegative.text = mNegativeText
        }


        if (mPositiveText == null && mNegativeText == null) {
            blockBottomBar.visibility = View.GONE
        }

//        tvMessage.setTextColor(ContextCompat.getColor(tvMessage.context, mTextColor))
        tvMessage.setTextColor(ContextCompat.getColor(tvMessage.context, R.color.color_9FADC6_535D76))
        btnNegative.setTextColor(ContextCompat.getColor(btnNegative.context, mNegateTextColor))

        btnPositive.setOnClickListener(mPositiveClickListener)
        btnNegative.setOnClickListener(mNegativeClickListener)
    }

    fun setTitle(title: String?) {
        mTitle = title
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
        removeDialogTag(activity, getMessageTag())
    }

    override fun show(manager: FragmentManager, tag: String?) = runWithCatch {
        if (manager.isDestroyed) {
            return@runWithCatch
        }

        super.show(manager, tag)
        addDialogTag(activity, getMessageTag())
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