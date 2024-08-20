package org.cxct.sportlottery.view.dialog.queue

interface PriorityDialog: Comparable<PriorityDialog> {

    // 在队列里根据ID去重(为空不做是否重复判断)
    fun getId(): String? = null

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