package org.cxct.sportlottery.network.third_game.money_transfer


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class GameData(
    @Json(name = "money")
    val money: Int ?= null,
    @Json(name = "remark")
    val remark: String ?= null,
    @Json(name = "transRemaining")
    val transRemaining: String ?= null
): Parcelable {
    var showName: String = ""
    var code: String ?= null
    var isChecked: Boolean = false
}