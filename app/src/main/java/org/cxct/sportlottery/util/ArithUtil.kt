package org.cxct.sportlottery.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

object ArithUtil {
    /**
     * @param value 数字
     * @param scale 小数点后保留几位
     * @param roundingMode 格式(四捨五入、無條件捨去...等)
     * @return 計算後的结果
     */
    fun round(value: Double?, scale: Int, roundingMode: RoundingMode): String {
        require(scale >= 0) { "保留的小数位数必须大于零" }
        var zeroScale = ""
        zeroScale.apply {
            for (i in 0 until scale) {
                zeroScale += "0"
            }
        }

        val decimalFormat: DecimalFormat?
        decimalFormat = if (scale == 0) {
            DecimalFormat("0")
        } else {
            DecimalFormat("0.${zeroScale}") // 不足位數 補0
        }

        decimalFormat.roundingMode = roundingMode
        decimalFormat.isGroupingUsed = false

        return decimalFormat.format(value)
    }

    /**
     * 20201015金額全部統一，無條件捨去、保留小數點後兩位
     * 20210112體育數值金額統一改成小數點後第三位
     */
    fun toMoneyFormat(value: Double?): String {
        return round(value ?: 0.0, 3, RoundingMode.HALF_UP)
    }

    /**
     * 20201015賠率、手續費率等，保留小數點後三位
     */
    fun toOddFormat(value: Double?): String {
        val rounded = round(value ?: 0.0, 4, RoundingMode.HALF_UP)
        return round(rounded.toDouble(), 3, RoundingMode.HALF_UP)
    }

    /**
     * 20201124 贈送金額四捨五入到小數點第二位
     * */
    fun toBonusMoneyFormat(value: Double?): String {
        return round(value?:0.0, 2, RoundingMode.HALF_UP)
    }

    /**
     * 提款金額取整數
     */
    fun moneyToLong(value: String?): String{
        return (value?.toDouble()?.toLong() ?: 0 ).toString()
    }


    /**
     * 除法运算
     * 当发生除不尽的情况时，由scale参数指定精度
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    fun div(v1: Double, v2: Double, scale: Int, roundMode: RoundingMode? = RoundingMode.HALF_UP): Double {
        require(scale >= 0) { "保留的小数位数必须大于零" }
        val b1 = BigDecimal(v1.toString())
        val b2 = BigDecimal(v2.toString())
        return b1.divide(b2, scale, roundMode).toDouble()
    }

}