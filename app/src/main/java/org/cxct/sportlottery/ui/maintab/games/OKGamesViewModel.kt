package org.cxct.sportlottery.ui.maintab.games

import android.app.Application
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.games.OKGamesRepository
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

    suspend fun okGamesHall(): ApiResult<Any?> {
        return OKGamesRepository.okGamesApi.getOKGamesHall(OKGamesRepository.paramDevice())
    }

    fun getOKGamesHall() = callApi({ OKGamesRepository.okGamesHall() }) {

    }

    fun collectGame(gameId: Int, isCollected: Boolean) = callApi({ OKGamesRepository.collectOkGames(gameId, !isCollected) }) {

    }


}