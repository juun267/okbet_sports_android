package org.cxct.sportlottery.net.games.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
class OKGamesHall(
    val categoryList: List<OKGamesCategory>?,
    val firmList: List<OKGamesFirm>?,
    val collectList: List<OKGameBean>?,
)

@KeepMembers
data class OKGamesCategory(
    val id: Int,
    val categoryName: String?,
    val icon: String?,
    val iconSelected: String?,
    val iconUnselected: String?,
    val gameList: List<OKGameBean>?,
)

@KeepMembers
data class OKGamesFirm(
    val id: Int,
    val firmName: String?,//厂商名称
    val img: String?,//厂商图
)

@KeepMembers
data class OKGameBean(
    val id: Int,
    val firmId: Int,
    val firmType: String?,
    val firmCode: String?,
    val firmName: String?,
    val gameCode: String?,
    val gameName: String?,
    val imgGame: String?,
    val gameEntryTagName: String?,
    val thirdGameCategory: String?,
    var markCollect: Boolean,
)
