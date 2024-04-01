package org.cxct.sportlottery.network.bet.add.betReceipt


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@JsonClass(generateAdapter = true)
@KeepMembers
data class Receipt(
    @Json(name = "parlayBets")
    val parlayBets: List<BetResult>?,
    @Json(name = "singleBets")
    val singleBets: List<BetResult>?,
    @Json(name = "totalStake")
    val totalStake: Double?,
    @Json(name = "totalWinnable")
    val totalWinnable: Double?,
    @Json(name = "totalNum")
    val totalNum: Int?,
    @Json(name = "userPlayAmount")
    val userPlayAmount: Double?,
    @Json(name = "betConfirmTime")
    val betConfirmTime: Long? = null
):Parcelable