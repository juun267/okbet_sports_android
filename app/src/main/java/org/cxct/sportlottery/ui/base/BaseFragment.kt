package org.cxct.sportlottery.ui.base

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.net.flow.IUiView
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.ext.android.getViewModel
import kotlin.reflect.KClass

@SuppressLint("InflateParams")
// 不需要传入参数了，通过反射获取类型
open class BaseFragment<T : BaseViewModel>(private val clazz: KClass<T>? = null) : VisibilityFragment() ,IUiView{

    private lateinit var _viewModel: T
    val viewModel: T
    get() {
        if (!::_viewModel.isInitialized) {
            _viewModel = createVM(clazz = clazz ?: getKClass(0) as KClass<T>)
        }
        return _viewModel
    }


    protected open fun createVM(clazz: KClass<T>): T {
        return getViewModel(clazz = clazz, owner = { ViewModelOwner.from(requireActivity(), requireActivity()) })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            Log.e("For Test", "======>>> BaseFragment ${this::class.java.name}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _viewModel = createVM(clazz = clazz ?: getKClass(0) as KClass<T>)
        return createRootView(inflater, container, savedInstanceState)
    }

    protected open fun createRootView(
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
        (activity as? BaseActivity<*>)?.loading(message)
    }

    /*关闭加载界面*/
    open fun hideLoading() {
        (activity as? BaseActivity<*>)?.hideLoading()
    }

    private var progressDialog: ProgressDialog? = null

    override fun showLoading() {
        if (progressDialog == null)
            progressDialog = ProgressDialog(requireActivity())
        progressDialog?.show()
    }

    override fun dismissLoading() {
        progressDialog?.takeIf { it.isShowing }?.dismiss()
    }

    protected fun clearFocus() {
        activity?.currentFocus?.clearFocus()
    }

    protected fun modifyFinish() {
        requireActivity().hideSoftKeyboard()
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

    fun showErrorPromptDialog(title: String, message: String, hasCancel:Boolean = true, positiveClickListener: () -> Unit) {
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

    override fun onDestroy() {
       requireActivity().hideSoftKeyboard()
        super.onDestroy()
    }

    fun onNetworkUnavailable() {
        Toast.makeText(activity, R.string.connect_first, Toast.LENGTH_SHORT).show()
    }

    fun back() {
        findNavController().navigateUp()
    }

}
