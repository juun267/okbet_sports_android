package org.cxct.sportlottery.ui.maintab.games

import android.app.Application
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel

class OKGamesViewModel(androidContext: Application,
                       userInfoRepository: UserInfoRepository,
                       loginRepository: LoginRepository,
                       betInfoRepository: BetInfoRepository,
                       infoCenterRepository: InfoCenterRepository,
                       favoriteRepository: MyFavoriteRepository,
                       sportMenuRepository: SportMenuRepository
): MainHomeViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository,
    sportMenuRepository
)  {



}