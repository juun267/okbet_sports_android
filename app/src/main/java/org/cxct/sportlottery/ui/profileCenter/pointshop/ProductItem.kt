package org.cxct.sportlottery.ui.profileCenter.pointshop

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.TextUtil
import timber.log.Timber

/**
 * 普通商品項目數據模型
 */
interface ProductItem {
    val name: String //商品名稱
    val sort: Int? //商品排序

    /**
     * 商品類型[Product.type][org.cxct.sportlottery.net.point.data.Product.type]
     */
    val productType: ProductType?
    val valueType: ProductValueType
    val productPrice: Double
    val fundValue: Double? //彩金金額
    val originalPrice: Double?
    val discount: Double? //商品折價百分比(0~100)
    val storage: Int //商品庫存
    val imageUrl: String

    /**
     * 是否折扣中
     */
    val isOnSale: Boolean get() = (discount?:0.0) >0.0

    /**
     * 商品價格呈現
     */
    fun getProductPrice(context: Context): Spannable {
        val productSpannable = SpannableStringBuilder()
        //region 商品價值
        val priceColor = valueType.moneyValueColor
        val priceSign = valueType.moneySign
        val price = priceSign.plus(TextUtil.formatMoney2(productPrice))
        val priceSpannable = SpannableString(price)
        priceSpannable.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(context, priceColor)
            ),
            0,
            price.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        productSpannable.append(priceSpannable)
        //endregion 商品價值

        //region 商品原價(打折前價值)
        if (isOnSale) {
            originalPrice?.let { originalPriceNotNull ->
                if (originalPriceNotNull > 0.0) {
                    val originalPrice = TextUtil.formatMoney2(originalPriceNotNull)
                    val originalPriceSpannable = SpannableString(originalPrice)
                    originalPriceSpannable.setSpan(
                        StrikethroughSpan(),
                        0,
                        originalPrice.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    productSpannable.append(" ").append(originalPriceSpannable)
                }
            }
        }
        //endregion 商品原價(打折前價值)

        return productSpannable
    }

    fun setupProductDiscountView(tvDiscount: TextView?, ivDiscount: ImageView?) {
        if (isOnSale) {
            tvDiscount?.isVisible = true
            ivDiscount?.isVisible = true
            tvDiscount?.text = getProductDiscount()
        } else {
            tvDiscount?.isVisible = false
            ivDiscount?.isVisible = false
        }
    }

    fun getProductDiscount(): String {
        return if (isOnSale) {
            "-${discount?.toInt()?:0}%"
        } else {
            ""
        }
    }

    //region 商品圖
    fun setupProductImage(tvFundValue: TextView?, ivProduct: ImageView?) {
        when (productType) {
            ProductType.ITEM -> {
                tvFundValue?.isVisible = false
                ivProduct?.load(imageUrl)
            }

            ProductType.FUND -> {
                ivProduct?.setImageResource(R.drawable.img_shop_item_fund)
                tvFundValue?.isVisible = true
                tvFundValue?.text =
                    TextUtil.formatMoney2(fundValue ?: 0) //TODO 彩金數值待後端提供
            }

            null -> {
                Timber.d("[PointShop] 商品圖設置失敗, 沒有配置【${productType}】分類邏輯")
            }
        }
    }
    //endregion 商品圖
}