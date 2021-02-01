package org.cxct.sportlottery.ui.helpCenter

import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseViewModel

class HelpCenterViewModel(private val loginRepository: LoginRepository, betInfoRepo: BetInfoRepository) : BaseViewModel() {
    init {
        betInfoRepository = betInfoRepo
    }
    val token = loginRepository.token
}