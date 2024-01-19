package org.cxct.sportlottery.ui.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import org.cxct.sportlottery.common.extentions.createVBinding

abstract class BindingActivity<VM : BaseViewModel, VB : ViewBinding> : BaseActivity<VM>() {

    protected val binding: VB by lazy { createVBinding(layoutInflater, 1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentLayout()
        onInitView()
        onInitData()
    }

    protected open fun setContentLayout() = setContentView(binding.root)

    protected abstract fun onInitView()

    protected open fun onInitData() { }
}