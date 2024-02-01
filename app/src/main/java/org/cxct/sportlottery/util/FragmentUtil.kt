package org.cxct.sportlottery.util


import android.content.Context
import android.text.Spanned
import androidx.fragment.app.FragmentManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog


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
    negativeText: String?,
    positiveClickListener: () -> Unit?,
    isOutsideCancelable: Boolean = false,
) {
    if (checkDialogIsShowing(context, errorMessageSpan.toString())) {
        return
    }

    CustomAlertDialog().apply {
        if (isError) {
            setTextColor(R.color.color_E44438_e44438)
        }
        this@apply.isError = isError
        setTitle(title)
        setMessage(errorMessageSpan)
        setPositiveButtonText(buttonText ?: context.getString(R.string.btn_confirm))
        setNegativeButtonText(negativeText)
        setPositiveClickListener {
            positiveClickListener()
            this.dismiss()
        }

        setCanceledOnTouchOutside(isOutsideCancelable)
        isCancelable = isOutsideCancelable //不能用系統 BACK 按鈕關閉 dialog
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
    negativeText: String?,
    positiveClickListener: () -> Unit?,
) {
    if (checkDialogIsShowing(context, errorMessage ?: "")) {
        return
    }

    CustomAlertDialog().apply {
        if (isError) {
            setTextColor(R.color.color_E44438_e44438)
        }
        this@apply.isError = isError
        setTitle(title)
        setMessage(errorMessage)
        setPositiveButtonText(buttonText ?: context.getString(R.string.btn_confirm))
        setNegativeButtonText(if (isError) null else negativeText)
        setPositiveClickListener {
            positiveClickListener()
            this.dismiss()
        }

        setCanceledOnTouchOutside(false)
        isCancelable = false //不能用系統 BACK 按鈕關閉 dialog
    }.show(fm, errorMessage)
}

fun commonTwoButtonDialog(
    context: Context,
    fm: FragmentManager,
    isError: Boolean,
    isShowDivider: Boolean?,
    title: String?,
    errorMessage: String? = "",
    buttonText: String?,
    cancelText: String?,
    positiveClickListener: () -> Unit?,
    negativeClickListener: () -> Unit?
): CustomAlertDialog? {
    var dialog: CustomAlertDialog? = null
    if (checkDialogIsShowing(context, errorMessage ?: "")) {
        return dialog
    }

    CustomAlertDialog().apply {
        dialog = this
        if (isError) {
            setTextColor(R.color.color_E44438_e44438)
        }
        this@apply.isError = isError
        setTitle(title)
        setMessage(errorMessage)
        setPositiveButtonText(buttonText ?: context.getString(R.string.btn_confirm))
        setNegativeButtonText(cancelText ?: context.getString(R.string.btn_cancel))
        setPositiveClickListener {
            positiveClickListener()
            this.dismiss()
        }
        setNegativeClickListener {
            negativeClickListener()
            this.dismiss()
        }

        setCanceledOnTouchOutside(false)
        isCancelable = false //不能用系統 BACK 按鈕關閉 dialog
    }.show(fm, errorMessage)
    return dialog
}


fun phoneNumCheckDialog(context: Context, fm: FragmentManager) {
    val errorMsg = context.getString(R.string.dialog_security_need_phone)
    if (checkDialogIsShowing(context, errorMsg)) {
        return
    }
    CustomAlertDialog().apply {
        setTitle(context.getString(R.string.prompt))
        this@apply.isError = true
        setMessage(errorMsg)
        setNegativeButtonText(null)
        setCanceledOnTouchOutside(false)
        isCancelable = false
    }.show(fm, errorMsg)
}


private fun checkDialogIsShowing(context: Context, tag: String): Boolean {
    return CustomAlertDialog.checkDialogIsShowing(context, tag)
}