package org.cxct.sportlottery.ui.base

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.net.flow.IUiView
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.ext.android.getViewModel
import kotlin.reflect.KClass

@SuppressLint("InflateParams")
// 不需要传入参数了，通过反射获取类型
abstract class BaseFragment<VM : BaseViewModel, VB: ViewBinding>(private val clazz: KClass<VM>? = null): VisibilityFragment() ,IUiView{

    private lateinit var _viewModel: VM
    val viewModel: VM
    get() {
        if (!::_viewModel.isInitialized) {
            _viewModel = createVM(clazz = clazz ?: getKClass(0) as KClass<VM>)
        }
        return _viewModel
    }

    protected open fun createVM(clazz: KClass<VM>): VM {
        return getViewModel(clazz = clazz, owner = { ViewModelOwner.from(requireActivity(), requireActivity()) })
    }

    protected val binding: VB by lazy { createVBinding(layoutInflater, 1) }

    fun context() = binding.root.context
    private var _first = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _viewModel = createVM(clazz = clazz ?: getKClass(0) as KClass<VM>)
        return  binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (_first) {
            _first = false
            onInitView(view)
        }
        onBindViewStatus(view)
        onInitData()
    }


    // 该方法在整个生命周期中只会执行一次(类似与RecyclerView.Adapter的oonCreateViewHolder)
    protected abstract fun onInitView(view: View)

    /**
     * 与onInitView方法不同在于每次onViewCreated都会执行，对于都Fragment切换时会发生多次onCreateView的情况
     *  该方法可以用于将View设置回初始状态(类似与RecyclerView.Adapter的onBindViewHolder)
     */
    protected open fun onBindViewStatus(view: View) { }

    // 该方法每次onCreateView后都会执行
    protected open fun onInitData() { }


    /*弹出加载界面*/
    open fun loading() {
        loading(null)
    }

    open fun loading(message: String?) {
        requireActivity().loading(message)
    }

    /*关闭加载界面*/
    open fun hideLoading() {
        requireActivity().hideLoading()
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
        requireActivity().showPromptDialogNoCancel(title, message, positiveClickListener)
    }

    fun showPromptDialog(title: String, message: String, positiveClickListener: () -> Unit) {
        requireActivity().showPromptDialog(title, message, positiveClickListener)
    }

    fun showErrorPromptDialog(title: String, message: String, hasCancel:Boolean = true, positiveClickListener: () -> Unit) {
        requireActivity().showErrorPromptDialog(
            title,
            SpannableStringBuilder().append(message),
            hasCancel,
            positiveClickListener
        )
    }

    fun showPromptDialog(
        title: String,
        message: String,
        success: Boolean,
        positiveClickListener: () -> Unit,
    ) {
            if (success) {
                requireActivity().showPromptDialog(
                    title,
                    message,
                    positiveClickListener
                )
            } else {
                requireActivity().showErrorPromptDialog(
                    title,
                    message,
                    positiveClickListener
                )
            }

    }

    fun showPromptDialog(
        title: String,
        message: Spanned,
        success: Boolean,
        positiveClickListener: () -> Unit,
    ) {
        if (success) {
            requireActivity().showPromptDialog(
                title,
                message,
                null,
                positiveClickListener,
                isError = false,
                hasCancle = false)
        } else {
            requireActivity().showErrorPromptDialog(
                title,
                message.toString(),
                positiveClickListener
            )
        }
    }

    fun showPromptDialog(
        title: String,
        message: Spanned,
        success: Boolean,
        isOutsideCancelable: Boolean,
        positiveClickListener: () -> Unit,
    ) {
        if (activity is BaseActivity<*,*>) {
            if (success) {
                (activity as BaseActivity<*,*>).showPromptDialog(
                    title,
                    message,
                    isOutsideCancelable = isOutsideCancelable,
                    positiveClickListener
                )
            } else {
                (activity as BaseActivity<*,*>).showErrorPromptDialog(
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
        requireActivity().showPromptDialog(
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
