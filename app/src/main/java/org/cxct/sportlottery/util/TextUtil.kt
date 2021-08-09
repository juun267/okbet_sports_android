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

    fun formatInputMoney(any: Any): String {
        val df = DecimalFormat("0.###")
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
        val df = DecimalFormat("###,###,###,##0.00")
        return df.format(any)
    }

    fun formatBetQuota(any: Any): String {
        val df = DecimalFormat("###,###,###,###")
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

    //TODO 應以resource代入, 配合多國語
    fun replaceParlayByC(str: String): String {
        return str.replace("C", "串")
    }

    fun replaceCByParlay(str: String): String {
        return str.replace("串", "C")
    }

    fun maskFullName(fullName: String): String {
        val stringBuffer = StringBuffer().append(fullName[0])
        for (i in 0 until fullName.length - 1) stringBuffer.append('*')
        return stringBuffer.toString()
    }

    fun maskUserName(userName: String): String {
        return StringBuffer().append(userName.substring(0, 2)).append("***").append(userName.substring(userName.length - 2, userName.length)).toString()
    }

    fun compareWithGameKey(type: String, value: String): Boolean {
        return type == value || type.contains(value)
    }

}