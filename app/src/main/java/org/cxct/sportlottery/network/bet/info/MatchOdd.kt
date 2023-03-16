package org.cxct.sportlottery.network.bet.info

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.enum.SpreadState
import org.cxct.sportlottery.network.error.BetAddError

@JsonClass(generateAdapter = true) @KeepMembers
data class MatchOdd(
    @Json(name = "awayName")
    val awayName: String?,
    @Json(name = "homeName")
    val homeName: String?,
    @Json(name = "inplay")
    val inplay: Int,
    @Json(name = "leagueId")
    val leagueId: String,
    @Json(name = "leagueName")
    val leagueName: String?,
    @Json(name = "matchId")
    val matchId: String,
    @Json(name = "odds")
    var odds: Double,
    @Json(name = "hkOdds")
    var hkOdds: Double,
    @Json(name = "malayOdds")
    var malayOdds: Double,
    @Json(name = "indoOdds")
    var indoOdds: Double,
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
    val startTime: Long?,
    @Json(name = "status")
    var status: Int?,
    @Json(name = "gameType")
    var gameType: String,
    @Json(name = "homeScore")
    var homeScore: Int,
    @Json(name = "awayScore")
    var awayScore: Int,

    ) {
    var oddState: Int = OddState.SAME.state
    @Transient
    var runnable: Runnable? = null //賠率變更，按鈕顏色變換任務
    var betAddError: BetAddError? = null
    var oddsHasChanged = false
    var spreadState: Int = SpreadState.SAME.state
    var extInfo: String? = null //球員名稱
    var isOnlyEUType: Boolean = false
    var homeCornerKicks: Int? = null
    var awayCornerKicks: Int? = null
}

