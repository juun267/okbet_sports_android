package org.cxct.sportlottery.ui.maintab.games

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.common.extentions.asyncApi
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesHall
import org.cxct.sportlottery.net.live.OKLiveRepository
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.*

class OKGamesViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository
) : MainHomeViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {

    val providerResult: LiveData<OKGamesHall>
        get() = _providerresult
    private val _providerresult = MutableLiveData<OKGamesHall>()

    //游戏收藏结果
    val collectOkGamesResult: LiveData<Pair<Int, OKGameBean>>
        get() = _collectOkGamesResult
    private val _collectOkGamesResult = MutableLiveData<Pair<Int, OKGameBean>>()


    //jackpot数据
    val jackpotData: LiveData<String>
        get() = _jackpotData
    private val _jackpotData = MutableLiveData<String>()

    //游戏大厅数据
    val gameHall: LiveData<OKGamesHall>
        get() = _gameHall
    private val _gameHall = MutableLiveData<OKGamesHall>()

    //收藏游戏列表
    val collectList: LiveData<Pair<Boolean, List<OKGameBean>>> // 是否是通过服务端拉取-收藏列表
        get() = _collectList
    private val _collectList = MutableLiveData<Pair<Boolean, List<OKGameBean>>>()

    //最近游戏列表
    val recentPlay: LiveData<List<OKGameBean>>
        get() = _recentPlay
    private val _recentPlay = MutableLiveData<List<OKGameBean>>()

    val newRecentPlay: LiveData<OKGameBean>
        get() = _newRecentPlay
    private val _newRecentPlay = MutableLiveData<OKGameBean>()

    /**
     * 全部的赛事，map 类型
     */
    private var allGamesMap = mutableMapOf<Int, OKGameBean>()

    //游戏结果列表
    val gamesList: LiveData<Triple<Any, Int, List<OKGameBean>?>> // 请求id-总记录数-响应结果
        get() = _gamesList
    private val _gamesList = MutableLiveData<Triple<Any, Int, List<OKGameBean>?>>()

    private var isLoadingOKGamesHall = false
    /**
     * 获取游戏大厅数据（包含，厂商列表，收藏列表）
     */
    fun getOKGamesHall() {
        if (isLoadingOKGamesHall) {
            return
        }

        isLoadingOKGamesHall = true
        callApi({ OKGamesRepository.okGamesHall() }) {

            isLoadingOKGamesHall = false
            val data = it.getData() ?: return@callApi

            _gameHall.postValue(data)
            _collectList.postValue(Pair(true, data.collectList ?: listOf()))

            data.categoryList?.forEach {
                it.gameList?.forEach {
                    allGamesMap[it.id] = it
                }
            }

            if (data.firmList != null) {
                _providerresult.postValue(data)
            }
        }
    }

    /**
     * 获取jackpot奖池
     */
    fun getJackpotData(){
        callApi({ OKGamesRepository.okGamesJackpot() }) {
            it.getData()?.let {
                _jackpotData.postValue(it)
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
        page: Int,
        pageSize: Int
    ) = callApi({ OKGamesRepository.getOKGamesList(page, pageSize, null, categoryId, firmId) }) {

        _gamesList.value = Triple(requestTag, it.total, it.getData())
    }

    fun getFavoriteOKGames(
        requestTag: Any,
        page: Int = 1,
        pageSize: Int = 12
    ) = callApi({ OKGamesRepository.getOKGamesList(page, pageSize, null, null, null, true) }) {
        _gamesList.value = Triple(requestTag, it.total, it.getData())
    }

    /**
     * 收藏游戏
     */
    fun collectGame(gameData: OKGameBean,gameEntryType: String =GameEntryType.OKGAMES) =
        callApi({ OKGamesRepository.collectOkGames(gameData.id, !gameData.markCollect,gameEntryType) }) {
            if (!it.succeeded()) {
                ToastUtil.showToast(MultiLanguagesApplication.appContext, it.msg)
                return@callApi
            }

            gameData.markCollect = !gameData.markCollect
            _collectOkGamesResult.postValue(Pair(gameData.id, gameData))

            val markedGames = _collectList.value?.second?.toMutableList() ?: mutableListOf()
            if (gameData.markCollect) {
                markedGames.add(0, gameData)
                _collectList.postValue(Pair(false, markedGames))
                return@callApi
            }
            _collectList.postValue(Pair(false, markedGames.filter { it.id != gameData.id }.toList()))
        }


    /**
     * 进入OKgame游戏
     */
    fun requestEnterThirdGame(gameData: OKGameBean, baseActivity: BaseActivity<*>) {
        RecentDataManager.addRecent(RecentRecord(1, gameBean = gameData))
        requestEnterThirdGame(
            "${gameData.firmType}",
            "${gameData.gameCode}",
            "${gameData.gameCode}",
            "${gameData.gameType}",
            baseActivity
        )
    }

    /**
     * 获取最近游戏
     */
    fun getRecentPlay() {
        if (!LoginRepository.isLogined()) {  // 没登录不显示最近玩的游戏
            return
        }
        val ids = LoginRepository.getRecentPlayGameIds()
        val recentList = mutableListOf<OKGameBean>()
        ids.forEach {
            allGamesMap[it.toIntS(-1)]?.let {
                recentList.add(it.copy())
            }
        }
        recentList.reverse()
        _recentPlay.postValue(recentList)
    }

    /**
     * 记录最近游戏
     */
    fun addRecentPlay(okGameBean: OKGameBean) {
        val ids = LoginRepository.addRecentPlayGame(okGameBean.id.toString())
        val recentList = mutableListOf<OKGameBean>()
        ids.forEach {
            allGamesMap[it.toIntS(-1)]?.let {
                recentList.add(it.copy())
            }
        }

        recentList.reverse()
        _newRecentPlay.value = okGameBean
        _recentPlay.postValue(recentList)
    }

    fun searchGames(
        requestTag: Any,
        gameName: String,
        page: Int = 1,
        pageSize: Int = 15,
        categoryId: String? = null,
        firmId: String? = null,
    ) = callApi({
        OKGamesRepository.getOKGamesList(page,
            pageSize,
            gameName,
            categoryId,
            firmId)
    }) {
        _gamesList.postValue(Triple(requestTag, it.total, it.getData()))
    }

    val recordNewBetHttpOkGame: LiveData<List<RecordNewEvent>>
        get() = _recordNewBetHttpOkGame
    val recordResultWinsHttpOkGame: LiveData<List<RecordNewEvent>>
        get() = _recordResultWinsHttpOkGame

    private val _recordNewBetHttpOkGame = MutableLiveData<List<RecordNewEvent>>()
    private val _recordResultWinsHttpOkGame = MutableLiveData<List<RecordNewEvent>>()

    val sportFooterGames = SingleLiveEvent<List<OKGameBean>>()
    fun getOKGamesRecordNew() = callApi({ OKGamesRepository.getOKGamesRecordNew() }) {
        if (it.succeeded()) {
            _recordNewBetHttpOkGame.postValue(it.getData())
        }
    }

    fun getOKGamesRecordResult() = callApi({ OKGamesRepository.getOKGamesRecordResult() }) {
        if (it.succeeded()) {
            _recordResultWinsHttpOkGame.postValue(it.getData())
        }
    }
    fun getFooterGames(){
        viewModelScope.launch {
            var task1: Deferred<ApiResult<List<OKGameBean>>>? =null
            var task2: Deferred<ApiResult<List<OKGameBean>>>? =null
            task1 = asyncApi { OKGamesRepository.getOKLiveList(1, 12, GameEntryType.OKGAMES) }
            task2 = asyncApi { OKGamesRepository.getOKLiveList(1, 12, GameEntryType.OKLIVE) }
            val result1 = task1?.await()
            val result2 = task2?.await()
            val newList = mutableListOf<OKGameBean>()
            result1?.getData()?.let {
                newList.addAll(it)
            }
            result2?.getData()?.let {
                newList.addAll(it)
            }
            sportFooterGames.postValue(newList)
        }
    }

    /**
     * 获取游戏大厅数据（包含，厂商列表，收藏列表）
     */
    fun getOKLiveHall() {
        if (isLoadingOKGamesHall) {
            return
        }

        isLoadingOKGamesHall = true
        callApi({ OKLiveRepository.okLiveHall() }) {

            isLoadingOKGamesHall = false
            val data = it.getData() ?: return@callApi

            _gameHall.postValue(data)
            _collectList.postValue(Pair(true, data.collectList ?: listOf()))

            data.categoryList?.forEach {
                it.gameList?.forEach {
                    allGamesMap[it.id] = it
                }
            }

            if (data.firmList != null) {
                _providerresult.postValue(data)
            }
        }
    }

}