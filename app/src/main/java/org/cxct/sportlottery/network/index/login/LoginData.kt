package org.cxct.sportlottery.network.index.login

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.UserRebate

@JsonClass(generateAdapter = true)
data class LoginData(
    @Json(name = "fullName")
    val fullName: String?, //真实名称
    @Json(name = "iconUrl")
    val iconUrl: String?, //头像地址
    @Json(name = "lastLoginDate")
    val lastLoginDate: Long?, //最近一次登录日期
    @Json(name = "lastLoginIp")
    val lastLoginIp: String?, //最近一次登录ip
    @Json(name = "loginDate")
    val loginDate: Long?, //登录日期
    @Json(name = "loginIp")
    val loginIp: String?, //登录ip
    @Json(name = "nickName")
    var nickName: String?, //昵称
    @Json(name = "platformId")
    val platformId: Long?, //平台id
    @Json(name = "rechLevel")
    val rechLevel: String?, //充值层级
    @Json(name = "testFlag")
    val testFlag: Long?, //是否测试用户（0-正常用户，1-游客，2-内部测试）
    @Json(name = "token")
    val token: String?,
    @Json(name = "uid")
    val uid: Long?, //用户id（保留，建议用userId
    @Json(name = "userId")
    val userId: Long, //用户id
    @Json(name = "userName")
    val userName: String?, //用户名
    @Json(name = "userType")
    val userType: String?, //用户类型（"ADMIN"：超管；"ZDL"：总代理；"DL"：代理；"HY"：会员；"ZZH"：子账户）
    @Json(name = "hyType")
    val hyType: Int?, //会员类型
    @Json(name = "userRebateList")
    val userRebateList: List<UserRebate>?, //用户返点数据
    @Json(name = "maxBetMoney")
    val maxBetMoney: Int?,
    @Json(name = "maxCpBetMoney")
    val maxCpBetMoney: Int?,
    @Json(name = "maxParlayBetMoney")
    val maxParlayBetMoney: Int?,
    @Json(name = "discount")
    var discount: Float?, //後台維修 暫時修改做測試 要改回va
    @Json(name = "deviceValidateStatus")
    var deviceValidateStatus: Int?,
    @Json(name = "verified")
    var verified: Int?,
)