package org.cxct.sportlottery.ui.base

import androidx.fragment.app.FragmentManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.showAllowingStateLoss
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.reflect.KClass

open class BaseDialog<T : BaseViewModel>(clazz: KClass<T>) : BaseDialogFragment() {

    val viewModel: T by sharedViewModel(clazz = clazz)

    init {
        setDefaulStyle()
    }

    protected open fun setDefaulStyle() {
        setStyle(STYLE_NO_TITLE, R.style.MyDialogStyle)
    }

    protected fun setStyle(style: Int) {
        setStyle(STYLE_NO_TITLE, style)
    }


    open fun show(manager: FragmentManager){
        showAllowingStateLoss(manager)
    }

}
