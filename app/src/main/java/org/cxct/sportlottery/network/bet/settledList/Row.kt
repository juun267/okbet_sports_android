package org.cxct.sportlottery.network.bet.settledList


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "userId")
    val userId: Int?,
    @Json(name = "platformId")
    val platformId: Int?,
    @Json(name = "gameType")
    val gameType: String?,
    @Json(name = "date")
    val date: String?,
    @Json(name = "settledBetNum")
    val settledBetNum: Int?,
    @Json(name = "settledBetMoney")
    val settledBetMoney: Double?,
    @Json(name = "winBetMoney")
    val winBetMoney: Double?,
    @Json(name = "agentRebateMoney")
    val agentRebateMoney: Double?,
    @Json(name = "vipRewardMoney")
    val vipRewardMoney: Double?,
    @Json(name = "realRebate")
    val realRebate: Double?,
    @Json(name = "vipRewardMoneyWithRebate")
    val vipRewardMoneyWithRebate: Double?,
) : Parcelable