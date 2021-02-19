package org.cxct.sportlottery.ui.game

import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel

class GameViewModel(
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository
) : BaseOddButtonViewModel(loginRepository, betInfoRepository) {
}