package org.cxct.sportlottery.network.bet.info

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.common.enums.SpreadState
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.error.BetAddError
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate

@Parcelize
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
    override var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null,
    override var playCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null,
    override val matchInfo: org.cxct.sportlottery.network.odds.MatchInfo?,
    override var oddsMap: MutableMap<String, MutableList<Odd>?>? = null,
    override val oddsSort: String? = null,
    override var quickPlayCateList: MutableList<QuickPlayCate>? = null,
    override val oddsEps: EpsOdd? = null,
) : Parcelable, org.cxct.sportlottery.network.common.MatchOdd {
    var oddState: Int = OddState.SAME.state

    @Transient
    var runnable: Runnable? = null //賠率變更，按鈕顏色變換任務
    var betAddError: BetAddError? = null
    var oddsHasChanged = false
    var spreadState: Int = SpreadState.SAME
    var extInfo: String? = null //球員名稱
    var isOnlyEUType: Boolean = false
    var homeCornerKicks: Int? = null
    var awayCornerKicks: Int? = null
    var categoryCode: String? = null
}

