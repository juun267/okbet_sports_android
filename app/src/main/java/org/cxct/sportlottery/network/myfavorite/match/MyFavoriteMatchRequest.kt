package org.cxct.sportlottery.network.myfavorite.match

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class MyFavoriteMatchRequest(
    val gameType: String?,
    val playCateMenuCode: String,
)