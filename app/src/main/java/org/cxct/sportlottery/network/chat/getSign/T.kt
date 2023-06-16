package org.cxct.sportlottery.network.chat.getSign


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class T(
    @Json(name = "betMoney")
    val betMoney: String?,
    @Json(name = "currency")
    val currency: String?,
    @Json(name = "flag")
    val flag: String?,
    @Json(name = "growthCode")
    val growthCode: String?,
    @Json(name = "growthUpdateTime")
    val growthUpdateTime: String?,
    @Json(name = "growthValue")
    val growthValue: Int?,
    @Json(name = "iconUrl")
    var iconUrl: String?,
    @Json(name = "nationCode")
    val nationCode: String?,
    @Json(name = "nickName")
    val nickName: String?,
    @Json(name = "platCode")
    val platCode: String?,
    @Json(name = "platformId")
    val platformId: Int?,
    @Json(name = "rechMoney")
    val rechMoney: String?,
    @Json(name = "sign")
    val sign: String?,
    @Json(name = "signTime")
    val signTime: String?,
    @Json(name = "userId")
    val userId: Int?,
    @Json(name = "userLevelConfig")
    val userLevelConfig: UserLevelConfig?,
    @Json(name = "userName")
    val userName: String?,
    @Json(name = "userType")
    val userType: Int?,
    @Json(name = "sysRechMoney")
    val sysRechMoney: String?,
    @Json(name = "sysBetMoney")
    val sysBetMoney: String?,
) {
    /*傳遞出去的參數不可為“” (改為後端傳什麼就送什麼 @Ying:getsign返回的t如果是null就帶null過來 , 簽名的時候會濾掉null的欄位)*/
    fun transformNull() {
        if (iconUrl == "") iconUrl = null
    }
}