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
import org.cxct.sportlottery.network.sport.SaveMyFavoriteRequest
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.util.TextUtil


abstract class BaseFavoriteViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
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

    val favorSportList: LiveData<List<String>>
        get() = _favorSportList
    private val _favorSportList = MutableLiveData<List<String>>()

    val favorLeagueList: LiveData<List<String>>
        get() = _favorLeagueList
    private val _favorLeagueList = MutableLiveData<List<String>>()

    val favorMatchList: LiveData<List<String>>
        get() = _favorMatchList
    private val _favorMatchList = MutableLiveData<List<String>>()

    val favorMatchOddList: LiveData<List<LeagueOdd>>
        get() = _favorMatchOddList
    private val _favorMatchOddList = MutableLiveData<List<LeagueOdd>>()


    fun getFavorite() {
        if (isLogin.value != true) {
            _notifyLogin.postValue(true)
            return
        }

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.favoriteService.getMyFavorite()
            }

            result?.t?.let {
                _favorSportList.postValue(TextUtil.split(it.sport))
                _favorLeagueList.postValue(TextUtil.split(it.league))
                _favorMatchList.postValue(TextUtil.split(it.match))
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
                    leagueOdd.gameType = GameType.getGameType(gameType)
                }
                _favorMatchOddList.postValue(it)
            }
        }
    }

    fun clearFavorite() {
        _favorSportList.postValue(listOf())
        _favorLeagueList.postValue(listOf())
        _favorMatchList.postValue(listOf())
    }

    fun notifyFavorite(type: FavoriteType) {
        when (type) {
            FavoriteType.SPORT -> _favorSportList.postValue(_favorSportList.value)
            FavoriteType.LEAGUE -> _favorLeagueList.postValue(_favorLeagueList.value)
            FavoriteType.MATCH -> _favorMatchList.postValue((_favorMatchList.value))
            else -> {
                //TODO add other FavoriteType
            }
        }
    }

    fun pinFavorite(type: FavoriteType, content: String) {
        if (isLogin.value != true) {
            _notifyLogin.postValue(true)
            return
        }

        val saveList = when (type) {
            FavoriteType.SPORT -> _favorSportList.value?.toMutableList() ?: mutableListOf()
            FavoriteType.LEAGUE -> _favorLeagueList.value?.toMutableList() ?: mutableListOf()
            FavoriteType.MATCH -> _favorMatchList.value?.toMutableList() ?: mutableListOf()
            else -> mutableListOf()
        }

        when (saveList.contains(content)) {
            true -> saveList.remove(content)
            false -> saveList.add(content)
        }

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.favoriteService.saveMyFavorite(
                    SaveMyFavoriteRequest(type.code, saveList.toList())
                )
            }

            result?.t?.let {
                when (type) {
                    FavoriteType.SPORT -> _favorSportList.postValue(TextUtil.split(it.sport))
                    FavoriteType.LEAGUE -> _favorLeagueList.postValue(TextUtil.split(it.league))
                    FavoriteType.MATCH -> _favorMatchList.postValue(TextUtil.split(it.match))
                    else -> {
                        //TODO add other FavoriteType
                    }
                }
            }
        }
    }
}