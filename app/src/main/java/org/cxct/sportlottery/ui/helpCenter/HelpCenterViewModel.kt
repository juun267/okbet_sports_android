package org.cxct.sportlottery.ui.helpCenter

import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseViewModel

class HelpCenterViewModel(private val loginRepository: LoginRepository, betInfoRepository: BetInfoRepository) : BaseViewModel() {
    init {
        br = betInfoRepository
    }
    val token = loginRepository.token
}