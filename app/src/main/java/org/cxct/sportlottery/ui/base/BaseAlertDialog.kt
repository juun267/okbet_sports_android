package org.cxct.sportlottery.ui.base

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.cxct.sportlottery.common.extentions.runWithCatch

// 用于处理activity销毁后dialog没dismiss跑出的window leak
open class BaseAlertDialog(val activity: AppCompatActivity): AlertDialog(activity),
    LifecycleEventObserver, DialogInterface.OnDismissListener {

    private var dismissListener: DialogInterface.OnDismissListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity.lifecycle.addObserver(this)
        super.setOnDismissListener(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY && isShowing) {
            runWithCatch { dismiss() }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        runWithCatch { activity.lifecycle.removeObserver(this) }
        dismissListener?.onDismiss(dialog)
    }

    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        dismissListener = listener
    }
}