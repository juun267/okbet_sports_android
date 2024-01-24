package org.cxct.sportlottery.ui.helpCenter

import android.app.Application
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.base.BaseUserViewModel
import org.cxct.sportlottery.ui.base.BaseViewModel

class HelpCenterViewModel(
    androidContext: Application,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
) : BaseViewModel(
    androidContext,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
) {
    val token = loginRepository.token
}