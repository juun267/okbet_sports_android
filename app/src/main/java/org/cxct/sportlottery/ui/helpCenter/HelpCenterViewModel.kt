package org.cxct.sportlottery.ui.helpCenter

import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseViewModel

class HelpCenterViewModel(private val loginRepository: LoginRepository) : BaseViewModel() {
    val token = loginRepository.token
}