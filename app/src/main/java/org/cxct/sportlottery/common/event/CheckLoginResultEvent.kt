package org.cxct.sportlottery.common.event

import org.cxct.sportlottery.network.index.login.LoginResult

data class CheckLoginResultEvent(var loginResult: LoginResult)