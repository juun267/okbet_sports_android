package org.cxct.sportlottery.network.bet.settledDetailList

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class ParlayComsDetailVO(
    @Json(name = "matchOddsVOList")
    val matchOddsVOList: List<MatchOddsVO>,
    @Json(name = "stake")
    val stake: Double?,
    @Json(name = "status")
    val status: Int?,
    @Json(name = "winMoney")
    val winMoney: Double?
): Parcelable