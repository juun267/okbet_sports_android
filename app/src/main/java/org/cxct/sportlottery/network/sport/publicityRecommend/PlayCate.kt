package org.cxct.sportlottery.network.sport.publicityRecommend


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@JsonClass(generateAdapter = true)
@KeepMembers
data class PlayCate(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
) : Parcelable