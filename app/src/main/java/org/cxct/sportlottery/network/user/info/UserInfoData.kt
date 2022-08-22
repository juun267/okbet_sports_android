package org.cxct.sportlottery.network.user.info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.UserRebate

@JsonClass(generateAdapter = true)
data class UserInfoData(
    @Json(name = "bankName")
    val bankName: String?,
    @Json(name = "cardNo")
    val cardNo: String?,
    @Json(name = "email")
    val email: String?,
    @Json(name = "facebook")
    val facebook: String?,
    @Json(name = "fullName")
    val fullName: String?,
    @Json(name = "hideFlag")
    val hideFlag: Int?,
    @Json(name = "iconUrl")
    val iconUrl: String?,
    @Json(name = "lastLoginIp")
    val lastLoginIp: String,
    @Json(name = "lastLoginTime")
    val lastLoginTime: Long?,
    @Json(name = "loginIp")
    val loginIp: String,
    @Json(name = "loginTime")
    val loginTime: Long,
    @Json(name = "nickName")
    val nickName: String,
    @Json(name = "phone")
    val phone: String,
    @Json(name = "platformId")
    val platformId: Long,
    @Json(name = "qq")
    val qq: String?,
    @Json(name = "remark")
    val remark: String?,
    @Json(name = "roleId")
    val roleId: Int?,
    @Json(name = "setted")
    val setted: Int,
    @Json(name = "subAddress")
    val subAddress: String?,
    @Json(name = "superId")
    val superId: Int,
    @Json(name = "superUserName")
    val superUserName: String,
    @Json(name = "telegram")
    val telegram: String?,
    @Json(name = "testFlag")
    val testFlag: Long,
    @Json(name = "updatePayPw")
    val updatePayPw: Int,
    @Json(name = "updatePw")
    val updatePw: Int,
    @Json(name = "userId")
    val userId: Long,
    @Json(name = "userLevelId")
    val userLevelId: Int,
    @Json(name = "userName")
    val userName: String,
    @Json(name = "userRebateList")
    val userRebateList: List<UserRebate>?,
    @Json(name = "userType")
    val userType: String,
    @Json(name = "wechat")
    val wechat: String?,
    @Json(name = "whatsapp")
    val whatsapp: String?,
    @Json(name = "zalo")
    val zalo: String?,
    @Json(name = "growth")
    val growth: Long?,
    @Json(name = "maxBetMoney")
    val maxBetMoney: Long?,
    @Json(name = "maxCpBetMoney")
    val maxCpBetMoney: Long?,
    @Json(name = "maxParlayBetMoney")
    val maxParlayBetMoney: Long?,
    @Json(name = "discount")
    val discount: Float?,
    @Json(name = "verified")
    val verified: Int?, // 是否通过实名验证,0:未通过 1:已通过 2:验证中 3:验证失败
    @Json(name = "perBetLimit")
    val perBetLimit: Int?,
    @Json(name = "uwEnableTime")
    val uwEnableTime: Long? = 0,
    @Json(name = "maxPayout")
    val maxPayout: Double? = 0.0,
    @Json(name = "firstRechTime")
    val firstRechTime: String?
)