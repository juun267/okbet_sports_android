package org.cxct.sportlottery.network.bet.settledList


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true) @KeepMembers
data class Row(
    @Json(name = "userId")
    val userId: Int? = null,
    @Json(name = "platformId")
    val platformId: Int? = null,
    @Json(name = "gameType")
    val gameType: String? = null,
    @Json(name = "statDate")
    val statDate: String? = null,
    @Json(name = "settledBetNum")
    val settledBetNum: Int? = null,
    @Json(name = "settledBetMoney")
    val settledBetMoney: Double? = null,
    @Json(name = "validBetNum")
    val validBetNum: Int? = null,
    @Json(name = "validBetMoney")
    val validBetMoney: Double? = null,
    @Json(name = "winBetMoney")
    val winBetMoney: Double? = null,
    @Json(name = "agentRebateMoney")
    val agentRebateMoney: Double? = null,
    @Json(name = "vipRewardMoney")
    val vipRewardMoney: Double? = null,
    @Json(name = "realRebate")
    val realRebate: Double? = null,
    @Json(name = "vipRewardMoneyWithRebate")
    val vipRewardMoneyWithRebate: Double? = null,
) : Parcelable