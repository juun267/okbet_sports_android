package org.cxct.sportlottery.network.bet.add


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.network.bet.MatchOdd
import java.io.Serializable

@Parcelize
@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "eventType")
    val eventType: Int,
    @Json(name = "matchOdds")
    val matchOdds: List<org.cxct.sportlottery.network.bet.add.MatchOdd>,
    @Json(name = "num")
    val num: Int,
    @Json(name = "orderNo")
    val orderNo: String,
    @Json(name = "parlay")
    val parlay: Int,
    @Json(name = "parlayType")
    val parlayType: String,
    @Json(name = "winnable")
    val winnable: Double,
    @Json(name = "stake")
    val stake: Int,
    @Json(name = "status")
    val status: Int
) : Parcelable