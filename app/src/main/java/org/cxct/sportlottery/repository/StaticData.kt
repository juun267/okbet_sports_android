package org.cxct.sportlottery.repository

import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.network.index.config.ConfigData
import org.cxct.sportlottery.network.index.login.LoginData

const val FLAG_OPEN = "1"
const val FLAG_CLOSE = "0"
const val FLAG_IS_NEED_UPDATE_PAY_PW = 1 //尚未設置過資金密碼的 flag，需要更新資金密碼
const val FLAG_NICKNAME_IS_SET = 1 //已經設置過暱稱

enum class TestFlag(val index: Long) { NORMAL(0), GUEST(1), TEST(2) } //是否测试用户（0-正常用户，1-游客，2-内部测试）

const val LOGIN_SRC: Long = 2 //登录来源，WEB(0), MOBILE_BROWSER(1), ANDROID(2), IOS(3);

const val PLATFORM_CODE = BuildConfig.CHANNEL_NAME //平台代碼
const val PROJECT_CODE = "cx_sports" //項目代碼

var sConfigData: ConfigData? = null
