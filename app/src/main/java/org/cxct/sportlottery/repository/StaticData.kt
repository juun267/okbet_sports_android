package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.index.config.ConfigData
import org.cxct.sportlottery.network.index.login.LoginData

const val FLAG_OPEN = "1"
const val FLAG_CLOSE = "0"
const val LOGIN_SRC: Long = 2 //登录来源，WEB(0), MOBILE_BROWSER(1), ANDROID(2), IOS(3);

var sLoginData: LoginData? = null
var sConfigData: ConfigData? = null
//TODO Dean : 測試使用, 串接/api/user/info後將此處重新review
var sUserInfo: UserInfo = UserInfo()
