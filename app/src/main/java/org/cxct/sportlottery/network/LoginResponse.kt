package org.cxct.sportlottery.network

data class LoginResponse(
    val code: Int,
    val msg: String,
    val success: Boolean,
    val t: T
)

data class T(
    val fullName: String,
    val iconUrl: Any,
    val lastLoginDate: Long,
    val lastLoginIp: String,
    val loginDate: Long,
    val loginIp: String,
    val nickName: Any,
    val platformId: Int,
    val rechLevel: Any,
    val testFlag: Int,
    val token: String,
    val uid: Int,
    val userId: Int,
    val userName: String,
    val userType: String
)
