package org.cxct.sportlottery.util

import java.math.RoundingMode
import java.text.DecimalFormat

object TextUtil {
    fun split(str: String): MutableList<String> {
        return str.split(",").toMutableList()
    }

    fun format(any: Any): String {
        val df = DecimalFormat("###,###,###,##0.000")
        return df.format(any)
    }

    fun formatMoney(double: Double): String {
        val df = DecimalFormat("###,###,###,##0.000")
        return df.format(ArithUtil.toMoneyFormat(double).toDouble())
    }

    fun formatMoney(int: Int): String {
        val df = DecimalFormat("###,###,###,##0.000")
        return df.format(ArithUtil.toMoneyFormat(int.toDouble()).toDouble())
    }

    fun formatMoneyNoDecimal(int: Int): String {
        val df = DecimalFormat("###,###,###,##0")
        return df.format(int)
    }

    fun formatForOdd(any: Any): String {
        val df = DecimalFormat("###,###,###,##0.000")
        return df.format(any)
    }

    fun formatForVipRebates(any: Any): String {
        val df = DecimalFormat("#.# %").apply { roundingMode = RoundingMode.HALF_UP }
        return df.format(any)
    }

    fun formatForBetHint(any: Any): String {
        val df = DecimalFormat("###,###,###,###")
        return df.format(any)
    }

    fun replaceParlayByC(str: String): String {
        return str.replace("C", "串")
    }

}