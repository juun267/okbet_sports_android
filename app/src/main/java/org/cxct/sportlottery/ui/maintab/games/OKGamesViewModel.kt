package org.cxct.sportlottery.ui.maintab.games

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesHall
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.KvUtils

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
    companion object {
        const val KEY_RECENT_PLAY = "recentPlay"
    }


    val providerResult: LiveData<OKGamesHall>
        get() = _providerresult
    private val _providerresult = MutableLiveData<OKGamesHall>()

    val collectOkGamesResult: LiveData<Pair<Int, OKGameBean>>
        get() = _collectOkGamesResult
    private val _collectOkGamesResult = MutableLiveData<Pair<Int, OKGameBean>>()

    val gamesList: LiveData<List<OKGameBean>>
        get() = _gamesList
    private val _gamesList = MutableLiveData<List<OKGameBean>>()

    val gameHall: LiveData<OKGamesHall>
        get() = _gameHall
    private val _gameHall = MutableLiveData<OKGamesHall>()

    val collectList: LiveData<List<OKGameBean>>
        get() = _collectList
    private val _collectList = MutableLiveData<List<OKGameBean>>()

    val recentPlay: LiveData<List<OKGameBean>>
        get() = _recentPlay
    private val _recentPlay = MutableLiveData<List<OKGameBean>>()

    /**
     * 全部的赛事，map 类型
     */
    private var allGamesMap = mutableMapOf<Int, OKGameBean>()

    val searchResult: LiveData<Pair<String, List<OKGameBean>?>>
        get() = _searchResult
    private val _searchResult = MutableLiveData<Pair<String, List<OKGameBean>?>>()

    fun getOKGamesHall() = callApi({ OKGamesRepository.okGamesHall() }) {
        it.getData()?.let {
            _gameHall.postValue(it)
            _collectList.postValue(it.collectList)
            it.categoryList?.forEach {
                it.gameList?.forEach {
                    allGamesMap[it.id] = it
                }
            }
            if (it.firmList != null) {
                _providerresult.postValue(it)
            }
        }
    }

    fun getOKGamesList(
        page: Int,
        gameName: String?,
        categoryId: String?,
        firmId: String?,
    ) = callApi({ OKGamesRepository.getOKGamesList(page, 12, gameName, categoryId, firmId) }) {
        _gamesList.postValue(it.getData() ?: listOf())
    }

    fun collectGame(gameData: OKGameBean) =
        callApi({ OKGamesRepository.collectOkGames(gameData.id, !gameData.markCollect) }) {
            gameData.markCollect = !gameData.markCollect
            _collectOkGamesResult.postValue(Pair(gameData.id, gameData))
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

    fun getRecentPlay() {
        val ids = KvUtils.decodeString(KEY_RECENT_PLAY)
        if (ids.isNotEmpty()) {
            var playList = ids.split(",").toMutableList()
            val recentList = mutableListOf<OKGameBean>()
            playList.forEach {
                allGamesMap[it.toIntS(-1)]?.let {
                    recentList.add(it)
                }
            }
            _recentPlay.postValue(recentList)
        }
    }

    fun addRecentPlay(gameId: String) {
        val ids = KvUtils.decodeString(KEY_RECENT_PLAY)
        var playList = if (ids.isNotEmpty()) ids.split(",").toMutableList() else mutableListOf()
        playList.remove(gameId)
        playList.add(gameId)
        if (playList.size > 12) {
            playList.subList(0, 12)
        }
        KvUtils.put(KEY_RECENT_PLAY, playList.joinToString(separator = ","))
        val recentList = mutableListOf<OKGameBean>()
        playList.forEach {
            allGamesMap[it.toIntS(-1)]?.let {
                recentList.add(it)
            }
        }
        _recentPlay.postValue(recentList)
    }
    fun searchGames(gameName: String,
                    page: Int = 1,
                    pageSize: Int = 15,
                    categoryId: String? = null,
                    firmId: String? = null,
    ) = callApi({ OKGamesRepository.getOKGamesList(page, pageSize, gameName, categoryId, firmId) }) {
        _searchResult.postValue(Pair(gameName, it.getData()))
    }

    val recordNewHttp: LiveData<List<RecordNewEvent>>
        get() = _recordNewHttp
    val recordResultHttp: LiveData<List<RecordNewEvent>>
        get() = _recordResultHttp

    private val _recordNewHttp = MutableLiveData<List<RecordNewEvent>>()
    private val _recordResultHttp = MutableLiveData<List<RecordNewEvent>>()
    fun getOKGamesRecordNew() = callApi({ OKGamesRepository.getOKGamesRecordNew() }) {
        _recordNewHttp.postValue(it.getData())
    }
    fun getOKGamesRecordResult() = callApi({ OKGamesRepository.getOKGamesRecordResult() }) {
        _recordResultHttp.postValue(it.getData())
    }

}