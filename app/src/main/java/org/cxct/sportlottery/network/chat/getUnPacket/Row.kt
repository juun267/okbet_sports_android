package org.cxct.sportlottery.network.chat.getUnPacket


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "id")
    val id: Int,
    @Json(name = "roomId")
    val roomId: Int,
    @Json(name = "currency")
    val currency: String,
    @Json(name = "rechMoney")
    val rechMoney: Int,
    @Json(name = "betMoney")
    val betMoney: Int,
    @Json(name = "createBy")
    val createBy: String,
    @Json(name = "createDate")
    val createDate: Long,
    @Json(name = "status")
    val status: Int,
    @Json(name = "packetType")
    val packetType: Int,
    @Json(name = "platformId")
    val platformId: Int,
)