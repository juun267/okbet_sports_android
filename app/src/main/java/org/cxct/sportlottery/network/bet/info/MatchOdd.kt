package org.cxct.sportlottery.network.bet.info

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.SpreadState
import org.cxct.sportlottery.network.error.BetAddError

@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "awayName")
    val awayName: String,
    @Json(name = "homeName")
    val homeName: String,
    @Json(name = "inplay")
    val inplay: Int,
    @Json(name = "leagueId")
    val leagueId: String,
    @Json(name = "leagueName")
    val leagueName: String,
    @Json(name = "matchId")
    val matchId: String,
    @Json(name = "odds")
    var odds: Double,
    @Json(name = "hkOdds")
    var hkOdds: Double,
    @Json(name = "oddsId")
    var oddsId: String,
    @Json(name = "playCateId")
    val playCateId: Int,
    @Json(name = "playCateName")
    val playCateName: String,
    @Json(name = "playCode")
    val playCode: String,
    @Json(name = "playId")
    val playId: Int,
    @Json(name = "playName")
    val playName: String,
    @Json(name = "producerId")
    val producerId: Int,
    @Json(name = "spread")
    var spread: String,
    @Json(name = "startTime")
    val startTime: Long,
    @Json(name = "status")
    var status: Int = BetStatus.ACTIVATED.code,
    @Json(name = "gameType")
    var gameType: String,
    @Json(name = "homeScore")
    var homeScore: Int,
    @Json(name = "awayScore")
    var awayScore: Int,

    ) {
    var oddState: Int = OddState.SAME.state
    var runnable: Runnable? = null //賠率變更，按鈕顏色變換任務
    var betAddError: BetAddError? = null
    var oddsHasChanged = false
    var spreadState: Int = SpreadState.SAME.state

    //socket進來的新賠率較大或較小
    enum class OddState(val state: Int) {
        SAME(0),
        LARGER(1),
        SMALLER(2)
    }

}

