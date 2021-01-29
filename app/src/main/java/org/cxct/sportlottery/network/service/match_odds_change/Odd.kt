package org.cxct.sportlottery.network.service.match_odds_change


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Deprecated(message = "不取extInfo和name, 以Odd取代", replaceWith = ReplaceWith("org.cxct.sportlottery.network.odds.list.Odd"))
@JsonClass(generateAdapter = true)
data class Odd(
    @Json(name = "extInfo")
    val extInfo: String?, //额外信息. （如果是球员玩法，则H表示主队,C表示客队）
    @Json(name = "id")
    val id: String, //赔率id
    @Json(name = "name")
    val name: String?, //玩法名称（如果是球员玩法，则名称代码球员名称）
    @Json(name = "odds")
    val odds: Double?, //赔率
    @Json(name = "producerId")
    val producerId: Int?, //赔率生产者
    @Json(name = "spread")
    val spread: String?, //让分或大小分值 (如果是球员玩法，则表示球员ID)
    @Json(name = "status")
    val status: Int?, //0:活跃可用，可投注、1：临时锁定，不允许投注、2：不可用，不可见也不可投注
)