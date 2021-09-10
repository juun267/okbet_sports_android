package org.cxct.sportlottery.ui.game

data class LiveStreamInfo(
    val matchId: String,
    val streamUrl: String,
    var isNewest: Boolean = false
)
