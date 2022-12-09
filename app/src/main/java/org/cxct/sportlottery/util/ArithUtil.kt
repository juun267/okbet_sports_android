package org.cxct.sportlottery.util

import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 需要使用DecimalFormat轉換格式時, 需配合doNumberFormat()
 * @see DecimalFormatUtil.doNumberFormat
 */
object ArithUtil : DecimalFormatUtil() {
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

        val formatPattern = if (scale == 0) {
            "0"
        } else {
            "0.${zeroScale}" // 不足位數 補0
        }

        return doNumberFormat(value ?: 0, formatPattern) { decimalFormat ->
            decimalFormat.roundingMode = roundingMode
            decimalFormat.isGroupingUsed = false
        }
    }

    /**
     * 20201015金額全部統一，無條件捨去、保留小數點後兩位
     * 20210112體育數值金額統一改成小數點後第三位
     */
    fun toMoneyFormat(value: Double?): String {
        return round(value ?: 0.0, 3, RoundingMode.HALF_UP)
    }

    /**
     * 20210220 輸入欄位內提示之金額不顯示小數點
     */
    fun toMoneyFormatForHint(value: Double?): String {
        return round(value ?: 0.0, 0, RoundingMode.HALF_UP)
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
        return round(value ?: 0.0, 2, RoundingMode.HALF_UP)
    }

    /**
     * 20220105 印度盤口無條件進位到小數點第二位
     * */
    fun oddIdfFormat(value: Double?): String {
        return round(value ?: 0.0, 2, RoundingMode.UP)
    }

    /**
     * 提款金額取整數
     */
    fun moneyToLong(value: String?): String {
        return (value?.toDouble()?.toLong() ?: 0).toString()
    }

    /**
     * 提供精確的加法運算
     * @param v1 被加數
     * @param v2 加數
     * @return 兩個參數的和
     */
    fun add(v1: Double, v2: Double): Double{
        val b1 = BigDecimal(v1)
        val b2 = BigDecimal(v2)
        return b1.add(b2).toDouble()
    }

    /**
     * 提供精確的減法運算
     * @param v1 被減數
     * @param v2 減數
     * @return 兩個參數的差
     */
    fun sub(v1: Double, v2: Double): Double {
        val b1 = BigDecimal(v1)
        val b2 = BigDecimal(v2)
        return b1.subtract(b2).toDouble()
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
        val b1 = BigDecimal(v1)
        val b2 = BigDecimal(v2)
        val divisor = if (b2.toDouble() == 0.0) BigDecimal(1.0) else b2
        return b1.divide(divisor, scale, roundMode).toDouble()
    }

    fun div(b1: BigDecimal, b2: BigDecimal, scale: Int, roundMode: RoundingMode? = RoundingMode.HALF_UP): BigDecimal {
        require(scale >= 0) { "保留的小数位数必须大于零" }
        val divisor = if (b2.toDouble() == 0.0) BigDecimal(1.0) else b2
        return b1.divide(divisor, scale, roundMode)
    }

    /**
     * 小數運算
     */
    fun minus(paramsS: Double?, paramsO: Double?): Double {
        val decimalSubject = BigDecimal(paramsS ?: 0.0)
        val decimalObject = BigDecimal(paramsO ?: 0.0)
        return round((decimalSubject - decimalObject).toDouble(), 3, RoundingMode.FLOOR).toDouble()
    }

    /**
     * 提供(相對)精確的除法運算，當發生除不盡的情況時，精確到小數點以後10位，以後的數字四捨五入
     */
    fun div(v1: Double, v2: Double):Double{
        val b1 = BigDecimal(v1)
        val b2 = BigDecimal(v2)
        return b1.divide(b2, BigDecimal.ROUND_HALF_UP).toDouble()
    }

    /**
     * 提供精确的乘法运算。
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    fun mul(v1: Double, v2: Double): Double {
//        val s1 = java.lang.Double.toString(v1)
//        val s2 = java.lang.Double.toString(v2)
        val b1 = BigDecimal(v1)
        val b2 = BigDecimal(v2)
//        val m = b1.multiply(b2)
        return b1.multiply(b2).toDouble()
    }
}