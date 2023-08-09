package org.cxct.sportlottery.ui.sport.list

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.sport.SportRepository
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.QuickPlayCate
import org.cxct.sportlottery.network.myfavorite.match.MyFavoriteMatchRequest
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.*
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListRequest
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListResult
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.TimeUtil.HM_FORMAT
import org.cxct.sportlottery.util.TimeUtil.YMDE_FORMAT
import kotlin.collections.ArrayList

open class SportListViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    myFavoriteRepository: MyFavoriteRepository,
    private val sportMenuRepository: SportMenuRepository,
) : BaseBottomNavViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    myFavoriteRepository,
) {

    val oddsListGameHallResult: LiveData<Event<OddsListResult?>>
        get() = _oddsListGameHallResult
    private val _oddsListGameHallResult = SingleLiveEvent<Event<OddsListResult?>>()

    private val _sportMenuResult = SingleLiveEvent<SportMenuResult?>()

    val outrightList = MutableLiveData<Event<OutrightOddsListResult?>>()

    fun loadFavoriteGameList() {
        if (!LoginRepository.isLogined()) {
            return
        }

        doRequest(androidContext, {
            OneBoSportApi.favoriteService.getMyFavoriteQueryAll(MyFavoriteMatchRequest(null, MenuCode.MAIN.code))
        }) { result ->

            if (result?.success != true) {
                sportTypeMenuData.value = Triple(listOf(), false, "${result?.msg}")
                return@doRequest
            }

            val favoriteList = result.rows
            if (favoriteList.isNullOrEmpty()) {
                sportTypeMenuData.value = Triple(listOf(), true, "")
                return@doRequest
            }

            val gameItems = mutableListOf<Item>()

            favoriteList.forEach {
                val item = Item(it.gameType,
                    GameType.getGameTypeString(androidContext, it.gameType),
                    it.leagueOddsList.size,
                    0)
                item.leagueOddsList = it.leagueOddsList
                gameItems.add(item)

                val leagueOddList = it.leagueOddsList

                leagueOddList.sortOdds()
                leagueOddList.getPlayCateNameMap()
                leagueOddList.forEach { leagueOdd ->
                    leagueOdd.gameType =
                        GameType.getGameType(leagueOdd.matchOdds[0].matchInfo?.gameType!!)
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.matchInfo?.isFavorite = true
                    }

                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.setupOddDiscountFixed()
                        matchOdd.matchInfo?.let { matchInfo ->

                            matchInfo.startDateDisplay =
                                TimeUtil.timeFormat(matchInfo.startTime, "MM/dd")
                            matchInfo.startTimeDisplay =
                                TimeUtil.timeFormat(matchInfo.startTime, "HH:mm")
                            matchInfo.remainTime = TimeUtil.getRemainTime(matchInfo.startTime)

                        }

                        // 过滤掉赔率为空掉对象
                        matchOdd.oddsMap?.let { oddsMap ->
                            oddsMap.forEach {
                                oddsMap[it.key] =
                                    it.value?.filter { null != it }?.toMutableList() ?: mutableListOf()
                            }
                        }
                    }
                }
            }

            sportTypeMenuData.value = Triple(gameItems, true, "")
        }
    }

    private fun dealLeagueList(playCateMenuCode: String, matchType: String, leagueList: List<LeagueOdd>,filterMatchIdList: List<String>) {
        leagueList.updateMatchType()
        leagueList.forEach { leagueOdd ->
            var iterator = leagueOdd.matchOdds.iterator()
            while (iterator.hasNext()) {
                val matchOdd = iterator.next()
                if (matchOdd.matchInfo == null||(filterMatchIdList.isNotEmpty()&&!filterMatchIdList.contains(matchOdd.matchInfo.id))) { // 过滤掉matchInfo为空的脏数据
                    iterator.remove()
                } else {
                    matchOdd.sortOddsMap()
                    val matchInfo = matchOdd.matchInfo
                    matchInfo.startDateDisplay = TimeUtil.timeFormat(matchInfo.startTime, "MM/dd")
                    matchOdd.matchInfo.startTimeDisplay = TimeUtil.timeFormat(matchInfo.startTime, "HH:mm")
                    matchInfo.remainTime = TimeUtil.getRemainTime(matchInfo.startTime)

                    matchOdd.oddsMap?.forEach { map ->
                        map.value?.updateOddSelectState()
                    }

//                    matchOdd.setupPlayCate()
//                    matchOdd.refactorPlayCode() //改成在OddButtonPagerAdapter處理
                    matchOdd.sortOdds()

                    matchOdd.setupOddDiscount()
                    matchOdd.updateOddStatus()
                    if (playCateMenuCode != MenuCode.CS.code) {
                        matchOdd.oddsSort = PlayCateMenuFilterUtils.filterOddsSort(matchOdd.matchInfo?.gameType, MenuCode.MAIN.code)
                    }

                    matchOdd.filterQuickPlayCate(matchType)
                    //波胆的数据获取方式
                    if (matchType == MatchType.CS.postValue) {
                        matchOdd.playCateNameMap = leagueOdd.playCateNameMap
                    }

                }
            }
        }

        leagueList.getPlayCateNameMap(matchType)
    }

    fun getGameHallList(
        matchType: MatchType,
        gameType: String,
        selectLeagueIdList: ArrayList<String> = arrayListOf(),
        selectMatchIdList: ArrayList<String> = arrayListOf()
    ) {

        when (matchType) {
            MatchType.IN_PLAY -> {
                getOddsList(
                    gameType = gameType,
                    matchType.postValue,
                    leagueIdList = selectLeagueIdList,
                    matchIdList = selectMatchIdList,
                )
            }
            MatchType.AT_START -> {
                getOddsList(
                    gameType = gameType,
                    matchType.postValue,
                    TimeUtil.getAtStartTimeRangeParams(),
                    leagueIdList = selectLeagueIdList,
                    matchIdList = selectMatchIdList,
                )
            }
            MatchType.TODAY -> {
                getOddsList(
                    gameType = gameType,
                    matchType.postValue,
                    leagueIdList = selectLeagueIdList,
                    matchIdList = selectMatchIdList,
                )
            }
            MatchType.EARLY -> {
                getOddsList(
                    gameType = gameType,
                    matchType.postValue,
                    leagueIdList = selectLeagueIdList,
                    matchIdList = selectMatchIdList,
                )
            }
            MatchType.CS -> {
                getOddsList(
                    gameType = gameType,
                    matchType.postValue,
                    leagueIdList = selectLeagueIdList,
                    matchIdList = selectMatchIdList,
                )
            }
            MatchType.PARLAY -> {
                getOddsList(
                    gameType = gameType,
                    matchType.postValue,
                    leagueIdList = selectLeagueIdList,
                    matchIdList = selectMatchIdList,
                )

            }

            MatchType.END_SCORE -> {
                getOddsList(
                    gameType = gameType,
                    matchType.postValue,
                    leagueIdList = selectLeagueIdList,
                    matchIdList = selectMatchIdList,
                )

            }
            MatchType.OUTRIGHT -> {
                getOutrightOddsList(
                    gameType,
                    leagueIdList = selectLeagueIdList,
                    selectMatchIdList = selectMatchIdList
                )
            }
            MatchType.MY_EVENT -> {
                getOddsList(
                    gameType = gameType,
                    matchType.postValue,
                    leagueIdList = selectLeagueIdList,
                    matchIdList = selectMatchIdList,
                )

            }
            else -> {
            }
        }
    }

    fun switchGameType(matchType: MatchType, item: Item,selectLeagueIdList: ArrayList<String>,selectMatchIdList: ArrayList<String>) {
        if (jobSwitchGameType?.isActive == true) {
            jobSwitchGameType?.cancel()
        }
        //視覺上需要優先跳轉 tab
        _sportMenuResult.value?.updateSportSelectState(matchType, item.code)
        jobSwitchGameType = viewModelScope.launch {
            getGameHallList(matchType, item.code, selectLeagueIdList,selectMatchIdList)
        }
    }


    private lateinit var oddsListRequestTag: Any
    private var jobSwitchGameType: Job? = null
    private var jobGetOddsList: Job? = null
    private fun getOddsList(
        gameType: String,
        matchType: String,
        timeRangeParams: TimeRangeParams? = null,
        leagueIdList: List<String>? = null,
        matchIdList: List<String>? = null,
    ) {
        val requestTag = Any()
        oddsListRequestTag = requestTag
        var currentTimeRangeParams: TimeRangeParams? = null
        when (matchType) {
            MatchType.IN_PLAY.postValue, MatchType.AT_START.postValue, MatchType.OTHER.postValue -> {
                currentTimeRangeParams = timeRangeParams
            }
            MatchType.TODAY.postValue, MatchType.CS.postValue, MatchType.EARLY.postValue, MatchType.PARLAY.postValue -> {
                currentTimeRangeParams = timeRangeParams
            }
            else -> { // 特殊賽事要給特殊代碼 Ex: matchType: "sc:QAtest"

            }
        }

        val emptyFilter = { list: List<String>? ->
            if (list.isNullOrEmpty()) listOf<String>() else list
        }
        var startTime = currentTimeRangeParams?.startTime ?: ""
        var endTime = currentTimeRangeParams?.endTime ?: ""
        var playCateMenuCode = MenuCode.MAIN.code
        if (matchType == MatchType.CS.postValue) {
            playCateMenuCode = MenuCode.CS.code
        } else if (matchType == MatchType.END_SCORE.postValue) {
            playCateMenuCode = MatchType.END_SCORE.postValue
        }
        if (jobGetOddsList?.isActive == true) {
            jobGetOddsList?.cancel()
        }

        jobGetOddsList = viewModelScope.launch(Dispatchers.IO) {
            var result: OddsListResult? = null
            if (matchType == MatchType.IN_PLAY.postValue && gameType == GameType.ALL.key) {
                doNetwork(androidContext) {
                    OneBoSportApi.oddsService.getInPlayAllList(
                        OddsListRequest(
                            gameType,
                            matchType,
                            leagueIdList = emptyFilter(leagueIdList),
                            startTime = startTime,
                            endTime = endTime,
                            playCateMenuCode = playCateMenuCode
                        )
                    )
                }?.let {
                    var leagueOdds = mutableListOf<LeagueOdd>().apply {
                        it.OddsListDataList.forEach {
                            it?.leagueOdds?.let {
                                addAll(it)
                            }
                        }
                    }
                    result = OddsListResult(
                        it.code, it.msg, it.success,
                        OddsListData(leagueOdds)
                    )
                }
            } else {
                result = doNetwork(androidContext) {
                    OneBoSportApi.oddsService.getOddsList(
                        OddsListRequest(
                            gameType,
                            matchType,
                            leagueIdList = emptyFilter(leagueIdList),
                            startTime = startTime,
                            endTime = endTime,
                            playCateMenuCode = playCateMenuCode
                        )
                    )
                }
            }

            if (requestTag != oddsListRequestTag) {
                return@launch
            }

            result?.oddsListData?.leagueOdds?.let { dealLeagueList(playCateMenuCode, matchType, it,emptyFilter(matchIdList)) }
            _oddsListGameHallResult.postValue(Event(result, gameType))
            notifyFavorite(FavoriteType.MATCH)
        }
    }

    private lateinit var outrightOddsListRequestTag: Any
    private fun getOutrightOddsList(gameType: String, leagueIdList: List<String>? = null, selectMatchIdList: ArrayList<String>? = null) {
        val requestTag = Any()
        outrightOddsListRequestTag = requestTag
        viewModelScope.launch(Dispatchers.IO) {
            val result = doNetwork(androidContext) {
                OneBoSportApi.outrightService.getOutrightOddsList(

                    if (leagueIdList.isNullOrEmpty()) {
                        OutrightOddsListRequest(
                            gameType,
                            matchType = MatchType.OUTRIGHT.postValue,
                            matchIdList = selectMatchIdList
                        )
                    } else {
                        OutrightOddsListRequest(
                            gameType,
                            matchType = MatchType.OUTRIGHT.postValue,
                            leagueIdList = leagueIdList,
                            matchIdList = selectMatchIdList
                        )
                    }
                )
            }

            if (requestTag != outrightOddsListRequestTag) {
                return@launch
            }

            result?.outrightOddsListData?.leagueOdds?.forEach { leagueOdd ->
                leagueOdd.matchOdds?.forEach { matchOdd ->
                    matchOdd?.oddsMap?.values?.forEach { oddList ->
                        oddList?.updateOddSelectState()
                    }

                    matchOdd?.setupOddDiscount()
                    //20220617 冠軍的排序字串切割方式不同, 跟進iOS odds給什麼就顯示什麼
//                    matchOdd?.setupPlayCate()
                    //20220613 冠軍的排序字串切割方式不同, 跟進iOS此處無重新排序
//                    matchOdd?.sortOdds()

                    matchOdd?.startDate =
                        TimeUtil.timeFormat(matchOdd?.matchInfo?.endTime, YMDE_FORMAT)
                    matchOdd?.startTime =
                        TimeUtil.timeFormat(matchOdd?.matchInfo?.endTime, HM_FORMAT)

                }
            }

            if (requestTag != outrightOddsListRequestTag) {
                return@launch
            }
            outrightList.postValue(Event(result, gameType))
        }
    }


    /**
     * 检查赛事是否到了开赛时间，并且更新滚球状态
     */
    private fun List<LeagueOdd>.updateMatchType() {
        forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->

                matchOdd.matchInfo?.isInPlay =
                    System.currentTimeMillis() > matchOdd.matchInfo?.startTime ?: 0

                matchOdd.matchInfo?.isAtStart =
                    TimeUtil.isTimeAtStart(matchOdd.matchInfo?.startTime)
            }
        }
    }

    /**
     * 根據賽事的oddsSort將盤口重新排序
     */
    private fun MatchOdd.sortOdds() {
        val sortOrder = this.oddsSort?.split(",")
        val oddsMap = this.oddsMap?.toSortedMap(compareBy<String> {
            val oddsIndex = sortOrder?.indexOf(it.split(":")[0])
            oddsIndex
        }.thenBy { it })

        this.oddsMap?.clear()
        oddsMap?.let { this.oddsMap?.putAll(it) }
    }

    /**
     * 根据注单列表，更新注单状态
     */
    private fun List<Odd?>.updateOddSelectState() {
        this.forEach { odd ->
            odd?.isSelected = betInfoRepository.betInfoList.value?.peekContent()
                ?.any { betInfoListData ->
                    betInfoListData.matchOdd.oddsId == odd?.id
                } == true
        }
    }

    /**
     * 更新翻譯
     */
    private fun List<LeagueOdd>.getPlayCateNameMap(matchType: String) {
        onEach { LeagueOdd ->
            LeagueOdd.matchOdds.onEach { matchOdd ->
                //上方已经把leagueOdds.playCateNameMap 赋值给 matchOdd.playCateNameMap，所以这里不再对波胆玩法处理
                if (matchType == MatchType.CS.postValue) {
//                    matchOdd.playCateNameMap =
//                        PlayCateMenuFilterUtils.filterList?.get(GameType.FT.name)
//                            ?.get(PlayCate.CS.value)?.playCateNameMap
//                    Timber.e("matchOdd.playCateNameMap: ${matchOdd.playCateNameMap}")
                } else {
                    matchOdd.playCateNameMap =
                        PlayCateMenuFilterUtils.filterList?.get(matchOdd.matchInfo?.gameType)
                            ?.get(MenuCode.MAIN.code)?.playCateNameMap
                }
            }
        }
    }

    /**
     * 根據當前MatchType過濾快捷玩法
     */
    private fun MatchOdd.filterQuickPlayCate(matchType: String) {
        //MatchType為波膽時, 僅需顯示反波膽, 其餘不需顯示反波膽
        quickPlayCateList = when (matchType) {
            MatchType.CS.postValue -> {
                quickPlayCateList?.filter { it.code == QuickPlayCate.QUICK_LCS.value }
                    ?.toMutableList()
            }
            else -> {
                quickPlayCateList?.filter { it.code != QuickPlayCate.QUICK_LCS.value }
                    ?.toMutableList()
            }
        }
    }

    val sportTypeMenuData by lazy { SingleLiveEvent<Triple<List<Item>, Boolean, String>>() }
    val sportMenuApiResult = SingleLiveEvent<ApiResult<SportMenuData>>()

    fun loadMatchType(matchType: MatchType) = callApi({
        SportRepository.getSportMenu(
            TimeUtil.getNowTimeStamp().toString(),
            TimeUtil.getTodayStartTimeStamp().toString())
    }) { sportMenuResult->
        val menuData = sportMenuResult.getData()

        if(!sportMenuResult.succeeded() || menuData == null) {
            sportTypeMenuData.value = Triple(listOf(), sportMenuResult.succeeded(), sportMenuResult.msg)
            return@callApi
        }

        val itemList = when (matchType) {
            MatchType.IN_PLAY ->  menuData.menu.inPlay.items
            MatchType.TODAY -> menuData.menu.today.items
            MatchType.EARLY -> menuData.menu.early.items
            MatchType.PARLAY -> menuData.menu.parlay.items
            MatchType.OUTRIGHT -> menuData.menu.outright.items
            MatchType.AT_START -> menuData.atStart.items
            MatchType.CS -> menuData.menu.cs.items
            MatchType.EPS -> menuData.menu.eps?.items ?: listOf()
            MatchType.MY_EVENT -> menuData.menu.myFavorite?.items ?: listOf()
            else -> listOf()
        }

        sportTypeMenuData.value = Triple(itemList, sportMenuResult.succeeded(), sportMenuResult.msg)
        sportMenuApiResult.value = sportMenuResult
    }

    private fun SportMenuResult.updateSportSelectState(
        matchType: MatchType?,
        gameTypeCode: String?,
    ) {
        this.sportMenuData?.updateSportSelectState(matchType, gameTypeCode)
        _sportMenuResult.postValue(this)
    }

    private fun SportMenuData.updateSportSelectState(
        matchType: MatchType?,
        gameTypeCode: String?,
    ): SportMenuData {
        when (matchType) {
            MatchType.IN_PLAY -> this.menu.inPlay.items.map { sport ->
                sport.isSelected = when {
                    (gameTypeCode != null && sport.num > 0) -> {
                        sport.code == gameTypeCode
                    }
                    else -> {
                        this.menu.inPlay.items.indexOf(sport) == 0
                    }
                }
            }
            MatchType.TODAY -> this.menu.today.items.map { sport ->
                sport.isSelected = when {
                    (gameTypeCode != null && sport.num > 0) -> {
                        sport.code == gameTypeCode
                    }
                    else -> {
                        this.menu.today.items.indexOf(sport) == 0
                    }
                }
            }
            MatchType.EARLY -> this.menu.early.items.map { sport ->
                sport.isSelected = when {
                    (gameTypeCode != null && sport.num > 0) -> {
                        sport.code == gameTypeCode
                    }
                    else -> {
                        this.menu.early.items.indexOf(sport) == 0
                    }
                }
            }
            MatchType.CS -> this.menu.cs.items.map { sport ->
                sport.isSelected = when {
                    (gameTypeCode != null && sport.num > 0) -> {
                        sport.code == gameTypeCode
                    }
                    else -> {
                        this.menu.cs.items.indexOf(sport) == 0
                    }
                }
            }
            MatchType.PARLAY -> this.menu.parlay.items.map { sport ->
                sport.isSelected = when {
                    (gameTypeCode != null && sport.num > 0) -> {
                        sport.code == gameTypeCode
                    }
                    else -> {
                        this.menu.parlay.items.indexOf(sport) == 0
                    }
                }
            }
            MatchType.OUTRIGHT -> this.menu.outright.items.map { sport ->
                sport.isSelected = when {
                    (gameTypeCode != null && sport.num > 0) -> {
                        sport.code == gameTypeCode
                    }
                    else -> {
                        this.menu.outright.items.indexOf(sport) == 0
                    }
                }
            }
            MatchType.AT_START -> this.atStart.items.map { sport ->
                sport.isSelected = when {
                    (gameTypeCode != null && sport.num > 0) -> {
                        sport.code == gameTypeCode
                    }
                    else -> {
                        this.atStart.items.indexOf(sport) == 0
                    }
                }
            }
            MatchType.EPS -> this.menu.eps?.items?.map { sport ->
                sport.isSelected = when {
                    (gameTypeCode != null && sport.num > 0) -> {
                        sport.code == gameTypeCode
                    }
                    else -> {
                        this.menu.eps.items.indexOf(sport) == 0
                    }
                }
            }
        }
        return this
    }
}