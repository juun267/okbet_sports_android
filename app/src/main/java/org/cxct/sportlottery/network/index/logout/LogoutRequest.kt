package org.cxct.sportlottery.network.index.logout

import cn.jpush.android.api.JPushInterface
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class LogoutRequest(
    val uid: Int? = null,
    val userId: Int? = null,
    val token: String? = null,
    val loginDate: String? = null,
    val loginIp: String? = null,
    val domain: String? = null,
    val lastLoginDate: String? = null,
    val lastLoginIp: String? = null,
    val userName: String? = null,
    val nickName: String? = null,
    val userType: String? = null,
    val queryCode: String? = null,
    val platformId: Int? = null,
    val fullName: String? = null,
    val testFlag: Int? = null,
    val permission: String? = null,
    val platformName: String? = null,
    val deviceSn: String = JPushInterface.getRegistrationID(MultiLanguagesApplication.getInstance()),
    val iconUrl: String? = null,
    val innerAdmin: Int? = null
)
