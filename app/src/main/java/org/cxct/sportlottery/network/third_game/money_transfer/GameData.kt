package org.cxct.sportlottery.network.third_game.money_transfer


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GameData(
    @Json(name = "money")
    val money: Int ?= null,
    @Json(name = "remark")
    val remark: String ?= null,
    @Json(name = "transRemaining")
    val transRemaining: String ?= null
) {
    var showName: String = ""
    var code: String ?= null
    var isChecked: Boolean = false
}