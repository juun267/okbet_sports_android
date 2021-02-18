package org.cxct.sportlottery.ui.base

import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository

abstract class BaseOddButtonViewModel(
    loginRepository: LoginRepository,
    val betInfoRepository: BetInfoRepository
) : BaseSocketViewModel(loginRepository)