package org.cxct.sportlottery.ui.base

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.cxct.sportlottery.R
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.reflect.KClass

open class BaseBottomSheetFragment <T : BaseViewModel>(clazz: KClass<T>) : BottomSheetDialogFragment() {

    val viewModel: T by sharedViewModel(clazz = clazz)

    /*弹出加载界面*/
    fun loading() {
        loading(null)
    }

    fun loading(message: String?) {
        if (activity is BaseActivity<*>)
            (activity as BaseActivity<*>).loading(message)
    }

    /*关闭加载界面*/
    fun hideLoading() {
        if (activity is BaseActivity<*>)
            (activity as BaseActivity<*>).hideLoading()
    }

    fun hideKeyboard() {
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