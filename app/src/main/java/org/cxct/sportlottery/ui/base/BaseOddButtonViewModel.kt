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

    val oddType: LiveData<String> = loginRepository.mOddType

    fun saveOddType(oddType: String) {
        loginRepository.sOddType = oddType
        loginRepository.mOddType.postValue(oddType)
    }

//    fun getOddType(): String? {
//        return loginRepository.oddType
//    }

    fun getOddType(){
        loginRepository.mOddType.postValue(loginRepository.sOddType)
    }

}