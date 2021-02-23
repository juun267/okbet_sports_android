package org.cxct.sportlottery.ui.base

import org.cxct.sportlottery.network.service.user_notice.UserNotice
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository

abstract class BaseNoticeViewModel(
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    val infoCenterRepository: InfoCenterRepository
) : BaseOddButtonViewModel(loginRepository, betInfoRepository) {

    fun setUserNoticeList(userNoticeList: List<UserNotice>) {
        infoCenterRepository.setUserNoticeList(userNoticeList)
    }
}