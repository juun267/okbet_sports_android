package org.cxct.sportlottery.network.odds.list

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Odd(
    @Json(name = "id")
    val id: String? = "",
    @Json(name = "odds")
    var odds: Double? = null,
    @Json(name = "hkOdds")
    var hkOdds: Double? = null,
    @Json(name = "producerId")
    var producerId: Int? = null,
    @Json(name = "spread")
    val spread: String? = null,
    @Json(name = "status")
    var status: Int = BetStatus.ACTIVATED.code
) {
    var isSelected :Boolean? = false
    var oddState: Int = OddState.SAME.state
}

//socket進來的新賠率較大或較小
enum class OddState(val state: Int) {
    SAME(0),
    LARGER(1),
    SMALLER(2)
}

//0:活跃可用，可投注、1：临时锁定，不允许投注、2：不可用，不可见也不可投注
enum class BetStatus(val code: Int) {
    ACTIVATED(0),
    LOCKED(1),
    DEACTIVATED(2)
}