package org.cxct.sportlottery.util

import java.text.DecimalFormat

object TextUtil {
    fun split(str: String): MutableList<String> {
        return str.split(",").toMutableList()
    }

    fun format(any: Any): String {
        val df = DecimalFormat("###,###,###,##0.000")
        return df.format(any)
    }

    fun formatForOdd(any: Any):String{
        val df = DecimalFormat("0.000")
        return df.format(any)
    }

}