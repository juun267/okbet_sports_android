package org.cxct.sportlottery.network.myfavorite.save


data class SaveMyFavoriteRequest(
    val type: Int, //1:sport,2:league,3:match,4:outright,5:playCate
    val code: List<String>,
    val gameType: String? = null
)

