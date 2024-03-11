package org.cxct.sportlottery.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.createVBinding
import org.cxct.sportlottery.common.extentions.getKClass
import org.cxct.sportlottery.util.showAllowingStateLoss
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.ext.android.getViewModel
import kotlin.reflect.KClass

abstract class BaseDialog<VM : BaseViewModel,VB : ViewBinding>(private val clazz: KClass<VM>? = null) : BaseDialogFragment() {

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
    private var _first = true
    init {
        setDefaulStyle()
    }
    protected open fun setDefaulStyle() {
        setStyle(STYLE_NO_TITLE, R.style.CustomDialogStyle)
    }

    protected fun setStyle(style: Int) {
        setStyle(STYLE_NO_TITLE, style)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (_first) {
            _first = false
            onInitView()
        }
        onBindViewStatus(view)
    }

    open fun show(manager: FragmentManager){
        showAllowingStateLoss(manager,this.javaClass.simpleName)
    }
    abstract fun onInitView()
    /**
     * 与onInitView方法不同在于每次onViewCreated都会执行，对于都Fragment切换时会发生多次onCreateView的情况
     *  该方法可以用于将View设置回初始状态(类似与RecyclerView.Adapter的onBindViewHolder)
     */
    protected open fun onBindViewStatus(view: View) { }
}
