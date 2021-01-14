package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.index.ConfigData
import org.cxct.sportlottery.network.index.LoginData

const val FLAG_OPEN = "1"
const val FLAG_CLOSE = "0"

var sLoginData: LoginData? = null
var sConfigData: ConfigData? = null