package org.cxct.sportlottery.network.sport


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.odds.list.LeagueOdd

@JsonClass(generateAdapter = true)
@KeepMembers
data class Item(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "num")
    val num: Int,
    @Json(name = "sortNum")
    val sortNum: Int
) {
    var isSelected: Boolean = false
    var playCateNum: Int? = null

    var leagueOddsList: List<LeagueOdd> = listOf() // 收藏赛事-该字段会手动负值
}