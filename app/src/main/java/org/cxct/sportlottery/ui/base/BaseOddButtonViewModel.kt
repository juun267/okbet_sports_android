package org.cxct.sportlottery.ui.base

import androidx.lifecycle.LiveData
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.menu.OddsType

abstract class BaseOddButtonViewModel(
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseSocketViewModel(loginRepository, betInfoRepository, infoCenterRepository){

    val oddsType: LiveData<OddsType> = loginRepository.mOddsType

    fun saveOddsType(oddsType: OddsType) {
        loginRepository.sOddsType = oddsType.code
        loginRepository.mOddsType.postValue(oddsType)
    }

    fun getOddsType(){
        loginRepository.mOddsType.postValue(
            when(loginRepository.sOddsType){
                OddsType.EU.code -> OddsType.EU
                OddsType.HK.code -> OddsType.HK
                else -> OddsType.EU
            }
        )
    }

}