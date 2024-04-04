package org.cxct.sportlottery.network.odds


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
@Parcelize
data class League(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "shortName")
    val shortName: String?,
    @Json(name = "category")
    val category: String,
    @Json(name = "categoryCode")
    val categoryCode: String,
    @Json(name = "categoryIcon")
    val categoryIcon: String,
    @Json(name = "icon")
    val icon: String?,
):Parcelable{
    var firstCap: String?=null
    var isSelected: Boolean=true
}