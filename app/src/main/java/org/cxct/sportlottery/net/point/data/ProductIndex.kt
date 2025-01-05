package org.cxct.sportlottery.net.point.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class ProductIndex(
    val blocked: Int,
    val images: List<Image>,
    val limitProducts: List<Product>,
    val pointRule: PointRule,
    val points: Int,
    val productCateVO: List<ProductCateVO>,
    val products: List<Product>,
    val redeemNotifies: List<RedeemNotify>,
    val expiredPoints: ExpiredPoints
)