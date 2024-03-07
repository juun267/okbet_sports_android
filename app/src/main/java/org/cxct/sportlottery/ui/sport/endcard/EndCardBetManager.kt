package org.cxct.sportlottery.ui.sport.endcard

object EndCardBetManager {

    private val selectedOddsId = mutableSetOf<String>()

    fun setBetOdds(list: MutableList<String>?) {
        selectedOddsId.clear()
        if (list.isNullOrEmpty()) {
            return
        }
        selectedOddsId.addAll(list)
    }

    fun containOdd(oddId: String) : Boolean {
        return selectedOddsId.contains(oddId)
    }

    fun getBetOdds(): MutableList<String> {
        return selectedOddsId.toMutableList()
    }

    fun addBetOdd(oddId: String) {
        selectedOddsId.add(oddId)
    }

    fun removeBetOdd(oddId: String) {
        selectedOddsId.remove(oddId)
    }

}