package org.cxct.sportlottery.network.user.info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
data class LiveSyncUserInfoVO(
    @Json(name = "platUserId")
    val platUserId: Long?,
    @Json(name = "userName")
    val userName: String?,
    @Json(name = "nickName")
    val nickName: String?,
    @Json(name = "platformCode")
    val platformCode: String?,
    @Json(name = "iconUrl")
    val iconUrl: String?,
    @Json(name = "sign")
    val sign: String?,
    @Json(name = "timestamp")
    val timestamp: Long?,
    @Json(name = "testFlag")
    val testFlag: Int?,
    @Json(name = "loginSrc")
    val loginSrc: Int = 2,
) : java.io.Serializable