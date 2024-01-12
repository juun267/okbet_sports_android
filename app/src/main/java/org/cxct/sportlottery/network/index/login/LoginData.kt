package org.cxct.sportlottery.network.index.login

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.UserGameTypeDiscount
import org.cxct.sportlottery.network.common.UserRebate
import org.cxct.sportlottery.network.user.info.LiveSyncUserInfoVO

@KeepMembers
data class LoginData(
    val fullName: String?, //真实名称
    val iconUrl: String?, //头像地址
    val lastLoginDate: Long?, //最近一次登录日期
    val lastLoginIp: String?, //最近一次登录ip
    val loginDate: Long?, //登录日期
    val loginIp: String?, //登录ip
    var nickName: String?, //昵称
    val platformId: Long?, //平台id
    val rechLevel: String?, //充值层级
    val testFlag: Long?, //是否测试用户（0-正常用户，1-游客，2-内部测试）
    val token: String?,
    val uid: Long?, //用户id（保留，建议用userId
    val userId: Long?=0, //用户id
    val userName: String?, //用户名
    val userType: String?, //用户类型（"ADMIN"：超管；"ZDL"：总代理；"DL"：代理；"HY"：会员；"ZZH"：子账户）
    val hyType: Int?, //会员类型
    val userRebateList: List<UserRebate>?, //用户返点数据
    val maxBetMoney: Long?,
    val maxCpBetMoney: Long?,
    val maxParlayBetMoney: Long?,
    var deviceValidateStatus: Int?, //0: 未验证(需要驗證), 1: 已验证(不需要驗證)
    var verified: Int?,
    var liveSyncUserInfoVO: LiveSyncUserInfoVO?,
    var ifnew: Boolean?,
    var vipType: Int?, //0是普通用户，1是Glife用户
    var msg: String?,   //错误信息提示，需要判断
    var isCreateAccount: Int?, // 提醒创建平台用户(如果登录用户为glife用户) 0-否 1-是
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val birthday: String?,
    val needOTPLogin: Boolean, // 账号长时间未登陆需要验证手机号
    val phone: String?,         // 账号长时间未登陆需要验证手机号时会返回
    val email: String?,
    val firstPhoneGiveMoney: Boolean?,// 注册绑定手机送金额
    val discountByGameTypeList: List<UserGameTypeDiscount>?
) : java.io.Serializable