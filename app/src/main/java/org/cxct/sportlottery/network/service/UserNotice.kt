package org.cxct.sportlottery.network.service

data class UserNotice(
    val addDate: Long,
    val content: String,
    val id: Int,
    val isRead: Int,
    val msgShowType: Int,
    val noticeType: Int,
    val operatorId: Int,
    val operatorName: String,
    val platformId: Int,
    val tempId: Int,
    val title: String,
    val userId: Int,
    val userName: String
)