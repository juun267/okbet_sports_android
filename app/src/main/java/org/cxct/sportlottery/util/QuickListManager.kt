package org.cxct.sportlottery.util

object QuickListManager {

    private val selectedOddsId = mutableSetOf<String>()

    fun setQuickSelectedList(list: MutableList<String>?) {
        selectedOddsId.clear()
        if (list.isNullOrEmpty()) {
            return
        }
        selectedOddsId.addAll(list)
    }

    fun containOdd(oddId: String) : Boolean {
        return selectedOddsId.contains(oddId)
    }

    fun getQuickSelectedList(): MutableList<String> {
        return selectedOddsId.toMutableList()
    }
}