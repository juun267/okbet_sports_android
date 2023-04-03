package org.cxct.sportlottery.ui.login.signUp.info

import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseViewModel

class RegisterInfoViewModel(
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
): BaseViewModel(loginRepository, betInfoRepository, infoCenterRepository){

}