package org.cxct.sportlottery.ui.profileCenter.pointshop

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.profileCenter.pointshop.ShopTypeFilterType.CAN_BUY
import org.cxct.sportlottery.ui.profileCenter.pointshop.ShopTypeFilterType.PRICES
import org.cxct.sportlottery.ui.profileCenter.pointshop.ShopTypeFilterType.RECOMMEND

/**
 * 積分商城物品類別
 * ITEM: 商品
 * FUND: 彩金
 */
enum class ProductType(val code: Int) {
    ITEM(2), FUND(1);

    companion object {
        private val typeCodeMap: Map<Int, ProductType> by lazy {
            ProductType.values().associateBy { it.code }
        }

        fun toEnum(typeCode: Int?): ProductType? {
            typeCode?.let {
                return typeCodeMap[it]
            } ?: return null
        }
    }
}

/**
 * 積分商城兌換物品所需價格類別
 * CASH: 現金
 * POINT: 積分
 */
enum class ProductValueType {
    CASH, POINT;

    @get:ColorRes
    val moneyValueColor: Int by lazy {
        when (this) {
            CASH -> R.color.color_FF6533
            POINT -> R.color.color_764FF5
        }
    }

    /**
     * 價值符號
     * 資金: 幣種, 積分: 無符號
     */
    val moneySign: String by lazy {
        when (this) {
            CASH -> showCurrencySign ?: ""
            POINT -> ""
        }
    }

    @get:DrawableRes
    val moneyImage: Int by lazy {
        when (this) {
            CASH -> R.drawable.ic_task_value_cash
            POINT -> R.drawable.ic_task_value_point
        }
    }
}

/**
 * 定義了商店類型篩選器。
 *
 * @property RECOMMEND 推薦篩選器類型。
 * @property CAN_BUY 可購買篩選器類型。
 * @property PRICES 價格篩選器類型。
 */
enum class ShopTypeFilterType {
    RECOMMEND,
    CAN_BUY,
    PRICES
}
