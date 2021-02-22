package org.cxct.sportlottery.network.news

data class Row(
    val addTime: String,
    val id: Int,
    val message: String,
    val msgType: Int,
    val platformId: Int,
    val rechLevels: String,
    val sort: Int,
    val title: String,
    val type: Int,
    val updateTime: String
)