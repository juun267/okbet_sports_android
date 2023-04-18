package org.cxct.sportlottery.ui.maintab.games

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesHall
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

    val gamesList: LiveData<List<OKGameBean>>
        get() = _gamesList
    private val _gamesList = MutableLiveData<List<OKGameBean>>()

    val gameHall: LiveData<OKGamesHall>
        get() = _gameHall
    private val _gameHall = MutableLiveData<OKGamesHall>()

    val searchResult: LiveData<Pair<String, List<OKGameBean>?>>
        get() = _searchResult
    private val _searchResult = MutableLiveData<Pair<String, List<OKGameBean>?>>()

    fun getOKGamesHall() = callApi({ OKGamesRepository.okGamesHall() }) {
        _gameHall.postValue(it.getData())
    }

    fun getOKGamesList(
        page: Int,
        gameName: String?,
        categoryId: String?,
        firmId: String?,
    ) = callApi({ OKGamesRepository.getOKGamesList(page, 12, gameName, categoryId, firmId) }) {
        _gamesList.postValue(it.getData() ?: listOf())
    }

    fun collectGame(gameId: Int, markCollect: Boolean) =
        callApi({ OKGamesRepository.collectOkGames(gameId, !markCollect) }) {
            _collectOkGamesResult.postValue(Pair(gameId, it.succeeded()) as Pair<Int, Boolean>?)
        }

    fun searchGames(gameName: String,
                    page: Int = 1,
                    pageSize: Int = 15,
                    categoryId: String? = null,
                    firmId: String? = null,
    ) = callApi({ OKGamesRepository.getOKGamesList(page, pageSize, gameName, categoryId, firmId) }) {
        _searchResult.postValue(Pair(gameName, it.getData()))
    }

}