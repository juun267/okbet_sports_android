package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.maintab.entity.HomeMenuBean
import org.cxct.sportlottery.util.Event

abstract class BaseBottomNavViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {
    val thirdGameCategory: LiveData<Event<HomeMenuBean?>>
        get() = _thirdGameCategory

    val settlementNotificationMsg
        get() = betInfoRepository.settlementNotificationMsg

    private val _thirdGameCategory = MutableLiveData<Event<HomeMenuBean?>>()


}