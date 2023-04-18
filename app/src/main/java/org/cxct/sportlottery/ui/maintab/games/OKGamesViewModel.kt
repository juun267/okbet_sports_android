package org.cxct.sportlottery.ui.maintab.games

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesHall
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.entity.EnterThirdGameResult
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
        callApi({ OKGamesRepository.collectOkGames(gameId, markCollect) }) {
            _collectOkGamesResult.postValue(Pair(gameId, markCollect))
        }

    fun requestEnterThirdGame(gameData: OKGameBean, baseFragment: BaseFragment<*>) {
        if (gameData == null) {
            _enterThirdGameResult.postValue(
                Pair("${gameData.firmCode}", EnterThirdGameResult(
                    resultType = EnterThirdGameResult.ResultType.FAIL,
                    url = null,
                    errorMsg = androidContext.getString(R.string.hint_game_maintenance)
                ))

            )

            return
        }

        requestEnterThirdGame("${gameData.firmType}",
            "${gameData.gameCode}",
            "${gameData.gameCode}",
            baseFragment)
    }
}