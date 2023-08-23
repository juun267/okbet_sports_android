package org.cxct.sportlottery.network.user

import androidx.annotation.Keep
import org.cxct.sportlottery.network.common.UserRebate

@Keep
data class UserInfo(
    var userId: Long,
    var fullName: String? = null,
    var iconUrl: String? = null,
    var lastLoginIp: String? = null,
    var loginIp: String? = null,
    var nickName: String? = null,
    var platformId: Long? = null,
    var testFlag: Long? = null,
    var userName: String? = null,
    var userType: String? = null,
    var email: String? = null,
    var qq: String? = null,
    var phone: String? = null,
    var wechat: String? = null,
    var updatePayPw: Int? = 1,
    var setted: Int? = null, //是否设置过昵称 0单标未设置过 1代表设置过
    var userRebateList: List<UserRebate>? = null,
    var maxBetMoney: Long? = null,
    //会员对应vip层级的串关最大下注额
    var maxParlayBetMoney: Long? = null,
    //会员对应vip层级的单注冠军最大下注额
    var maxCpBetMoney: Long? = null,
    var discount: Float? = null,
    var verified: Int? = null, // 是否通过实名验证,0:未通过 1:已通过 2:验证中 3:验证失败
    val perBetLimit: Int? = null,
    var oddsChangeOption: Int? = null,
    val uwEnableTime: Long? = 0,
    val maxPayout: Double? = 0.0,
    val firstRechTime: String? = null, //首充时间
    val currencySign: String? = null, //幣種
    var facebookBind: Boolean = false,
    var googleBind: Boolean = false,
    var passwordSet: Boolean = true,//true 密码为空
    //0是普通用户，1是Glife用户
    var vipType: Int?,
    var placeOfBirth: String?="",
    var address: String?="",
    var permanentAddress: String?="",
    var zipCode: String?="",
    var permanentZipCode: String?="",
){
    fun isGlifeAccount():Boolean = vipType==1
}

