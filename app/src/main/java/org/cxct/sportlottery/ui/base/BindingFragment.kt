package org.cxct.sportlottery.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import org.cxct.sportlottery.common.extentions.createVBinding

abstract class BindingFragment<VM : BaseViewModel, VB: ViewBinding>: BaseFragment<VM>(null) {

    protected val binding: VB by lazy { createVBinding(layoutInflater, 1) }
    private var _first = true

    fun context() = binding.root.context

    override fun createRootView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (_first) {
            _first = false
            onInitView(view)
        }
        onBindViewStatus(view)
        onInitData()
    }

    @Deprecated("为了避免View复用进行了防重复加载，所以View的初始化工作只应该执行一次，为了与BaseFragment中的进行区分请使用onInitView")
    override fun onBindView(view: View) { }

    // 该方法在整个生命周期中只会执行一次(类似与RecyclerView.Adapter的oonCreateViewHolder)
    protected abstract fun onInitView(view: View)

    /**
     * 与onInitView方法不同在于每次onViewCreated都会执行，对于都Fragment切换时会发生多次onCreateView的情况
     *  该方法可以用于将View设置回初始状态(类似与RecyclerView.Adapter的onBindViewHolder)
      */
    protected open fun onBindViewStatus(view: View) { }

    // 该方法每次onCreateView后都会执行
    protected open fun onInitData() { }

}