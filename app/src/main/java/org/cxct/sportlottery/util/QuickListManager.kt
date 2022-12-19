package org.cxct.sportlottery.util

import android.util.Log
import java.util.*

object QuickListManager {
    private var quickListSelected: MutableList<String>? = null

    fun setQuickSelectedList(list: MutableList<String>?){
        Log.e("For Test", "=====>>> QuickListManager ")
        quickListSelected = list
    }

    fun getQuickSelectedList(): MutableList<String>?{
        return quickListSelected
    }
}