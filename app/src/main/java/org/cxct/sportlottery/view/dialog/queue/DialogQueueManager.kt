package org.cxct.sportlottery.view.dialog.queue

import androidx.lifecycle.LifecycleOwner
import org.cxct.sportlottery.common.extentions.doOnDestory
import java.util.PriorityQueue

class DialogQueueManager(lifecycleOwner: LifecycleOwner) {

    init {
        lifecycleOwner.doOnDestory {
            dialogQueue.clear()
            currentDialog?.dismissDialog()
        }
    }

    private val dialogQueue = PriorityQueue<PriorityDialog>()
    private var currentDialog: PriorityDialog? = null

    fun enqueue(dialog: PriorityDialog): DialogQueueManager {
        dialogQueue.forEach {
            if (it.getId() != null && it.getId() == dialog.getId()) {
                return this
            }
        }
        dialogQueue.add(dialog)
        return this
    }

    fun showNext() {
        if (currentDialog != null || dialogQueue.isEmpty()) {
            return
        }

        val dialog = dialogQueue.poll()
        dialog.observerDismiss{ onDismiss(dialog) }
        dialog.showDialog()
        currentDialog = dialog
    }

    private fun onDismiss(dialog: PriorityDialog) {
        if (currentDialog == dialog) {
            currentDialog = null
        }
        showNext()
    }
     fun isShowing(): Boolean{
       return currentDialog!=null
    }
}