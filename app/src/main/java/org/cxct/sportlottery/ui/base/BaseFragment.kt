package org.cxct.sportlottery.ui.base

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.cxct.sportlottery.R

open class BaseFragment : Fragment() {

    /*弹出加载界面*/
    open fun loading() {
        loading(null)
    }

    open fun loading(message: String?) {
        if (activity is BaseActivity)
            (activity as BaseActivity).loading(message)
    }

    /*关闭加载界面*/
    open fun hideLoading() {
        if (activity is BaseActivity)
            (activity as BaseActivity).hideLoading()
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

}
