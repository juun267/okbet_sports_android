package org.cxct.sportlottery.network.odds.detail

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState

@JsonClass(generateAdapter = true)
data class Odd(
    @Json(name = "extInfo")
    var extInfo: String?, //额外信息. （如果是球员玩法，则H表示主队,C表示客队）
    @Json(name = "id")
    var id: String, //赔率id
    @Json(name = "name")
    var name: String?, //玩法名称（如果是球员玩法，则名称代码球员名称）
    @Json(name = "odds")
    var odds: Double?, //赔率
    @Json(name = "producerId")
    var producerId: Int?, //赔率生产者
    @Json(name = "spread")
    var spread: String?, //让分或大小分值 (如果是球员玩法，则表示球员ID)
    @Json(name = "status")
    var status: Int? = BetStatus.ACTIVATED.code, //0:活跃可用，可投注、1：临时锁定，不允许投注、2：不可用，不可见也不可投注
) {
    var isSelect: Boolean = false
    var oddState: Int = OddState.SAME.state
}