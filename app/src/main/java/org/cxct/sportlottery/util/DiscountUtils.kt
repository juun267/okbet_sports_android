package org.cxct.sportlottery.util

import java.math.BigDecimal

object DiscountUtils {

    fun BigDecimal.applyDiscount(discount: BigDecimal): BigDecimal {
        return this.subtract(BigDecimal.ONE).multiply(discount).add(BigDecimal.ONE)
    }

    fun BigDecimal.applyHKDiscount(discount: BigDecimal): BigDecimal {
        return this.multiply(discount)
    }
}
