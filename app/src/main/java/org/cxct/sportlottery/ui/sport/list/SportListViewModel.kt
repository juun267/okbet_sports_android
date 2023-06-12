package org.cxct.sportlottery.ui.sport.list

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.clean
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.safeApi
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.sport.SportRepository
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.QuickPlayCate
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
import org.cxct.sportlottery.util.TimeUtil.DMY_FORMAT
import org.cxct.sportlottery.util.TimeUtil.HM_FORMAT
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class SportListViewModel(
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
    var gameType = GameType.FT.key

    val oddsListGameHallResult: LiveData<Event<OddsListResult?>>
        get() = _oddsListGameHallResult
    private val _oddsListGameHallResult = SingleLiveEvent<Event<OddsListResult?>>()

    //ErrorDialog
    val showErrorDialogMsg: LiveData<String>
        get() = _showErrorDialogMsg
    private var _showErrorDialogMsg = MutableLiveData<String>()
    val isNoEvents: LiveData<Boolean>
        get() = _isNoEvents
    private var _isNoEvents = MutableLiveData<Boolean>()
    val sportMenuResult: LiveData<SportMenuResult?>
        get() = _sportMenuResult
    private val _sportMenuResult = MutableLiveData<SportMenuResult?>()

    var tempDatePosition: Int = 0 //早盤的日期選擇切頁後要記憶的問題，切換球種要清除記憶


    val outrightList = MutableLiveData<Event<OutrightOddsListResult?>>()
    var selectTimeRangeParams:TimeRangeParams? = null
    var selectMatchIdList: ArrayList<String>? = null

    fun getGameHallList(
        matchType: MatchType,
        tag: Any? = null) {

        when (matchType) {
            MatchType.IN_PLAY -> {
                getOddsList(
                    gameType = gameType,
                    matchType.postValue,
                    matchIdList = selectMatchIdList,
                    tag = tag
                )
            }
            MatchType.AT_START -> {
                getOddsList(
                    gameType = gameType,
                    matchType.postValue,
                    TimeUtil.getAtStartTimeRangeParams(),
                    matchIdList = selectMatchIdList,
                    tag = tag
                )
            }
            MatchType.TODAY -> {
                getOddsList(
                    gameType = gameType,
                    matchType.postValue,
                    selectTimeRangeParams,
                    matchIdList = selectMatchIdList,
                    tag = tag
                )
            }
            MatchType.EARLY -> {
                getOddsList(
                    gameType = gameType,
                    matchType.postValue,
                    selectTimeRangeParams,
                    matchIdList = selectMatchIdList,
                    tag = tag
                )
            }
            MatchType.CS -> {
                getOddsList(
                    gameType = gameType,
                    matchType.postValue,
                    selectTimeRangeParams,
                    matchIdList = selectMatchIdList,
                    tag = tag
                )
            }
            MatchType.PARLAY -> {
                getOddsList(
                    gameType = gameType,
                    matchType.postValue,
                    selectTimeRangeParams,
                    matchIdList = selectMatchIdList,
                    tag = tag
                )

            }

            MatchType.END_SCORE -> {
                getOddsList(
                    gameType = gameType,
                    matchType.postValue,
                    selectTimeRangeParams,
                    matchIdList = selectMatchIdList,
                    tag = tag
                )

            }
            MatchType.OUTRIGHT -> {
                getOutrightOddsList(gameType)
            }
            else -> {
            }
        }
    }

    fun switchGameType(matchType: MatchType, item: Item, tag: Any? = null) {
        gameType = item.code
        if (jobSwitchGameType?.isActive == true) {
            jobSwitchGameType?.cancel()
        }
        //視覺上需要優先跳轉 tab
        _sportMenuResult.value?.updateSportSelectState(matchType, item.code)
        jobSwitchGameType = viewModelScope.launch {
            getGameHallList(matchType, tag = tag)
        }
    }

    fun cleanGameHallResult() {
    }

    fun getEndScoreOddsList(gameType: String,
                            matchType: MatchType,
                            leagueIdList: List<String>? = null,
                            matchIdList: List<String>? = null,) {

        getOddsList(gameType, matchType.postValue, selectTimeRangeParams)
    }

    private var requestTag: Any? = null
    private var jobSwitchGameType: Job? = null
    private var jobGetOddsList: Job? = null
    private fun getOddsList(
        gameType: String,
        matchType: String,
        timeRangeParams: TimeRangeParams? = null,
        leagueIdList: List<String>? = null,
        matchIdList: List<String>? = null,
        tag: Any? = null
    ) {
        requestTag = tag
        var currentTimeRangeParams: TimeRangeParams? = null
        when (matchType) {
            MatchType.IN_PLAY.postValue, MatchType.AT_START.postValue, MatchType.OTHER.postValue -> {
                currentTimeRangeParams = timeRangeParams
            }
            MatchType.TODAY.postValue, MatchType.CS.postValue, MatchType.EARLY.postValue, MatchType.PARLAY.postValue -> {
                _oddsListGameHallResult.value = Event(null, gameType)
                currentTimeRangeParams = timeRangeParams
            }
            else -> { // 特殊賽事要給特殊代碼 Ex: matchType: "sc:QAtest"
                _oddsListGameHallResult.value = Event(null, gameType)
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
                            matchIdList = emptyFilter(matchIdList),
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
                            matchIdList = emptyFilter(matchIdList),
                            startTime = startTime,
                            endTime = endTime,
                            playCateMenuCode = playCateMenuCode
                        )
                    )
                }
            }

            if (requestTag != tag) {
                return@launch
            }
            result?.updateMatchType()
            result?.oddsListData?.leagueOdds?.forEach { leagueOdd ->
                var iterator = leagueOdd.matchOdds.iterator()
                while (iterator.hasNext()) {
                    val matchOdd = iterator.next()
                    if (matchOdd.matchInfo == null) { // 过滤掉matchInfo为空的脏数据
                        iterator.remove()
                    } else {

                        matchOdd.sortOddsMap()
                        matchOdd.matchInfo?.let { matchInfo ->
                            matchInfo.startDateDisplay =
                                TimeUtil.timeFormat(matchInfo.startTime, "MM/dd")

                            matchOdd.matchInfo.startTimeDisplay =
                                TimeUtil.timeFormat(matchInfo.startTime, "HH:mm")

                            matchInfo.remainTime = TimeUtil.getRemainTime(matchInfo.startTime)
                        }

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
            result?.oddsListData.getPlayCateNameMap(matchType)

            when (matchType) {
                MatchType.IN_PLAY.postValue,
                MatchType.TODAY.postValue,
                MatchType.AT_START.postValue,
                MatchType.EARLY.postValue,
                MatchType.PARLAY.postValue,
                MatchType.END_SCORE.postValue,
                -> {
                    _oddsListGameHallResult.postValue(Event(result, gameType))
                }
                else -> {
                    _oddsListGameHallResult.postValue(Event(result, tag ?: gameType))
                }
            }

            notifyFavorite(FavoriteType.MATCH)
        }
    }

    private fun getOutrightOddsList(gameType: String, leagueIdList: List<String>? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = doNetwork(androidContext) {
                OneBoSportApi.outrightService.getOutrightOddsList(

                    if (leagueIdList.isNullOrEmpty()) {
                        OutrightOddsListRequest(
                            gameType,
                            matchType = MatchType.OUTRIGHT.postValue
                        )
                    } else {
                        OutrightOddsListRequest(
                            gameType,
                            matchType = MatchType.OUTRIGHT.postValue,
                            leagueIdList = leagueIdList
                        )
                    }
                )
            }

            if (gameType != this@SportListViewModel.gameType) {
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
                        TimeUtil.timeFormat(matchOdd?.matchInfo?.endTime, DMY_FORMAT)
                    matchOdd?.startTime =
                        TimeUtil.timeFormat(matchOdd?.matchInfo?.endTime, HM_FORMAT)

                }
            }

            if (gameType != this@SportListViewModel.gameType) {
                return@launch
            }
            outrightList.postValue(Event(result, gameType))
        }
    }


    fun resetErrorDialogMsg() {
        _showErrorDialogMsg.value = ""
    }

    /**
     * 检查赛事是否到了开赛时间，并且更新滚球状态
     */
    private fun OddsListResult.updateMatchType(): OddsListResult {
        this.oddsListData?.leagueOdds?.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->

                matchOdd.matchInfo?.isInPlay =
                    System.currentTimeMillis() > matchOdd.matchInfo?.startTime ?: 0

                matchOdd.matchInfo?.isAtStart =
                    TimeUtil.isTimeAtStart(matchOdd.matchInfo?.startTime)
            }
        }
        return this
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
    private fun OddsListData?.getPlayCateNameMap(matchType: String) {
        this?.leagueOdds?.onEach { LeagueOdd ->
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

    private fun SportMenuData.sortSport(): SportMenuData {
        this.menu.inPlay.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.today.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.early.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.cs.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.parlay.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.outright.items.sortedBy { sport ->
            sport.sortNum
        }
        this.atStart.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.eps?.items?.sortedBy { sport ->
            sport.sortNum
        }

        return this
    }

    fun getMatchCount(matchType: MatchType, sportMenuResult: SportMenuResult? = null): Int {
        val sportMenuRes = sportMenuResult ?: _sportMenuResult.value
        return when (matchType) {
            MatchType.IN_PLAY -> {
                sportMenuRes?.sportMenuData?.menu?.inPlay?.items?.sumBy { it.num } ?: 0
            }
            MatchType.TODAY -> {
                sportMenuRes?.sportMenuData?.menu?.today?.items?.sumBy { it.num } ?: 0
            }
            MatchType.EARLY -> {
                sportMenuRes?.sportMenuData?.menu?.early?.items?.sumBy { it.num } ?: 0
            }
            MatchType.CS -> {
                sportMenuRes?.sportMenuData?.menu?.cs?.items?.sumBy { it.num } ?: 0
            }
            MatchType.PARLAY -> {
                sportMenuRes?.sportMenuData?.menu?.parlay?.items?.sumBy { it.num } ?: 0
            }
            MatchType.OUTRIGHT -> {
                sportMenuRes?.sportMenuData?.menu?.outright?.items?.sumBy { it.num } ?: 0
            }
            MatchType.AT_START -> {
                sportMenuRes?.sportMenuData?.atStart?.items?.sumBy { it.num } ?: 0
            }
            MatchType.EPS -> {
                sportMenuRes?.sportMenuData?.menu?.eps?.items?.sumBy { it.num } ?: 0
            }
            else -> {
                0
            }
        }
    }

    //滾球、今日、早盤、冠軍、串關、(即將跟menu同一層)
    private suspend fun getSportMenuAll(): SportMenuResult? {
        return doNetwork(androidContext) {
            sportMenuRepository.getSportMenu(
                TimeUtil.getNowTimeStamp().toString(),
                TimeUtil.getTodayStartTimeStamp().toString()
            ).apply {
                if (isSuccessful && body()?.success == true) {
                    // 每次執行必做
                    body()?.sportMenuData?.sortSport()
                }
            }
        }
    }

    val sportMenuData by lazy { SingleLiveEvent<Pair<ApiResult<SportMenuData>, List<Item>>>() }

    fun loadMatchType(matchType: MatchType) = callApi({
        SportRepository.getSportMenu(
            TimeUtil.getNowTimeStamp().toString(),
            TimeUtil.getTodayStartTimeStamp().toString())
    }) { sportMenuResult->

        val menuData = sportMenuResult.getData()
        if (menuData?.sortSport() != null) {
            updateSportInfo(matchType)
        }


        if(!sportMenuResult.succeeded() || menuData == null) {
            sportMenuData.value = Pair(sportMenuResult, listOf())
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

            else -> listOf()
        }

        sportMenuData.value = Pair(sportMenuResult, itemList)
    }

    fun switchMatchType(matchType: MatchType) {

        viewModelScope.launch {
            val sportMenuResult = getSportMenuAll()
            sportMenuResult?.let {
                if (it.success) {
                    _sportMenuResult.postValue(it)     // 更新大廳上方球種數量、各MatchType下球種和數量
                }
            }
        }
        if (_sportMenuResult.value != null) {
            updateSportInfo(matchType)
        }
    }

    private fun updateSportInfo(matchType: MatchType) {

        // 無數量直接顯示無資料UI
        Timber.d("getMatchCount： ${getMatchCount(matchType)}")
        _isNoEvents.postValue(getMatchCount(matchType) < 1)
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