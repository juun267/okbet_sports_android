package org.cxct.sportlottery.ui.maintab.menu.viewmodel

import android.app.Application
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.MyFavoriteRepository
import org.cxct.sportlottery.repository.SportMenuRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseSocketViewModel

class SportLeftMenuViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
    private val sportMenuRepository: SportMenuRepository, )
    : BaseSocketViewModel(androidContext, userInfoRepository, loginRepository, betInfoRepository, infoCenterRepository, favoriteRepository) {



}