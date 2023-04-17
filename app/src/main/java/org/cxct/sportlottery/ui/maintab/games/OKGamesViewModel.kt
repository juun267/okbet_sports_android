package org.cxct.sportlottery.ui.maintab.games

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel

class OKGamesViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
    sportMenuRepository: SportMenuRepository,
): MainHomeViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository,
    sportMenuRepository,
) {
    val collectOkGamesResult: LiveData<Pair<Int, Boolean>>
        get() = _collectOkGamesResult
    private val _collectOkGamesResult = MutableLiveData<Pair<Int, Boolean>>()


    fun getOKGamesHall() = callApi({ OKGamesRepository.okGamesHall() }) {

    }

    fun collectGame(gameId: Int, isCollected: Boolean) = callApi({ OKGamesRepository.collectOkGames(gameId, !isCollected) }) {
        _collectOkGamesResult.postValue(Pair(gameId, it.succeeded()))
    }


}