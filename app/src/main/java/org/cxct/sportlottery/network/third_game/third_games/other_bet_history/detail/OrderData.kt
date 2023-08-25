package org.cxct.sportlottery.network.third_game.third_games.other_bet_history.detail


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class OrderData(
    @Json(name = "addTime")
    val addTime: String?,
    @Json(name = "betAmount")
    val betAmount: Double?,
    @Json(name = "betTime")
    val betTime: Long?,
    @Json(name = "dlId")
    val dlId: Int?,
    @Json(name = "dlName")
    val dlName: String?,
    @Json(name = "firmCode")
    val firmCode: String?,
    @Json(name = "firmType")
    val firmType: String?,
    @Json(name = "firmName")
    val firmName: String?,
    @Json(name = "flag")
    val flag: Int?,
    @Json(name = "fullName")
    val fullName: String?,
    @Json(name = "gameName")
    val gameName: String?,
    @Json(name = "gameType")
    val gameType: String?,
    @Json(name = "jackBonus")
    val jackBonus: Double?,
    @Json(name = "jackpot")
    val jackpot: Double?,
    @Json(name = "netAmount")
    val netAmount: Double?,
    @Json(name = "orderNo")
    val orderNo: String?,
    @Json(name = "platformCode")
    val platformCode: String?,
    @Json(name = "remark")
    val remark: String?,
    @Json(name = "statDate")
    val statDate: String?,
    @Json(name = "updateTime")
    val updateTime: String?,
    @Json(name = "userId")
    val userId: Int?,
    @Json(name = "userName")
    val userName: String?,
    @Json(name = "validBetAmount")
    val validBetAmount: Double?,
    @Json(name = "zdlId")
    val zdlId: Int?,
    @Json(name = "zdlName")
    val zdlName: String?
)