package org.cxct.sportlottery.network.bet.list


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.util.replaceSpecialChar

/**
 * 篮球末位比分赔率名字
 *  "oddsId": "sm44943215FS-LD-CS-2-2",
"playCode": "FS-LD-CS-2-2",
"playName": "2-2"
 */
@Parcelize
@JsonClass(generateAdapter = true)
@KeepMembers
data class EndScoreInfo(
    @Json(name = "oddsId")
    val oddsId: String,
    @Json(name = "playCode")
    val playCode: String,
    @Json(name = "playName")
    var playName: String,
) : Parcelable{
    init {
        playName = playName.replaceSpecialChar("\n")
    }
}