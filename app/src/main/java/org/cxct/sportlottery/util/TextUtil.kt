package org.cxct.sportlottery.util

import java.math.RoundingMode

/**
 * 需要使用DecimalFormat轉換格式時, 需配合doNumberFormat()
 * @see DecimalFormatUtil.doNumberFormat
 */
object TextUtil : DecimalFormatUtil() {
    fun split(str: String?): MutableList<String> {
        if (str.isNullOrEmpty()) {
            return mutableListOf()
        }
        return str.split(",").toMutableList()
    }

    fun format(any: Any): String {
        return doNumberFormat(any, "###,###,###,##0.000")
    }

    fun formatInputMoney(any: Any): String {
        return doNumberFormat(any, "0.###")
    }

    fun formatMoney(double: Double): String {
        return doNumberFormat(ArithUtil.toMoneyFormat(double).toDouble(), "###,###,###,##0.000")
    }

    fun formatMoney(int: Int): String {
        return doNumberFormat(ArithUtil.toMoneyFormat(int.toDouble()).toDouble(), "###,###,###,##0.000")
    }

    fun formatMoneyNoDecimal(int: Int): String {
        return doNumberFormat(int, "###,###,###,##0")
    }

    fun formatForOdd(any: Any): String {
        return doNumberFormat(any, "###,###,###,##0.00")
    }

    fun formatBetQuota(any: Any): String {
        return doNumberFormat(any, "###,###,###,###")
    }

    fun formatForVipRebates(any: Any): String {
        return doNumberFormat(any, "#.# %") { decimalFormat -> decimalFormat.roundingMode = RoundingMode.HALF_UP }
    }

    fun formatForBetHint(any: Any): String {
        return doNumberFormat(any, "###,###,###,###")
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