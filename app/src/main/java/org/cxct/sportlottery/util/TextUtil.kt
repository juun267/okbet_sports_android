package org.cxct.sportlottery.util

import android.content.Context
import android.icu.text.DecimalFormat
import org.cxct.sportlottery.R
import timber.log.Timber
import java.math.BigDecimal
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

    fun splitSet(str: String?): Set<String> {
        if (str.isNullOrEmpty()) {
            return setOf()
        }
        return str.split(",").toSet()
    }


    fun format(any: Any): String? {
        try {
            var target = any

            if (any !is Number) target = target.toString().toDouble()

            return doNumberFormat(target, "###,###,###,##0.00")
        } catch (e: Exception) {
            Timber.e("$e")
        }
        return null
    }


    fun format2(any: Any): String? {
        try {
            var target = any

            if (any !is Number) target = target.toString().toDouble()

            return doNumberFormat(target, "000,000,000.00")
        } catch (e: Exception) {
            Timber.e("$e")
        }
        return null
    }


    // 金额最多保留2为小数
    fun formatMoney2(any: Any): String {
        try {
            return DecimalFormat("###,###,###,###,###,###,###,###.##").format(any)
        } catch (e: Exception) {
            Timber.e("$e")
        }

        return "0.00"
    }

    /**
     * numberAfterDot 保留小数点后几位
     */
    fun formatMoney(any: Any, numAfterDot: Int = 0): String? {
        try {
            var target = any

            if (any !is Double) target = target.toString().toDouble()
            var numAfterDotBuilder = StringBuilder()
            if (numAfterDot > 0) {
                numAfterDotBuilder.append(".")
                for (index in 1..numAfterDot) {
                    numAfterDotBuilder.append("0")
                }
            }
            return doNumberFormatToDouble(
                target, "###,###,###,##0$numAfterDotBuilder", RoundingMode.DOWN
            )
        } catch (e: Exception) {
            Timber.e("$e")
        }
        return "0.00"
    }

    fun formatInputMoney(any: Any): String {
        return doNumberFormat(any, "0.##") { decimalFormat ->
            decimalFormat.roundingMode = RoundingMode.FLOOR
        }
    }

    fun formatMoney(double: Double): String {
        return doNumberFormat(ArithUtil.toMoneyFormat(double).toDouble(), "###,###,###,##0.00")
    }

    fun formatMoneyFourthDecimal(double: Double): String {
        return doNumberFormat(ArithUtil.toMoneyFormat(double).toDouble(), "###,###,###,##0.00")
    }

    fun formatMoney(int: Int): String {
        return doNumberFormat(
            ArithUtil.toMoneyFormat(int.toDouble()).toDouble(), "###,###,###,##0.00"
        )
    }

    fun formatMoneyNoDecimal(int: Int): String {
        return doNumberFormat(int, "###,###,###,###")
    }
    fun formatMoneyNoDecimal(long: Long): String {
        return doNumberFormat(long, "###,###,###,###,###,###,###,###")
    }
    fun formatMoneyNoDecimal(double: Double): String {
        return doNumberFormat(ArithUtil.toMoneyFormat(double).toDouble(), "###,###,###,##")
    }

    fun formatForOdd(any: Any): String {
        return doNumberFormat(
            any, "###,###,###,##0.00"
        ) { decimalFormat -> decimalFormat.roundingMode = RoundingMode.HALF_UP }
    }

    fun formatBetQuota(any: Any): String {
        return doNumberFormat(any, "###,###,###,###")
    }

    fun formatBetQuotaMoney(any: Any): String {
        return doNumberFormat(any, "###,###,###,###0.00")
    }

    fun formatForVipRebates(any: Any): String {
        return doNumberFormat(any, "#.# %") { decimalFormat ->
            decimalFormat.roundingMode = RoundingMode.HALF_UP
        }
    }

    fun formatForOddPercentage(any: Any): String {
        return doNumberFormat(
            any, "###,###,###,#0.00##%"
        ) { decimalFormat -> decimalFormat.roundingMode = RoundingMode.HALF_UP }
    }

    fun formatForBetHint(any: Any): String {
        return doNumberFormat(any, "###,###,###,###")
    }

    //TODO 應以resource代入, 配合多國語
    fun replaceCByParlay(context: Context, str: String): String {
        return str.replace(" ${context.getString(R.string.conspire)} ", "C")
    }

    fun getParlayShowName(context: Context, parlayType: String?): String? {
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

    /**
     * 手机密文显示后4位
     */
    fun maskPhoneNum(phone: String): String {
        return StringBuffer().append("******")
            .append(phone.substring(phone.length - 4, phone.length)).toString()
    }

    fun compareWithGameKey(type: String, value: String): Boolean {
        return type == value || type.contains(value)
    }

    fun String.strRoundDown2():BigDecimal {
        return toDouble().toBigDecimal().setScale(2, BigDecimal.ROUND_FLOOR)
    }

    fun Double.dRoundDown2():BigDecimal {
        return toBigDecimal().setScale(2, BigDecimal.ROUND_FLOOR)
    }

}