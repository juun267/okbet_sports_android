package org.cxct.sportlottery.ui.base

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.reflect.KClass

@SuppressLint("InflateParams")
open class BaseFragment<T : BaseViewModel>(clazz: KClass<T>) : Fragment() {

    val viewModel: T by sharedViewModel(clazz = clazz)
    var mIsEnabled = true //避免快速連點，所有的 item 一次只能點擊一個


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return createRootView(inflater, container, savedInstanceState)
    }

    open protected fun createRootView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(layoutId(), container, false)
    }

    protected open fun layoutId() = 0
    protected open fun onBindView(view: View) { }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBindView(view)
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
            val inputMethodManager =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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

    fun showPromptDialogNoCancel(title: String, message: String, positiveClickListener: () -> Unit) {
        if (activity is BaseActivity<*>) {
            (activity as BaseActivity<*>).showPromptDialogNoCancel(title, message, positiveClickListener)
        }
    }

    fun showPromptDialog(title: String, message: String, positiveClickListener: () -> Unit) {
        if (activity is BaseActivity<*>) {
            (activity as BaseActivity<*>).showPromptDialog(title, message, positiveClickListener)
        }
    }

    fun showErrorPromptDialog(title: String, message: String, hasCancel:Boolean = true,positiveClickListener: () -> Unit) {
        if (activity is BaseActivity<*>) {
            (activity as BaseActivity<*>).showErrorPromptDialog(
                title,
                SpannableStringBuilder().append(message),
                hasCancel,
                positiveClickListener
            )
        }
    }

    fun showPromptDialog(
        title: String,
        message: String,
        success: Boolean,
        positiveClickListener: () -> Unit,
    ) {
        if (activity is BaseActivity<*>) {
            if (success) {
                (activity as BaseActivity<*>).showPromptDialog(
                    title,
                    message,
                    positiveClickListener
                )
            } else {
                (activity as BaseActivity<*>).showErrorPromptDialog(
                    title,
                    message,
                    positiveClickListener
                )
            }
        }
    }

    fun showPromptDialog(
        title: String,
        message: Spanned,
        success: Boolean,
        positiveClickListener: () -> Unit,
    ) {
        if (activity is BaseActivity<*>) {
            if (success) {
                (activity as BaseActivity<*>).showPromptDialog(
                    title,
                    message,
                    null,
                    positiveClickListener,
                    isError = false,
                    hasCancle = false)
            } else {
                (activity as BaseActivity<*>).showErrorPromptDialog(
                    title,
                    message.toString(),
                    positiveClickListener
                )
            }
        }
    }

    fun showPromptDialog(
        title: String,
        message: Spanned,
        success: Boolean,
        isOutsideCancelable: Boolean,
        positiveClickListener: () -> Unit,
    ) {
        if (activity is BaseActivity<*>) {
            if (success) {
                (activity as BaseActivity<*>).showPromptDialog(
                    title,
                    message,
                    isOutsideCancelable = isOutsideCancelable,
                    positiveClickListener
                )
            } else {
                (activity as BaseActivity<*>).showErrorPromptDialog(
                    title,
                    message.toString(),
                    positiveClickListener
                )
            }
        }
    }

    fun showPromptDialog(
        title: String? = getString(R.string.prompt),
        message: String,
        buttonText: String?,
        isShowDivider: Boolean,
        positiveClickListener: () -> Unit?,
    ) {
        (activity as BaseActivity<*>).showPromptDialog(
            title,
            message,
            buttonText,
            positiveClickListener,
            false,
            isShowDivider
        )
    }

    fun showBottomSheetDialog(
        title: String?,
        dataList: List<StatusSheetData>,
        defaultData: StatusSheetData,
        itemClickListener: StatusSheetAdapter.ItemCheckedListener,
    ) {
        if (activity is BaseActivity<*>) {
            (activity as BaseActivity<*>).showBottomSheetDialog(
                title,
                dataList,
                defaultData,
                itemClickListener
            )
        }
    }

    override fun onDestroy() {
        hideKeyboard()
        super.onDestroy()
    }

    fun onNetworkUnavailable() {
        Toast.makeText(activity, R.string.connect_first, Toast.LENGTH_SHORT).show()
    }

    fun back() {
//        if (activity is GameActivity) {
//            val gameActivity = activity as GameActivity
//            if (gameActivity.isFromPublicity) {
//                gameActivity.finish()
//                gameActivity.isFromPublicity = false
//                return
//            }
//        }

        findNavController().navigateUp()
    }

    fun avoidFastDoubleClick(){
        mIsEnabled = false
        Handler().postDelayed({ mIsEnabled = true }, 300)
    }
}
