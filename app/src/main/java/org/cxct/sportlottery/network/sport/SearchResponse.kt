package org.cxct.sportlottery.network.sport

import com.chad.library.adapter.base.entity.node.BaseNode
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import com.squareup.moshi.Json
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true) @KeepMembers
data class SearchResponse(
    @Json(name = "code")
    override val code: Int = 0,
    @Json(name = "msg")
    override val msg: String = "",
    @Json(name = "rows")
    val rows: List<Row> = listOf(),
    @Json(name = "success")
    override val success: Boolean = false,
    @Json(name = "total")
    val total: Int = 0
): BaseResult() {
    @JsonClass(generateAdapter = true) @KeepMembers
    data class Row(
        @Json(name = "gameName")
        val gameName: String = "",
        @Json(name = "gameType")
        val gameType: String = "",
        @Json(name = "leagueMatchList")
        val leagueMatchList: List<LeagueMatch> = listOf(),
        @Json(name = "sort")
        val sort: Int = 0
    ) {
        @JsonClass(generateAdapter = true) @KeepMembers
        data class LeagueMatch(
            @Json(name = "leagueName")
            val leagueName: String = "",
            @Json(name = "matchInfoList")
            val matchInfoList: List<MatchInfo> = listOf(),
            @Json(name = "sort")
            val sort: Int = 0
        ) {
            @JsonClass(generateAdapter = true) @KeepMembers
            data class MatchInfo(
                @Json(name = "awayName")
                val awayName: String = "",
                @Json(name = "homeName")
                val homeName: String = "",
                @Json(name = "matchId")
                val matchId: String = "",
                @Json(name = "startTime")
                val startTime: String = "",
            ): BaseNode() {
                override val childNode: MutableList<BaseNode>? = null
                var isInPlay: Boolean = false
                @Transient
                var gameType: String = "" //本地赋值
            }
        }
    }
}