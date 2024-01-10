package org.cxct.sportlottery.ui.maintab

import android.app.Application
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel

class MainTabViewModel(
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
    val showBetUpperLimit = betInfoRepository.showBetUpperLimit
    val showBetBasketballUpperLimit = betInfoRepository.showBetBasketballUpperLimit
}