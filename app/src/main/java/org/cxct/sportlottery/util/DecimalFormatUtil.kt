package org.cxct.sportlottery.util

import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

open class DecimalFormatUtil {
    /**
     *
     * 2021/10/28
     * @since 使用DecimalFormat處理數字時會帶入Locale的設定, 越南數字小數點及千分位會顛倒, 在format前需先設定Locale為美國.
     * @param number 需透過DecimalFormat轉換的值
     * @param pattern DecimalFormat轉換的格式
     * @param applyFun 對DecimalFormat的設定
     */
    protected fun doNumberFormat(number: Any, pattern: String, applyFun: ((DecimalFormat) -> Unit)? = null): String {
        Locale.setDefault(Locale.US)
        val df = DecimalFormat(pattern).apply { applyFun?.invoke(this) }
        df.roundingMode = RoundingMode.FLOOR //不进行四舍五入
        return df.format(number)
    }

    fun doNumberFormatToDouble(number: Any, pattern: String, format: RoundingMode? = null): String {
        Locale.setDefault(Locale.US)
        val df = DecimalFormat(pattern)
        format?.let { df.roundingMode = it }
        return df.format(number)
    }
}
