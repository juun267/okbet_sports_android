package org.cxct.sportlottery.util

object QuickListManager {
    private var quickListSelected: MutableList<String>? = null

    fun setQuickSelectedList(list: MutableList<String>?){
        quickListSelected = list
    }

    fun getQuickSelectedList(): MutableList<String>?{
        return quickListSelected
    }
}