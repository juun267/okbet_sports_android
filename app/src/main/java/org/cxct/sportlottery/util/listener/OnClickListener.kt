package org.cxct.sportlottery.util.listener

interface OnClickListener {
    fun onItemClick(position: Int) {
    }
    fun onItemClick(item: String) {
    }
    fun onItemClick(position: Int, item: String) {
    }
    fun onItemClick(item: String, name: String, position: Int) {
    }
    fun onItemLongClick(id: Int) {
    }
    fun onReload() {
    }
}