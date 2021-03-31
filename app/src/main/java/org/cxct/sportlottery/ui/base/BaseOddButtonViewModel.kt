package org.cxct.sportlottery.ui.base

import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository

abstract class BaseOddButtonViewModel(
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseSocketViewModel(loginRepository, betInfoRepository, infoCenterRepository)