package org.cxct.sportlottery.util

import android.content.Context
import org.cxct.sportlottery.R
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
        return doNumberFormat(any, "###,###,###,##0.0000")
    }

    fun formatInputMoney(any: Any): String {
        return doNumberFormat(any, "0.###")
    }

    fun formatMoney(double: Double): String {
        return doNumberFormat(ArithUtil.toMoneyFormat(double).toDouble(), "###,###,###,##0.0000")
    }

    fun formatMoney(int: Int): String {
        return doNumberFormat(ArithUtil.toMoneyFormat(int.toDouble()).toDouble(), "###,###,###,##0.000")
    }

    fun formatMoneyNoDecimal(int: Int): String {
        return doNumberFormat(int, "###,###,###,##0")
    }

    fun formatMoneyNoDecimal(double: Double): String {
        return doNumberFormat(ArithUtil.toMoneyFormat(double).toDouble(), "###,###,###,##0")
    }

    fun formatForOdd(any: Any): String {
        return doNumberFormat(any, "###,###,###,##0.00") { decimalFormat -> decimalFormat.roundingMode = RoundingMode.HALF_UP }
    }

    fun formatBetQuota(any: Any): String {
        return doNumberFormat(any, "###,###,###,###")
    }

    fun formatBetQuotaMoney(any: Any): String {
        return doNumberFormat(any, "###,###,###,###0.000")
    }

    fun formatForVipRebates(any: Any): String {
        return doNumberFormat(any, "#.# %") { decimalFormat -> decimalFormat.roundingMode = RoundingMode.HALF_UP }
    }

    fun formatForOddPercentage(any: Any): String {
        return doNumberFormat(any, "#0.00 %")
    }

    fun formatForBetHint(any: Any): String {
        return doNumberFormat(any, "###,###,###,###")
    }

    //TODO 應以resource代入, 配合多國語
    fun replaceCByParlay(context:Context, str: String): String {
        return str.replace(" ${context.getString(R.string.conspire)} ", "C")
    }

    fun getParlayShowName(context:Context, parlayType: String?): String? {
        return parlayType?.replace("C", " ${context.getString(R.string.conspire)} ")
    }

    fun maskFullName(fullName: String): String {
        val stringBuffer = StringBuffer().append(fullName[0])
        for (i in 0 until fullName.length - 1) stringBuffer.append('*')
        return stringBuffer.toString()
    }

    fun maskUserName(userName: String): String {
        return StringBuffer().append(userName.substring(0, 2)).append("***")
            .append(userName.substring(userName.length - 2, userName.length)).toString()
    }

    fun compareWithGameKey(type: String, value: String): Boolean {
        return type == value || type.contains(value)
    }

}