package org.cxct.sportlottery.network.sport.publicityRecommend


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@JsonClass(generateAdapter = true) @KeepMembers
data class Menu(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "num")
    val num: Int,
    @Json(name = "playCateList")
    val playCateList: List<PlayCate>,
    @Json(name = "selectionType")
    val selectionType: Int,
) : Parcelable