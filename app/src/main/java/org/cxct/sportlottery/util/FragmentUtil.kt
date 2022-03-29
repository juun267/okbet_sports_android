package org.cxct.sportlottery.util


import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.CustomAlertDialog


/**
 * @author kevin
 * @create 2022/3/14
 * @description
 */
fun commonCheckDialog(
    context: Context,
    fm: FragmentManager,
    isError: Boolean,
    isShowDivider: Boolean?,
    title: String?,
    errorMessageSpan: Spanned,
    buttonText: String?,
    positiveClickListener: () -> Unit?
) {
    if (checkDialogIsShowing(fm, errorMessageSpan.toString())) {
        return
    }

    CustomAlertDialog(context).apply {
        if (isError) {
            setTextColor(R.color.colorRed)
        }
        setShowDivider(isShowDivider)
        setTitle(title)
        setMessage(errorMessageSpan)
        setPositiveButtonText(buttonText ?: context.getString(R.string.btn_determine))
        setNegativeButtonText(null)
        setPositiveClickListener {
            positiveClickListener()
            this.dismiss()
        }

        setCanceledOnTouchOutside(false)
        isCancelable = false //不能用系統 BACK 按鈕關閉 dialog
    }.show(fm, errorMessageSpan.toString())
}


fun commonCheckDialog(
    context: Context,
    fm: FragmentManager,
    isError: Boolean,
    isShowDivider: Boolean?,
    title: String?,
    errorMessage: String? = "",
    buttonText: String?,
    positiveClickListener: () -> Unit?
) {
    if (checkDialogIsShowing(fm, errorMessage ?: "")) {
        return
    }

    CustomAlertDialog(context).apply {
        if (isError) {
            setTextColor(R.color.colorRed)
        }
        setShowDivider(isShowDivider)
        setTitle(title)
        setMessage(errorMessage)
        setPositiveButtonText(buttonText ?: context.getString(R.string.btn_determine))
        setNegativeButtonText(null)
        setPositiveClickListener {
            positiveClickListener()
            this.dismiss()
        }

        setCanceledOnTouchOutside(false)
        isCancelable = false //不能用系統 BACK 按鈕關閉 dialog
    }.show(fm, errorMessage)
}


fun phoneNumCheckDialog(context: Context, fm: FragmentManager) {
    val errorMsg = context.getString(R.string.dialog_security_need_phone)
    if (checkDialogIsShowing(fm, errorMsg)) {
        return
    }
    CustomAlertDialog(context).apply {
        setMessage(errorMsg)
        setNegativeButtonText(null)
        setCanceledOnTouchOutside(false)
        isCancelable = false
    }.show(fm, errorMsg)
}


private fun checkDialogIsShowing(fm: FragmentManager, tag: String): Boolean {
    with(fm.findFragmentByTag(tag) as DialogFragment?) {
        return this != null && dialog?.isShowing == true
    }
}