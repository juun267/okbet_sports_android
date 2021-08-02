package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.sport.MyFavoriteMatchRequest
import org.cxct.sportlottery.repository.*


abstract class BaseFavoriteViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    private val myFavoriteRepository: MyFavoriteRepository
) : BaseNoticeViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository
) {
    //TODO add notify login ui to activity/fragment
    val notifyLogin: LiveData<Boolean>
        get() = _notifyLogin
    private val _notifyLogin = MutableLiveData<Boolean>()

    val favorMatchOddList: LiveData<List<LeagueOdd>>
        get() = _favorMatchOddList
    private val _favorMatchOddList = MutableLiveData<List<LeagueOdd>>()

    val favorSportList = myFavoriteRepository.favorSportList

    val favorLeagueList = myFavoriteRepository.favorLeagueList

    val favorMatchList = myFavoriteRepository.favorMatchList


    fun getFavorite() {
        if (isLogin.value != true) {
            _notifyLogin.postValue(true)
            return
        }

        viewModelScope.launch {
            doNetwork(androidContext) {
                myFavoriteRepository.getFavorite()
            }
        }
    }

    fun getFavoriteMatch(gameType: String?, playCateMenu: String?) {
        if (isLogin.value != true) {
            _notifyLogin.postValue(true)
            return
        }

        if (gameType == null || playCateMenu == null) {
            return
        }

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.favoriteService.getMyFavoriteMatch(
                    MyFavoriteMatchRequest(gameType, playCateMenu)
                )
            }

            result?.rows?.let {
                it.forEach { leagueOdd ->
                    leagueOdd.apply {
                        this.gameType = GameType.getGameType(gameType)
                        this.matchOdds.forEach { matchOdd ->
                            matchOdd.matchInfo?.isFavorite = true
                        }
                    }
                }
                _favorMatchOddList.postValue(it)
            }
        }
    }

    fun clearFavorite() {
        myFavoriteRepository.clearFavorite()
    }

    fun notifyFavorite(type: FavoriteType) {
        myFavoriteRepository.notifyFavorite(type)
    }

    fun pinFavorite(
        type: FavoriteType,
        content: String?,
    ) {
        if (isLogin.value != true) {
            _notifyLogin.postValue(true)
            return
        }

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                myFavoriteRepository.pinFavorite(type, content)
            }

            result?.t?.let {
                if (content == null) return@let

                when (type) {
                    FavoriteType.MATCH -> {
                        _favorMatchOddList.postValue(
                            _favorMatchOddList.value?.removeFavorMatchOdd(
                                content
                            )?.removeFavorLeague()
                        )
                    }

                    else -> {
                    }
                }
            }
        }
    }

    private fun List<LeagueOdd>.removeFavorMatchOdd(matchId: String): List<LeagueOdd> {
        this.forEach { leagueOdd ->
            leagueOdd.matchOdds.remove(
                leagueOdd.matchOdds.find { matchOdd ->
                    matchOdd.matchInfo?.id == matchId
                }
            )
        }

        return this
    }

    private fun List<LeagueOdd>.removeFavorLeague(): List<LeagueOdd> {
        val list = this.toMutableList()

        list.remove(list.find {
            it.matchOdds.isNullOrEmpty()
        })

        return list.toList()
    }
}