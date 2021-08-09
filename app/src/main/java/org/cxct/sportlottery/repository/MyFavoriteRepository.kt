package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.myfavorite.PlayCate
import org.cxct.sportlottery.network.myfavorite.save.MyFavoriteBaseResult
import org.cxct.sportlottery.network.myfavorite.save.SaveMyFavoriteRequest
import org.cxct.sportlottery.network.myfavorite.query.SportMenuFavoriteResult
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

    val favorPlayCateList: LiveData<List<PlayCate>>
        get() = _favorPlayCateList
    private val _favorPlayCateList = MutableLiveData<List<PlayCate>>()


    suspend fun getFavorite(): Response<SportMenuFavoriteResult> {
        val result = OneBoSportApi.favoriteService.getMyFavorite()

        if (result.isSuccessful) {
            result.body()?.t?.let {
                _favorSportList.postValue(TextUtil.split(it.sport ?: ""))
                _favorLeagueList.postValue(TextUtil.split(it.league ?: ""))
                _favorMatchList.postValue(TextUtil.split(it.match ?: ""))
                _favorPlayCateList.postValue(it.playCate ?: listOf())
            }
        }

        return result
    }

    suspend fun pinFavorite(
        type: FavoriteType,
        content: String?,
        gameType: String?
    ): Response<MyFavoriteBaseResult> {
        val saveList = when (type) {
            FavoriteType.SPORT -> _favorSportList.value?.toMutableList() ?: mutableListOf()
            FavoriteType.LEAGUE -> _favorLeagueList.value?.toMutableList() ?: mutableListOf()
            FavoriteType.MATCH -> _favorMatchList.value?.toMutableList() ?: mutableListOf()
            FavoriteType.PLAY_CATE -> {
                _favorPlayCateList.value?.transferSaveList(gameType) ?: mutableListOf()
            }
            else -> mutableListOf()
        }

        content?.let {
            when (saveList.contains(content)) {
                true -> saveList.remove(content)
                false -> saveList.add(content)
            }
        }

        val result = OneBoSportApi.favoriteService.saveMyFavorite(
            SaveMyFavoriteRequest(type.code, saveList.toList(), gameType)
        )

        if (result.isSuccessful) {
            result.body()?.t?.let {
                _favorSportList.postValue(TextUtil.split(it.sport ?: ""))
                _favorLeagueList.postValue(TextUtil.split(it.league ?: ""))
                _favorMatchList.postValue(TextUtil.split(it.match ?: ""))
                _favorPlayCateList.postValue(it.playCate ?: listOf())
            }
        }

        return result
    }

    fun clearFavorite() {
        _favorSportList.postValue(listOf())
        _favorLeagueList.postValue(listOf())
        _favorMatchList.postValue(listOf())
        _favorPlayCateList.postValue(listOf())
    }

    fun notifyFavorite(type: FavoriteType) {
        when (type) {
            FavoriteType.SPORT -> _favorSportList.postValue(_favorSportList.value)
            FavoriteType.LEAGUE -> _favorLeagueList.postValue(_favorLeagueList.value)
            FavoriteType.MATCH -> _favorMatchList.postValue((_favorMatchList.value))
            FavoriteType.PLAY_CATE -> _favorPlayCateList.postValue(_favorPlayCateList.value)
            else -> {
                //TODO add other FavoriteType
            }
        }
    }

    private fun List<PlayCate>.transferSaveList(gameType: String?): MutableList<String> {
        val playCateCode = this.find { it.gameType == gameType }?.code

        return if (!playCateCode.isNullOrEmpty()) {
            TextUtil.split(playCateCode)
        } else {
            mutableListOf()
        }
    }
}