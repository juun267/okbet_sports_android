package org.cxct.sportlottery.net.games.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
class OKGamesHall(
    val categoryList: List<OKGamesCategory>?,
    val firmList:  List<OKGamesFirm>?,
    val gameGroupList:  List<OKGamesGroup>?,
)

@KeepMembers
data class OKGamesCategory(
    val id: Int,
    val categoryName: String?,
    val icon: String?,
)

@KeepMembers
data class OKGamesFirm(
    val id: Int,
    val firmName: String?,
    val img: String?,
)

@KeepMembers
data class OKGamesGroup(
    val id: Int,
    val firmId: Int,
    val firmType: String?,
    val firmCode: String?,
    val firmName: String?,
    val gameCode: String?,
    val gameName: String?,
    val imgGame: String?,
    val gameEntryTagName: String?,
    var markCollect: Boolean,
)
