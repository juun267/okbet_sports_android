package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.index.ConfigData
import org.cxct.sportlottery.network.index.LoginData

const val FLAG_OPEN = "1"
const val FLAG_CLOSE = "0"

var sLoginData: LoginData? = null
var sConfigData: ConfigData? = null
//TODO Dean : 測試使用, 串接/api/user/info後將此處重新review
var sUserInfo: UserInfo = UserInfo()
