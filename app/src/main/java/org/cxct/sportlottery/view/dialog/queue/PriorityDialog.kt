package org.cxct.sportlottery.view.dialog.queue

interface PriorityDialog: Comparable<PriorityDialog> {

    fun showDialog()

    fun dismissDialog()

    // 显示的优先级, 树值越高越优先显示
    fun priority(): Int

    fun observerShow(onShow: () -> Unit)
    fun observerDismiss(onDismiss: () -> Unit)

    override fun compareTo(other: PriorityDialog): Int {
        return if (priority() > other.priority()) {
            -1
        } else if (priority() < other.priority()) {
            1
        } else {
            0
        }
    }
}