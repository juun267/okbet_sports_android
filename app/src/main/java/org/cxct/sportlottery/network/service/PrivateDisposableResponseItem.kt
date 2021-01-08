package org.cxct.sportlottery.network.service

data class PrivateDisposableResponseItem(
    val eventType: String,
    val money: Double,
    val userNoticeList: List<UserNotice>
)