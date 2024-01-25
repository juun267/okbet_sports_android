package org.cxct.sportlottery.common.extentions

import android.text.Spanned
import androidx.fragment.app.FragmentActivity
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.util.commonCheckDialog


private var mTokenPromptDialog: CustomAlertDialog? = null

fun FragmentActivity.showTokenPromptDialog(errorMessage: String, positiveClickListener: () -> Unit?) {
    runWithCatch{
        //防止跳出多個 error dialog
        if (mTokenPromptDialog != null)
            return

        mTokenPromptDialog = CustomAlertDialog().apply {
            setTextColor(R.color.color_E44438_e44438)
            setTitle(getString(R.string.prompt))
            setMessage(errorMessage)
            setPositiveButtonText(getString(R.string.btn_confirm))
            setNegativeButtonText(null)
            setCanceledOnTouchOutside(false)
            isCancelable = false //不能用系統 BACK 按鈕關閉 dialog
            setPositiveClickListener {
                positiveClickListener()
                mTokenPromptDialog?.dismiss()
                mTokenPromptDialog = null
            }
        }
        if (!isDestroyed){
            mTokenPromptDialog?.show(supportFragmentManager, null)
        }
    }
}

fun FragmentActivity.showPromptDialog(
    title: String? = getString(R.string.prompt),
    message: String,
    positiveClickListener: () -> Unit?
) {
    showPromptDialog(title, message, null, positiveClickListener, false)
}

fun FragmentActivity.showPromptDialogNoCancel(
    title: String? = getString(R.string.prompt),
    message: String,
    positiveClickListener: () -> Unit?
) {
    showPromptDialogNoCancel(title, message, null, positiveClickListener, false)
}

fun FragmentActivity.showPromptDialog(
    title: String? = getString(R.string.prompt),
    message: Spanned,
    positiveClickListener: () -> Unit?
) {
    showPromptDialog(title, message, null, positiveClickListener, false)
}

fun FragmentActivity.showPromptDialog(
    title: String? = getString(R.string.prompt),
    message: Spanned,
    isOutsideCancelable: Boolean,
    positiveClickListener: () -> Unit?
) {
    showPromptDialog(
        title,
        message,
        null,
        positiveClickListener,
        false,
        isOutsideCancelable = isOutsideCancelable
    )
}

fun FragmentActivity.showPromptDialog(
    title: String? = getString(R.string.prompt),
    message: String,
    buttonText: String?,
    isShowDivider: Boolean,
    positiveClickListener: () -> Unit?
) {
    showPromptDialog(title, message, buttonText, positiveClickListener, false, isShowDivider)
}

fun FragmentActivity.showErrorPromptDialog(message: String, positiveClickListener: () -> Unit?) {
    showErrorPromptDialog(getString(R.string.prompt), message, positiveClickListener)
}

fun FragmentActivity.showErrorPromptDialog(title: String, message: String, positiveClickListener: () -> Unit?) {
    showPromptDialog(title, message, null, positiveClickListener, true)
}

fun FragmentActivity.showErrorPromptDialog(title: String, message: Spanned, hasCancel: Boolean, positiveClickListener: () -> Unit?) {
    showPromptDialog(title, message, null, positiveClickListener, true,hasCancel)
}

fun FragmentActivity.showPromptDialog(
    title: String?,
    errorMessageSpan: Spanned,
    buttonText: String?,
    positiveClickListener: () -> Unit?,
    isError: Boolean,
    hasCancle: Boolean = true,
    isShowDivider: Boolean? = false,
    isOutsideCancelable: Boolean = false
) {
    commonCheckDialog(
        context = this,
        fm = supportFragmentManager,
        isError = isError,
        isShowDivider = isShowDivider,
        title = title,
        errorMessageSpan = errorMessageSpan,
        buttonText = buttonText,
        positiveClickListener = positiveClickListener,
        negativeText = null,
        isOutsideCancelable = isOutsideCancelable
    )
}

fun FragmentActivity.showPromptDialog(
    title: String?,
    errorMessage: String?,
    buttonText: String?,
    positiveClickListener: () -> Unit?,
    isError: Boolean,
    isShowDivider: Boolean? = false,
    hasCancle: Boolean = false,
) {
    commonCheckDialog(
        context = this,
        fm = supportFragmentManager,
        isError = isError,
        isShowDivider = isShowDivider,
        title = title,
        errorMessage = errorMessage,
        buttonText = buttonText,
        positiveClickListener = positiveClickListener,
        negativeText = if (hasCancle) getString(R.string.btn_cancel) else null,
    )
}

fun FragmentActivity.showPromptDialogNoCancel(
    title: String?,
    errorMessage: String?,
    buttonText: String?,
    positiveClickListener: () -> Unit?,
    isError: Boolean,
    isShowDivider: Boolean? = false
) {
    commonCheckDialog(
        context = this,
        fm = supportFragmentManager,
        isError = isError,
        isShowDivider = isShowDivider,
        title = title,
        errorMessage = errorMessage,
        buttonText = buttonText,
        positiveClickListener = positiveClickListener,
        negativeText = null
    )
}