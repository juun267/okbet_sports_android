package org.cxct.sportlottery.network.sport


data class SaveMyFavoriteRequest(
    val code: FavoriteType,
    val type: String
)

data class FavoriteType(
    val sport: String,
    val league: String,
    val match: String,
    val outright: String,
    val playCate: String,
)



