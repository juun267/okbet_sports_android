package org.cxct.sportlottery.ui.helpCenter

import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel

class HelpCenterViewModel(
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository
) : BaseOddButtonViewModel(loginRepository, betInfoRepository) {

    val token = loginRepository.token
}