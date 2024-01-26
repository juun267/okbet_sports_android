package org.cxct.sportlottery.ui.base

import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.cxct.sportlottery.common.extentions.runWithCatch

open class BaseDialogFragment: DialogFragment() {

    var onDismissListener: (() -> Unit)? = null
    var onShowListener: (() -> Unit)? = null

//    // 保留实例对象，在activity重建时直接使用保存的实例对象。避免DialogFragment没有无参构造函数时由系统重建引起的崩溃问题
//    protected open val isRetainInstance = true
//
//    init {
//        retainInstance = isRetainInstance
//    }

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

        onShowListener?.invoke()
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }


}