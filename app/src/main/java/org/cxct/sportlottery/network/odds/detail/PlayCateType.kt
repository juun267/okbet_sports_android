package org.cxct.sportlottery.network.odds.detail


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@JsonClass(generateAdapter = true) @KeepMembers
data class PlayCateType(
    @Json(name = "code")
    val code: String,
    @Json(name = "gameType")
    val gameType: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "remark")
    val remark: String,
    @Json(name = "sort")
    val sort: Int,
) : Parcelable