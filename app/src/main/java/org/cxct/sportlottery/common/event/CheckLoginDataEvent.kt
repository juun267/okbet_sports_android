package org.cxct.sportlottery.common.event

import org.cxct.sportlottery.network.index.login.LoginData

data class CheckLoginDataEvent(var loginData: LoginData)