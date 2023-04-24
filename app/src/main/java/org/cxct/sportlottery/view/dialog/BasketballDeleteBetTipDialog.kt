package org.cxct.sportlottery.view.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.dialog_basketball_delete_bet_tip.*
import kotlinx.android.synthetic.main.dialog_custom_alert.btn_negative
import kotlinx.android.synthetic.main.dialog_custom_alert.btn_positive
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.util.DisplayUtil.dp

/**
 * 常用提示對話框
 * 預設按鈕: 取消 / 確定
 *
 * memo:
 * this.setCanceledOnTouchOutside(false) //disable 點擊外部關閉 dialog
 * this.setCancelable(false) //disable 按實體鍵 BACK 關閉 dialog
 */
class BasketballDeleteBetTipDialog(private val mContext: Context? = null) : DialogFragment() {


    private var mPositiveClickListener: View.OnClickListener = View.OnClickListener { dismiss() }
    private var mNegativeClickListener: View.OnClickListener = View.OnClickListener { dismiss() }
    var dissmisCallback: ((BasketballDeleteBetTipDialog) -> Unit)? = null

    var isShowing = dialog?.isShowing

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return inflater.inflate(R.layout.dialog_basketball_delete_bet_tip, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(view: View) {

        val params = (view.layoutParams as MarginLayoutParams?) ?: MarginLayoutParams(-2, -2)
        val margin = 26.dp
        params.leftMargin = margin
        params.topMargin = margin
        params.rightMargin = margin
        params.bottomMargin = margin
        view.layoutParams = params

        btn_positive.setOnClickListener(mPositiveClickListener)
        btn_negative.setOnClickListener(mNegativeClickListener)
        context?.let {
            Glide.with(it).load(R.drawable.basketball_delete_bet_tip).into(iv_gif_basket)
        }

    }

    ////
    //以下設定要在 dialog.show() 之前才有效果
    ////

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
        return javaClass.simpleName
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dissmisCallback?.invoke(this)
        removeDialogTag(mContext, getMessageTag())
    }

    override fun show(manager: FragmentManager, tag: String?) = runWithCatch {

        runWithCatch {
            if (isAdded) {
                dismissAllowingStateLoss()
            }
        }
        modifyPrivateField("mDismissed", false)
        modifyPrivateField("mShownByMe", true)
        val ft = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()

        addDialogTag(mContext, getMessageTag())
    }

    private fun modifyPrivateField(fieldName: String, newValue: Any) {
        val fieldDismissed = DialogFragment::class.java.getDeclaredField(fieldName)
        fieldDismissed.isAccessible = true
        fieldDismissed.set(this, newValue)
    }

    override fun dismiss() = runWithCatch {
        if (isAdded) {
            super.dismiss()
        }
    }

    override fun dismissAllowingStateLoss() = runWithCatch {
        if (isAdded) {
            super.dismissAllowingStateLoss()
        }
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