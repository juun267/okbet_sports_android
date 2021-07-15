package org.cxct.sportlottery.ui.base

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_bottom_sheet_custom.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.reflect.KClass


@SuppressLint("InflateParams")
open class BaseFragment<T : BaseViewModel>(clazz: KClass<T>) : Fragment() {

    val viewModel: T by sharedViewModel(clazz = clazz)

    private val bottomSheetView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.dialog_bottom_sheet_custom, null)
    }
    protected val bottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(requireContext()) }


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

    protected fun clearFocus() {
        activity?.currentFocus?.clearFocus()
    }

    protected fun modifyFinish() {
        hideKeyboard()
        clearFocus()
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

    fun showBottomSheetDialog(
        title: String?,
        dataList: List<StatusSheetData>,
        defaultData: StatusSheetData,
        itemClickListener: StatusSheetAdapter.ItemCheckedListener,
        isShowSelectAll: Boolean = false,
    ) {
        bottomSheetView.apply {
            sheet_tv_title.text = title

            checkbox_select_all.visibility = if (isShowSelectAll) {
                View.VISIBLE
            } else {
                View.GONE
            }

            sheet_rv_more.adapter =
                StatusSheetAdapter(itemClickListener).apply {
                    this.dataList = dataList
                    this.defaultCheckedCode = defaultData.code
                }

            sheet_tv_close.setOnClickListener {
                bottomSheet.dismiss()
            }
        }
        bottomSheet.setContentView(bottomSheetView)
        bottomSheet.show()
    }

    override fun onDestroy() {
        hideKeyboard()
        super.onDestroy()
    }

    fun onNetworkUnavailable() {
        Toast.makeText(activity, R.string.connect_first, Toast.LENGTH_SHORT).show()
    }

}
