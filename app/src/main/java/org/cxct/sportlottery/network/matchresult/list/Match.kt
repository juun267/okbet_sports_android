package org.cxct.sportlottery.network.matchresult.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Match(
    @Json(name = "matchInfo")
    val matchInfo: MatchInfo,
    @Json(name = "matchStatusList")
    val matchStatusList: List<MatchStatus>
) {

    private val matchStatusMap: Map<Int, MatchStatus> by lazy {
        val matchStatus = mutableMapOf<Int, MatchStatus>()
        matchStatusList.forEach {
            matchStatus[it.status] = it
        }
        matchStatus.toMap()
    }

    fun getMatch(statu: Int): MatchStatus? {
        return matchStatusMap[statu]
    }
}