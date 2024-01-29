package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.myfavorite.MyFavoriteNotify
import org.cxct.sportlottery.network.myfavorite.PlayCate
import org.cxct.sportlottery.network.myfavorite.save.MyFavoriteBaseResult
import org.cxct.sportlottery.network.myfavorite.save.SaveMyFavoriteRequest
import org.cxct.sportlottery.network.myfavorite.query.SportMenuFavoriteResult
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.SingleLiveEvent
import org.cxct.sportlottery.util.TextUtil
import retrofit2.Response

object FavoriteRepository {

    val favorSportList: LiveData<List<String>>
        get() = _favorSportList
    private val _favorSportList = MutableLiveData<List<String>>()

    val favorLeagueList: LiveData<List<String>>
        get() = _favorLeagueList
    private val _favorLeagueList = MutableLiveData<List<String>>()

    val favorMatchList: LiveData<Set<String>>
        get() = _favorMatchList
    private val _favorMatchList = MutableLiveData<Set<String>>()

    val favoriteOutrightList: LiveData<List<String>>
        get() = _favoriteOutrightList
    private val _favoriteOutrightList = MutableLiveData<List<String>>()

    val favorPlayCateList: LiveData<List<PlayCate>>
        get() = _favorPlayCateList
    private val _favorPlayCateList = MutableLiveData<List<PlayCate>>()

    val favorNotify: LiveData<Event<MyFavoriteNotify>>
        get() = _favorNotify
    private val _favorNotify = MutableLiveData<Event<MyFavoriteNotify>>()

    val detailFavorNotify: SingleLiveEvent<Pair<String, Boolean>> = SingleLiveEvent()

    suspend fun getFavorite(): Response<SportMenuFavoriteResult> {
        val result = OneBoSportApi.favoriteService.getMyFavorite()

        if (result.isSuccessful) {
            result.body()?.t?.let {
                _favorSportList.postValue(TextUtil.split(it.sport ?: ""))
                _favorLeagueList.postValue(TextUtil.split(it.league ?: ""))
                _favorMatchList.postValue(TextUtil.splitSet(it.match))
                _favoriteOutrightList.postValue(TextUtil.split(it.outright ?: ""))
                _favorPlayCateList.postValue(it.playCate ?: listOf())
            }
        }

        return result
    }

    suspend fun pinFavorite(
        type: FavoriteType, content: String?, gameType: String?
    ): Response<MyFavoriteBaseResult> {
        val saveList = when (type) {
            FavoriteType.SPORT -> _favorSportList.value?.toMutableList() ?: mutableListOf()
            FavoriteType.LEAGUE -> _favorLeagueList.value?.toMutableList() ?: mutableListOf()
            FavoriteType.MATCH -> _favorMatchList.value?.toMutableList() ?: mutableListOf()
            FavoriteType.OUTRIGHT -> {
                _favoriteOutrightList.value?.toMutableList() ?: mutableListOf()
            }

            FavoriteType.PLAY_CATE -> {
                _favorPlayCateList.value?.transferSaveList(gameType) ?: mutableListOf()
            }
        }

        content?.let {
            val isContain = saveList.contains(content)
            detailFavorNotify.postValue(Pair(content, !isContain))
            when (isContain) {
                true -> {
                    saveList.remove(content)
                    _favorNotify.postValue(Event(MyFavoriteNotify(type, false)))
                }

                false -> {
                    saveList.add(content)
                    _favorNotify.postValue(Event(MyFavoriteNotify(type, true)))
                }
            }
        }

        val result = OneBoSportApi.favoriteService.saveMyFavorite(
            SaveMyFavoriteRequest(
                type.code, saveList.filter { it.isNotBlank() }.toList(), gameType
            )
        )

        if (result.isSuccessful) {
            result.body()?.t?.let {
                _favorSportList.postValue(TextUtil.split(it.sport ?: ""))
                _favorLeagueList.postValue(TextUtil.split(it.league ?: ""))
                _favorMatchList.postValue(TextUtil.splitSet(it.match))
                _favoriteOutrightList.postValue(TextUtil.split(it.outright ?: ""))
                _favorPlayCateList.postValue(it.playCate ?: listOf())
            }
        }

        return result
    }

    fun clearFavorite() {
        _favorSportList.postValue(listOf())
        _favorLeagueList.postValue(listOf())
        _favorMatchList.postValue(setOf())
        _favoriteOutrightList.postValue(listOf())
        _favorPlayCateList.postValue(listOf())
    }

    fun notifyFavorite(type: FavoriteType) {
        when (type) {
            FavoriteType.SPORT -> _favorSportList.postValue(_favorSportList.value ?: listOf())
            FavoriteType.LEAGUE -> _favorLeagueList.postValue(_favorLeagueList.value ?: listOf())
            FavoriteType.MATCH -> _favorMatchList.postValue(_favorMatchList.value ?: setOf())
            FavoriteType.OUTRIGHT -> _favoriteOutrightList.postValue(
                _favoriteOutrightList.value ?: listOf()
            )

            FavoriteType.PLAY_CATE -> _favorPlayCateList.postValue(
                _favorPlayCateList.value ?: listOf()
            )
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