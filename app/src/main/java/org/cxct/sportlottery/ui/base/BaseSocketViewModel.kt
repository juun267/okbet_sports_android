package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.index.playquotacom.t.PlayQuotaComData
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository

abstract class BaseSocketViewModel(
    androidContext: Application,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseOddButtonViewModel(
    androidContext,
    loginRepository,
    betInfoRepository,
    infoCenterRepository
) {

    init {
        if (!loginRepository.isCheckToken) {
            viewModelScope.launch {
                loginRepository.checkToken()
            }
        }
    }

    fun updatePlayQuota(playQuotaComData: PlayQuotaComData) {
        betInfoRepository.playQuotaComData = playQuotaComData
    }
}