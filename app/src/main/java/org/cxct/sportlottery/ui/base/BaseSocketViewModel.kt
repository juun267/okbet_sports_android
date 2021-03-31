package org.cxct.sportlottery.ui.base

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository

abstract class BaseSocketViewModel(
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    init {
        if (!loginRepository.isCheckToken) {
            viewModelScope.launch {
                loginRepository.checkToken()
            }
        }
    }
}