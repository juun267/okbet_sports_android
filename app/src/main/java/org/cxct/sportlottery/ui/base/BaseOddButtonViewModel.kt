package org.cxct.sportlottery.ui.base

import androidx.lifecycle.LiveData
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository

abstract class BaseOddButtonViewModel(
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseSocketViewModel(loginRepository, betInfoRepository, infoCenterRepository){

    val oddsType: LiveData<String> = loginRepository.mOddsType

    fun saveOddsType(oddsType: String) {
        loginRepository.sOddsType = oddsType
        loginRepository.mOddsType.postValue(oddsType)
    }

    fun getOddsType(){
        loginRepository.mOddsType.postValue(loginRepository.sOddsType)
    }

}