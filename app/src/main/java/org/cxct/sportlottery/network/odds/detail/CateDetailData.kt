package org.cxct.sportlottery.network.odds.detail


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.odds.Odd

@Parcelize
@JsonClass(generateAdapter = true) @KeepMembers
data class CateDetailData(
    @Json(name = "name")
    val name: String,
    @Json(name = "odds")
    val odds: MutableList<Odd?>,
    @Json(name = "typeCodes")
    val typeCodes: String,
    @Json(name = "nameMap")
    val nameMap: Map<String?, String?>? = null, //保存各语系name对应值的map
    @Json(name = "rowSort")
    val rowSort: Int,
) : Parcelable