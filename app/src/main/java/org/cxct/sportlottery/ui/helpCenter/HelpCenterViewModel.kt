package org.cxct.sportlottery.ui.helpCenter

import android.app.Application
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseSocketViewModel

class HelpCenterViewModel(
    androidContext: Application,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseSocketViewModel(
    androidContext,
    loginRepository,
    betInfoRepository,
    infoCenterRepository
) {
    val token = loginRepository.token
}