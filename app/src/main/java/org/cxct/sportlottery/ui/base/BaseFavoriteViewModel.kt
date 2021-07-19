package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.FavoriteType
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
            }
        }
    }

    fun clearFavorite() {
        _favorSportList.postValue(listOf())
        _favorLeagueList.postValue(listOf())
    }

    fun notifyFavorite(type: FavoriteType) {
        when (type) {
            FavoriteType.SPORT -> _favorSportList.postValue(_favorSportList.value)
            FavoriteType.LEAGUE -> _favorLeagueList.postValue(_favorLeagueList.value)
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
                    else -> {
                        //TODO add other FavoriteType
                    }
                }
            }
        }
    }
}