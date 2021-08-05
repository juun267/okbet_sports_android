package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.sport.MyFavoriteBaseResult
import org.cxct.sportlottery.network.sport.SaveMyFavoriteRequest
import org.cxct.sportlottery.network.sport.SportMenuFavoriteResult
import org.cxct.sportlottery.util.TextUtil
import retrofit2.Response

class MyFavoriteRepository {

    val favorSportList: LiveData<List<String>>
        get() = _favorSportList
    private val _favorSportList = MutableLiveData<List<String>>()

    val favorLeagueList: LiveData<List<String>>
        get() = _favorLeagueList
    private val _favorLeagueList = MutableLiveData<List<String>>()

    val favorMatchList: LiveData<List<String>>
        get() = _favorMatchList
    private val _favorMatchList = MutableLiveData<List<String>>()


    suspend fun getFavorite(): Response<SportMenuFavoriteResult> {
        val result = OneBoSportApi.favoriteService.getMyFavorite()

        if (result.isSuccessful) {
            result.body()?.t?.let {
                _favorSportList.postValue(TextUtil.split(it.sport))
                _favorLeagueList.postValue(TextUtil.split(it.league))
                _favorMatchList.postValue(TextUtil.split(it.match))
            }
        }

        return result
    }

    suspend fun pinFavorite(type: FavoriteType, content: String?): Response<MyFavoriteBaseResult> {
        val saveList = when (type) {
            FavoriteType.SPORT -> _favorSportList.value?.toMutableList() ?: mutableListOf()
            FavoriteType.LEAGUE -> _favorLeagueList.value?.toMutableList() ?: mutableListOf()
            FavoriteType.MATCH -> _favorMatchList.value?.toMutableList() ?: mutableListOf()
            else -> mutableListOf()
        }

        content?.let {
            when (saveList.contains(content)) {
                true -> saveList.remove(content)
                false -> saveList.add(content)
            }
        }

        val result = OneBoSportApi.favoriteService.saveMyFavorite(
            SaveMyFavoriteRequest(type.code, saveList.toList())
        )

        if (result.isSuccessful) {
            result.body()?.t?.let {
                _favorSportList.postValue(TextUtil.split(it.sport))
                _favorLeagueList.postValue(TextUtil.split(it.league))
                _favorMatchList.postValue(TextUtil.split(it.match))
            }
        }

        return result
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
}