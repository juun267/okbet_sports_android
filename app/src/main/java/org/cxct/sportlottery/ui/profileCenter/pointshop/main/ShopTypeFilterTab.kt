package org.cxct.sportlottery.ui.profileCenter.pointshop.main

import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.profileCenter.pointshop.ShopTypeFilterType

/**
 *  ShopTypeFilterTabImpl 介面定義了商店類型篩選器選項卡的實現。
 *  所有實現此介面的類別都必須提供以下屬性：
 *  @property tabType 選項卡的類型，使用 [ShopTypeFilterType] 枚舉表示。
 *  @property isSelected 選項卡是否被選中。
 *
 *  TODO 以sealed class取代interface會更好
 */
interface ShopTypeFilterTabImpl {
    val tabType: ShopTypeFilterType
    var isSelected: Boolean

    fun setupTextColor(textView: TextView) {
        textView.setTextColor(
            ContextCompat.getColor(
                textView.context,
                if (isSelected) R.color.color_025BE8 else R.color.color_6D7693
            )
        )
    }
}

data class ShopSortedNormalTab(
    override val tabType: ShopTypeFilterType,
    override var isSelected: Boolean = false
) :
    ShopTypeFilterTabImpl

data class ShopSortedPricesTab(
    override val tabType: ShopTypeFilterType,
    override var isSelected: Boolean = false
) :
    ShopTypeFilterTabImpl {
    /**
     * 是否升冪排序
     */
    var isAscending: Boolean = true

    fun setupSortedIcon(imageView: ImageView) {
        val imageResource = when {
            isSelected -> if (isAscending) {
                R.drawable.ic_shop_prices_ascending
            } else {
                R.drawable.ic_shop_prices_dscending
            }

            else -> R.drawable.ic_shop_prices_normal
        }
        imageView.setImageResource(imageResource)
    }
}