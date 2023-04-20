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
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameTab
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

    //游戏收藏结果
    val collectOkGamesResult: LiveData<Pair<Int, OKGameBean>>
        get() = _collectOkGamesResult
    private val _collectOkGamesResult = MutableLiveData<Pair<Int, OKGameBean>>()


    //游戏大厅数据
    val gameHall: LiveData<OKGamesHall>
        get() = _gameHall
    private val _gameHall = MutableLiveData<OKGamesHall>()

    //收藏游戏列表
    val collectList: LiveData<List<OKGameBean>>
        get() = _collectList
    private val _collectList = MutableLiveData<List<OKGameBean>>()

    //最近游戏列表
    val recentPlay: LiveData<List<OKGameBean>>
        get() = _recentPlay
    private val _recentPlay = MutableLiveData<List<OKGameBean>>()

    /**
     * 全部的赛事，map 类型
     */
    private var allGamesMap = mutableMapOf<Int, OKGameBean>()

    //游戏结果列表
    val gamesList: LiveData<Triple<Any, Int, List<OKGameBean>?>> // 请求id-总记录数-响应结果
        get() = _gamesList
    private val _gamesList = MutableLiveData<Triple<Any, Int, List<OKGameBean>?>>()

    /**
     * 获取游戏大厅数据（包含，厂商列表，收藏列表）
     */
    fun getOKGamesHall() = callApi({ OKGamesRepository.okGamesHall() }) {
        it.getData()?.let {
            _gameHall.postValue(it)
            _collectList.postValue(it.collectList ?: listOf())
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

    /**
     * 获取游戏分页列表
     */
    fun getOKGamesList(
        requestTag: Any,
        categoryId: String?,
        firmId: String? = null,
        page: Int = 1,
        pageSize: Int = 12) = callApi({ OKGamesRepository.getOKGamesList(page, pageSize, null, categoryId, firmId) }) {

        _gamesList.value = Triple(requestTag, it.total, it.getData())
    }

    /**
     * 收藏游戏
     */
    fun collectGame(gameData: OKGameBean) =
        callApi({ OKGamesRepository.collectOkGames(gameData.id, !gameData.markCollect) }) {
            if (it.succeeded()) {
                gameData.markCollect = !gameData.markCollect
                _collectOkGamesResult.postValue(Pair(gameData.id, gameData))
            }
        }

    /**
     * 进入OKgame游戏
     */
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

    /**
     * 获取最近游戏
     */
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

    /**
     * 记录最近游戏
     */
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
    fun searchGames(requestTag: Any,
                    gameName: String,
                    page: Int = 1,
                    pageSize: Int = 15,
                    categoryId: String? = null,
                    firmId: String? = null,
    ) = callApi({ OKGamesRepository.getOKGamesList(page, pageSize, gameName, categoryId, firmId) }) {
        _gamesList.postValue(Triple(requestTag, it.total, it.getData()))
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