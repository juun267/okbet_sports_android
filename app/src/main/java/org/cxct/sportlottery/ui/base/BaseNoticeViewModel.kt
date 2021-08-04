package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.service.user_notice.UserNotice
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
    betInfoRepository,
    infoCenterRepository
) {
    val userInfo: LiveData<UserInfo?> = userInfoRepository.userInfo.asLiveData()

    fun setUserNoticeList(userNoticeList: List<UserNotice>) {
        infoCenterRepository.setUserNoticeList(userNoticeList)
    }
}