package org.cxct.sportlottery.ui.profileCenter.pointshop

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R

interface LimitedProductItem {
    val startDate: Long?
    val endDate: Long?

    /**
     * 是否開始搶購
     */
    val isStarted: Boolean
        get() = startDate?.let {
            it < System.currentTimeMillis()
        } ?: false

    val isFinished: Boolean
        get() = endDate?.let {
            it < System.currentTimeMillis()
        } ?: true

    /**
     * 商品分頁 (當前index / 商品總數)
     */
    fun getProductIndicator(context: Context, productNumber: Int, productTotalNum: Int): Spannable {
        val productIndexString = productNumber.toString()
        val productIndexSpannable = SpannableString(productIndexString)
        productIndexSpannable.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(context, R.color.color_FF4343)
            ),
            0,
            productIndexString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return SpannableStringBuilder(productIndexSpannable).append("/$productTotalNum")
    }
}