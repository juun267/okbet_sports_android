package org.cxct.sportlottery.ui.maintab

import android.app.Application
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel

class MainViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository
) : MainHomeViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository,
) {
    val token
        get() = loginRepository.token

    val userId = loginRepository.userId



}