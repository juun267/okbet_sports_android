package org.cxct.sportlottery.network.vip

/**
 * 多隻api發送request時, 當所有的api都獲取response後才隱藏loading view
 */
data class LoadingResult (
    var userInfoLoading: Boolean = false,
    var userGrowthLoading: Boolean = false,
    var thirdRebatesLoading: Boolean = false
)