package org.cxct.sportlottery.ui.base

import android.content.Context
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import org.cxct.sportlottery.R
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.reflect.KClass

open class BaseDialog<T : BaseViewModel>(clazz: KClass<T>) : DialogFragment() {

    val viewModel: T by sharedViewModel(clazz = clazz)

    init {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) //hide keyboard
        setStyle(STYLE_NO_TITLE, R.style.MyDialogStyle)
    }

    protected fun setStyle (style: Int) {
        setStyle(STYLE_NO_TITLE, style)
    }

    /*弹出加载界面*/
    open fun loading() {
        loading(null)
    }

    open fun loading(message: String?) {
        if (activity is BaseActivity<*>)
            (activity as BaseActivity<*>).loading(message)
    }

    /*关闭加载界面*/
    open fun hideLoading() {
        if (activity is BaseActivity<*>)
            (activity as BaseActivity<*>).hideLoading()
    }

    protected fun hideKeyboard() {
        try {
            //*隱藏軟鍵盤
            val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val focusedView = activity?.currentFocus
            if (inputMethodManager.isActive && focusedView != null) {
                inputMethodManager.hideSoftInputFromWindow(focusedView.windowToken, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun onNetworkUnavailable() {
        Toast.makeText(activity, R.string.connect_first, Toast.LENGTH_SHORT).show()
    }

    fun showPromptDialog(title: String, message: String, positiveClickListener: () -> Unit) {
        if (activity is BaseActivity<*>) {
            (activity as BaseActivity<*>).showPromptDialog(title, message, positiveClickListener)
        }
    }

    fun showErrorPromptDialog(title: String, message: String, positiveClickListener: () -> Unit) {
        if (activity is BaseActivity<*>) {
            (activity as BaseActivity<*>).showErrorPromptDialog(title, message, positiveClickListener)
        }
    }

    fun showPromptDialog(title: String, message: String, success: Boolean, positiveClickListener: () -> Unit) {
        if (activity is BaseActivity<*>) {
            if (success) {
                (activity as BaseActivity<*>).showPromptDialog(title, message, positiveClickListener)
            } else {
                (activity as BaseActivity<*>).showErrorPromptDialog(title, message, positiveClickListener)
            }
        }
    }

}
