package org.cxct.sportlottery.ui.maintab.games

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesHall
import org.cxct.sportlottery.net.live.OKLiveRepository
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.ToastUtil

class OKLiveViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
    sportMenuRepository: SportMenuRepository,
) : MainHomeViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository,
    sportMenuRepository,
) {

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

    /**
     * 获取游戏分页列表
     */
    fun getOKGamesList(
        requestTag: Any,
        categoryId: String?,
        firmId: String? = null,
        page: Int,
        pageSize: Int
    ) = callApi({ OKLiveRepository.getOKLivesList(page, pageSize, null, categoryId, firmId) }) {

        _gamesList.value = Triple(requestTag, it.total, it.getData())
    }

    fun getFavoriteOKGames(
        requestTag: Any,
        page: Int = 1,
        pageSize: Int = 12
    ) = callApi({ OKLiveRepository.getOKLivesList(page, pageSize, null, null, null, true) }) {
        _gamesList.value = Triple(requestTag, it.total, it.getData())
    }

    /**
     * 收藏游戏
     */
    fun collectGame(gameData: OKGameBean) =
        callApi({ OKLiveRepository.collectOkLive(gameData.id, !gameData.markCollect) }) {
            if (!it.succeeded()) {
                ToastUtil.showToast(MultiLanguagesApplication.appContext, it.msg)
                return@callApi
            }

            gameData.markCollect = !gameData.markCollect
            _collectOkGamesResult.postValue(Pair(gameData.id, gameData))

            val markedGames = _collectList.value?.second?.toMutableList() ?: mutableListOf()
            if (gameData.markCollect) {
                markedGames.add(0, gameData)
                _collectList.value = Pair(false, markedGames)
                return@callApi
            }

            _collectList.value = Pair(false, markedGames.filter { it.id != gameData.id }.toList())
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
        OKLiveRepository.getOKLivesList(page,
            pageSize,
            gameName,
            categoryId,
            firmId)
    }) {
        _gamesList.postValue(Triple(requestTag, it.total, it.getData()))
    }

    val recordNewBetHttpOkLive: LiveData<List<RecordNewEvent>>
        get() = _recordNewBetHttpOkLive
    val recordResultWinsHttpOkLive: LiveData<List<RecordNewEvent>>
        get() = _recordResultWinsHttpOkLive

    private val _recordNewBetHttpOkLive = MutableLiveData<List<RecordNewEvent>>()
    private val _recordResultWinsHttpOkLive = MutableLiveData<List<RecordNewEvent>>()
    fun getOKGamesRecordNew() = callApi({ OKLiveRepository.getOKLiveRecordNew()}) {
        if (it.succeeded()) {
            _recordNewBetHttpOkLive.postValue(it.getData())
        }
    }

    fun getOKGamesRecordResult() = callApi({ OKLiveRepository.getOKLiveRecordResult() }) {
        if (it.succeeded()) {
            _recordResultWinsHttpOkLive.postValue(it.getData())
        }
    }

}