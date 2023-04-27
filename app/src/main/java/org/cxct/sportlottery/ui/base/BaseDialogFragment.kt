package org.cxct.sportlottery.ui.base

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.cxct.sportlottery.common.extentions.runWithCatch

open class BaseDialogFragment: DialogFragment() {

    override fun show(manager: FragmentManager, tag: String?) = runWithCatch {
        if (manager.isDestroyed) {
            return@runWithCatch
        }

        if (isAdded) { runWithCatch { dismissAllowingStateLoss() } }

        modifyPrivateField("mDismissed", false)
        modifyPrivateField("mShownByMe", true)
        val ft = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }

    protected fun modifyPrivateField(fieldName: String, newValue: Any) {
        val fieldDismissed = DialogFragment::class.java.getDeclaredField(fieldName)
        fieldDismissed.isAccessible = true
        fieldDismissed.set(this, newValue)
    }

    override fun dismiss() = runWithCatch {
        if (isAdded) { super.dismiss() }
    }

    override fun dismissAllowingStateLoss() = runWithCatch {
        if (isAdded) { super.dismissAllowingStateLoss() }
    }

}