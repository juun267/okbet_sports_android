package org.cxct.sportlottery.view.dialog.queue

import androidx.fragment.app.FragmentManager
import org.cxct.sportlottery.ui.base.BaseDialogFragment

abstract class BasePriorityDialog<T : BaseDialogFragment>: PriorityDialog {

    open fun getTag(): String? = null
    abstract fun createDialog(): BaseDialogFragment
    abstract fun getFragmentManager(): FragmentManager

    private var dialog: BaseDialogFragment? = null
    private var dismissCallback: (() -> Unit)? = null
    private var showCallback: (() -> Unit)? = null

    override fun showDialog() {
        if (dialog == null) {
            dialog = createDialog()
        }

        dialog!!.let {
            it.onShowListener = showCallback
            it.onDismissListener = dismissCallback
            it.show(getFragmentManager(), tag = getTag())
        }
    }

    override fun dismissDialog() {
        dialog?.let {
            it.dismiss()
            it.onShowListener = null
            it.onDismissListener = null
        }
        dialog = null
        showCallback = null
        dismissCallback = null
    }

    override fun observerDismiss(onDismiss: () -> Unit) {
        dismissCallback = onDismiss
        dialog?.onDismissListener = onDismiss
    }

    override fun observerShow(onShow: () -> Unit) {
        showCallback = onShow
        dialog?.onShowListener = onShow
    }


}