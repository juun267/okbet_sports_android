package org.cxct.sportlottery.network.odds.list


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.odds.Odd

@Parcelize
@JsonClass(generateAdapter = true) @KeepMembers
data class QuickPlayCate(
    @Json(name = "code")
    val code: String?,
    @Json(name = "gameType")
    val gameType: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "sort")
    val sort: Int?,
    @Json(name = "nameMap")
    val nameMap: Map<String?, String?>? = null,
) : Parcelable {
    var isSelected = false
    val quickOdds: MutableMap<String, MutableList<Odd?>?> = mutableMapOf()

}