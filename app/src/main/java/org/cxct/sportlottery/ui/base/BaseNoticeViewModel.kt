package org.cxct.sportlottery.ui.base

import android.app.Application
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository

abstract class BaseNoticeViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseOddButtonViewModel(
    androidContext,
    loginRepository,
    userInfoRepository,
    betInfoRepository,
    infoCenterRepository
) {

}