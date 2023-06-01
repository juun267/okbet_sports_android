package org.cxct.sportlottery.net.chat.data


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
data class Row(
    @Json(name = "basicAmount")
    val basicAmount: Int,
    @Json(name = "betMoney")
    val betMoney: Double,
    @Json(name = "constraintType")
    val constraintType: String,
    @Json(name = "createDate")
    val createDate: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "isOpen")
    val isOpen: String,
    @Json(name = "isShowCount")
    val isShowCount: String,
    @Json(name = "isSpeak")
    val isSpeak: String,
    @Json(name = "language")
    val language: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "onlineCount")
    val onlineCount: Int,
    @Json(name = "platCode")
    val platCode: String,
    @Json(name = "platName")
    val platName: String,
    @Json(name = "rechMoney")
    val rechMoney: Double,
    @Json(name = "remark")
    val remark: String,
)