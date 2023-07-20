package org.cxct.sportlottery.network.myfavorite.match

data class MyFavoriteMatchRequest(
    val gameType: String?,
    val playCateMenuCode: String,
    val matchType: String = "MYEVENT"
)