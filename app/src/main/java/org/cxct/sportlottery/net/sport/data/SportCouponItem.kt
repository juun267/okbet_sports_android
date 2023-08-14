package org.cxct.sportlottery.net.sport.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class SportCouponItem(
    val couponCode: String?,
    val couponName: String?,
    val icon: String?,
    val num: Int,
    val sort: Int,
    val pcPubImage: String?,
    val h5PubImage: String?,
    val isFiba: Boolean = false
)