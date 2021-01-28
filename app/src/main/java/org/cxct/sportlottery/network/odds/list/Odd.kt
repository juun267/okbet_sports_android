package org.cxct.sportlottery.network.odds.list

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Odd(
    @Json(name = "id")
    val id: String,
    @Json(name = "odds")
    var odds: Double?,
    @Json(name = "producerId") var producerId: Int,
    @Json(name = "spread")
    val spread: String?,
    @Json(name = "status")
    var status: Int = BetStatus.ACTIVATED.code
) {
    var oddState: Int = OddState.SAME.state
    var isSelected = false
}

enum class OddState(val state : Int) {
    SAME(0),
    LARGER(1),
    SMALLER(2)
}

//0:活跃可用，可投注、1：临时锁定，不允许投注、2：不可用，不可见也不可投注
enum class BetStatus(val code : Int) {
    ACTIVATED(0),
    LOCKED(1),
    DEACTIVATED(2)
}