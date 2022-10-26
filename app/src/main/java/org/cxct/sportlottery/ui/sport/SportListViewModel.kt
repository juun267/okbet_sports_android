package org.cxct.sportlottery.ui.sport

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.QuickPlayCate
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.*
import org.cxct.sportlottery.network.outright.odds.OutrightItem
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListRequest
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.SearchResponse
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.ui.game.data.Date
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.TimeUtil.DMY_FORMAT
import org.cxct.sportlottery.util.TimeUtil.HM_FORMAT
import org.cxct.sportlottery.util.TimeUtil.getTodayTimeRangeParams
import java.util.*

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
    var matchType: MatchType = MatchType.IN_PLAY
    var gameType = GameType.FT.key

    val leagueFilterList: LiveData<List<League>>
        get() = _leagueFilterList
    private val _leagueFilterList = MutableLiveData<List<League>>()

    val curDate: LiveData<List<Date>>
        get() = _curDate

    val curDatePosition: LiveData<Int>
        get() = _curDatePosition
    private val _curDate = MutableLiveData<List<Date>>()
    private val _curDatePosition = MutableLiveData<Int>()

    val oddsListGameHallResult: LiveData<Event<OddsListResult?>>
        get() = _oddsListGameHallResult
    private val _oddsListGameHallResult = MutableLiveData<Event<OddsListResult?>>()

    val outrightMatchList: LiveData<Event<List<Any>>>
        get() = _outrightMatchList
    private val _outrightMatchList = MutableLiveData<Event<List<Any>>>()

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

    private var sportMenuData: SportMenuData? = null //球種菜單資
    var tempDatePosition: Int = 0 //早盤的日期選擇切頁後要記憶的問題，切換球種要清除記憶

    fun getSportSelectedCode(matchType: MatchType): String? = when (matchType) {
        MatchType.IN_PLAY -> {
            sportMenuResult.value?.sportMenuData?.menu?.inPlay?.items?.find { it.isSelected }?.code
        }
        MatchType.TODAY -> {
            sportMenuResult.value?.sportMenuData?.menu?.today?.items?.find { it.isSelected }?.code
        }
        MatchType.EARLY -> {
            sportMenuResult.value?.sportMenuData?.menu?.early?.items?.find { it.isSelected }?.code
        }
        MatchType.CS -> {
            sportMenuResult.value?.sportMenuData?.menu?.cs?.items?.find { it.isSelected }?.code
        }
        MatchType.PARLAY -> {
            sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.find { it.isSelected }?.code
        }
        MatchType.OUTRIGHT -> {
            sportMenuResult.value?.sportMenuData?.menu?.outright?.items?.find { it.isSelected }?.code
        }
        MatchType.AT_START -> {
            sportMenuResult.value?.sportMenuData?.atStart?.items?.find { it.isSelected }?.code
        }
        MatchType.EPS -> {
            sportMenuResult.value?.sportMenuData?.menu?.eps?.items?.find { it.isSelected }?.code
        }
        else -> {
            null
        }
    }

    fun getGameHallList(
        isReloadDate: Boolean,
        date: String? = null,
        leagueIdList: List<String>? = null,
        isReloadPlayCate: Boolean = false,
        isLastSportType: Boolean = false,
    ) {
        var reloadedDateRow: List<Date>? = null

        if (isReloadDate) {
            reloadedDateRow = getDateRow(matchType)
        }

        //20220422 若重新讀取日期列(isReloadDate == true)時，會因postValue 比getCurrentTimeRangeParams取當前日期慢導致取到錯誤的時間
        val reloadedTimeRange = reloadedDateRow?.find {
            it.isSelected
        }?.timeRangeParams
        //20220422 若重新讀取日期列(isReloadDate == true)時，會因postValue 比getCurrentTimeRangeParams取當前日期慢導致取到錯誤的時間\
        val requestTimeRangeParams = reloadedTimeRange ?: getCurrentTimeRangeParams()
            when (matchType) {
                MatchType.IN_PLAY -> {
                    getOddsList(
                        gameType = gameType,
                        matchType.postValue,
                        leagueIdList = leagueIdList
                    )
                }
                MatchType.AT_START -> {
                    getOddsList(
                        gameType = gameType,
                        matchType.postValue,
                        TimeUtil.getAtStartTimeRangeParams(),
                        leagueIdList = leagueIdList,
                    )
                }
                MatchType.TODAY -> {
                    getOddsList(
                        gameType = gameType,
                        matchType.postValue,
                        getCurrentTimeRangeParams(),
                        leagueIdList = leagueIdList
                    )
                }
                MatchType.EARLY -> {
                    getOddsList(
                        gameType = gameType,
                        matchType.postValue,
                        requestTimeRangeParams,
                        leagueIdList = leagueIdList,
                    )
                }
                MatchType.CS -> {
                    getOddsList(
                        gameType = gameType,
                        matchType.postValue,
                        requestTimeRangeParams,
                        leagueIdList = leagueIdList,
                    )
                }
                MatchType.PARLAY -> {
                    getOddsList(
                        gameType = gameType,
                        matchType.postValue,
                        requestTimeRangeParams,
                        leagueIdList = leagueIdList,
                    )

                }
                MatchType.OUTRIGHT -> {
                    getOutrightOddsList(gameType)
                }
                else -> {
                }
        }

    }

    private fun getDateRow(matchType: MatchType): List<Date>? {
        val dateRow = when (matchType) {
            MatchType.TODAY -> {
                tempDatePosition = 0 //切換賽盤清除記憶
                listOf(Date("", getTodayTimeRangeParams()))
            }
            MatchType.EARLY, MatchType.CS -> {
                getDateRowEarly()
            }
            MatchType.PARLAY -> {
                tempDatePosition = 0
                getDateRowParlay()
            }
            MatchType.AT_START -> {
                tempDatePosition = 0
                listOf(Date("", TimeUtil.getAtStartTimeRangeParams()))
            }
            else -> {
                tempDatePosition = 0
                listOf()
            }
        }

        return if (tempDatePosition != 0) {
            dateRow[tempDatePosition].let {
                dateRow.updateDateSelectedState(it)
            }
        } else
            dateRow.firstOrNull()?.let {
                dateRow.updateDateSelectedState(it)
            }
    }

    fun switchGameType(item: Item) {
        gameType = item.code
        if (jobSwitchGameType?.isActive == true) {
            jobSwitchGameType?.cancel()
        }
        //視覺上需要優先跳轉 tab
        _sportMenuResult.value?.updateSportSelectState(matchType, item.code)
        jobSwitchGameType = viewModelScope.launch {
            getGameHallList(true, isReloadPlayCate = true)
        }
    }

    fun cleanGameHallResult() {
        _oddsListGameHallResult.postValue(Event(null))
    }

    private var jobSwitchGameType: Job? = null
    private fun getOddsList(
        gameType: String,
        matchType: String,
        timeRangeParams: TimeRangeParams? = null,
        leagueIdList: List<String>? = null,
        matchIdList: List<String>? = null,
    ) {
        var currentTimeRangeParams: TimeRangeParams? = null
        when (matchType) {
            MatchType.IN_PLAY.postValue, MatchType.AT_START.postValue, MatchType.OTHER.postValue -> {
                currentTimeRangeParams = timeRangeParams
            }
            MatchType.TODAY.postValue, MatchType.CS.postValue, MatchType.EARLY.postValue, MatchType.PARLAY.postValue -> {
                _oddsListGameHallResult.value = Event(null)
                currentTimeRangeParams = timeRangeParams
            }
            else -> { // 特殊賽事要給特殊代碼 Ex: matchType: "sc:QAtest"
                _oddsListGameHallResult.value = Event(null)
            }
        }

        val emptyFilter = { list: List<String>? ->
            if (list.isNullOrEmpty()) null else list
        }

        val matchTypeFilter = { matchType: String ->
            if (matchIdList.isNullOrEmpty()) matchType
            else "PARLAY"
        }

        val timeFilter = { timeString: String? ->
            if (matchIdList.isNullOrEmpty()) timeString
            else null
        }

        var startTime = ""
        var endTime = ""
        if (matchType != MatchType.OTHER.postValue) { // 特殊賽事則不帶時間 Ex: {gameType: "FT", matchType: "sc:QAtest", playCateMenuCode: "MAIN"}
            startTime = timeFilter(currentTimeRangeParams?.startTime) ?: ""
            endTime = timeFilter(currentTimeRangeParams?.endTime) ?: ""
        }
        var playCateMenuCode = MenuCode.MAIN.code
//        Timber.e("getPlayCateSelected: ${getPlayCateSelected()}")
        if (matchType == MatchType.CS.postValue) {
            playCateMenuCode = MenuCode.CS.code
        }
//        Timber.e("playCateMenuCode: $playCateMenuCode")

        viewModelScope.launch {
            var result: OddsListResult? = null
            if (matchType == MatchType.IN_PLAY.postValue && gameType == GameType.ALL.key) {
                doNetwork(androidContext) {
                    OneBoSportApi.oddsService.getInPlayAllList(
                        OddsListRequest(
                            gameType,
                            matchTypeFilter(matchType),
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
                    result = OddsListResult(it.code, it.msg, it.success,
                        OddsListData(leagueOdds, Sport(GameType.ALL.key, GameType.ALL.name)))
                }
            } else {
                result = doNetwork(androidContext) {
                    OneBoSportApi.oddsService.getOddsList(
                        OddsListRequest(
                            gameType,
                            matchTypeFilter(matchType),
                            leagueIdList = emptyFilter(leagueIdList),
                            matchIdList = emptyFilter(matchIdList),
                            startTime = startTime,
                            endTime = endTime,
                            playCateMenuCode = playCateMenuCode
                        )
                    )
                }
            }
            result?.updateMatchType()
            result?.oddsListData?.leagueOdds?.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
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
                    matchOdd.filterQuickPlayCate(matchType)
                    //波胆的数据获取方式
                    if (matchType == MatchType.CS.postValue) {
                        matchOdd.playCateNameMap = leagueOdd.playCateNameMap
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
                -> {
                    if (_leagueFilterList.value?.isNotEmpty() == true) {
                        result?.oddsListData?.leagueOddsFilter =
                            result?.oddsListData?.leagueOdds?.filter {
                                leagueFilterList.value?.map { league -> league.id }
                                    ?.contains(it.league.id) ?: false
                            }
                    }
                    _oddsListGameHallResult.postValue(Event(result))
                }
                else -> {
                    _oddsListGameHallResult.postValue(Event(result))
                }
            }
            notifyFavorite(FavoriteType.MATCH)
        }
    }

    fun getOutrightOddsList(gameType: String, leagueIdList: List<String>? = null) {
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

            val outrightMatchList =
                mutableListOf<org.cxct.sportlottery.network.outright.odds.MatchOdd>()

            val oddsList = mutableListOf<Any>()
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

                    //region 先處理頁面顯示需要的資料結構
                    matchOdd?.let { matchOddNotNull ->
                        //聯賽標題
//                        oddsList.add(matchOddNotNull)
                        var playCateCodeList = mutableListOf<String>()
                        var subTitleList = mutableListOf<String>()
                        var odds = mutableListOf<List<Odd>>()
                        matchOddNotNull.oddsMap?.forEach { oddMap ->
                            val playCateExpand =
                                matchOddNotNull.oddsExpand?.get(oddMap.key) ?: false
                            playCateCodeList.add(oddMap.key)
                            subTitleList.add(matchOddNotNull.dynamicMarkets[oddMap.key]?.let {
                                when (LanguageManager.getSelectLanguage(LocalUtils.getLocalizedContext())) {
                                    LanguageManager.Language.ZH -> {
                                        it.zh
                                    }
                                    LanguageManager.Language.VI -> {
                                        it.vi
                                    }
                                    LanguageManager.Language.TH -> {
                                        it.th
                                    }
                                    else -> {
                                        it.en
                                    }
                                }
                            } ?: "")

                            //endregion

                            //region 玩法賠率項
                            odds.add(oddMap.value?.filterNotNull()
                                ?.mapIndexed { index, odd ->
                                    odd.outrightCateKey = oddMap.key
                                    odd.playCateExpand = playCateExpand
                                    odd.leagueExpanded = matchOddNotNull.isExpand
                                    odd.belongMatchOdd = matchOddNotNull
                                    odd.isExpand = true
                                    odd
                                } ?: listOf<Odd>())
                            //endregion

                        }
                        //region 玩法標題
                        oddsList.add(
                            OutrightItem(
                                matchOdd = matchOddNotNull,
                                playCateCodeList = playCateCodeList,
                                subTitleList = subTitleList,
                                leagueExpanded = matchOddNotNull.isExpand,
                                oddsList = odds
                            )
                        )
//                        matchOddNotNull.outrightOddsList = oddsList
                        outrightMatchList.add(matchOddNotNull)
                    }
                    //endregion
                }
            }
            withContext(Dispatchers.Main) {
                _outrightMatchList.value = Event(oddsList)
            }
        }
    }

    private fun getDateRowEarly(): List<Date> {
        val locale = LanguageManager.getSetLanguageLocale(androidContext)
        val dateRow = mutableListOf(
            Date(
                LocalUtils.getString(R.string.date_row_all),
                TimeUtil.getEarlyAllTimeRangeParams()
            ), Date(
                androidContext.getString(R.string.other),
                TimeUtil.getOtherEarlyDateTimeRangeParams()
            )
        )

        dateRow.addAll(1, TimeUtil.getFutureDate(
            7
        ).map {
            Date(it, TimeUtil.getDayDateTimeRangeParams(it, locale), isDateFormat = true)
        })
        return dateRow
    }

    private fun getDateRowParlay(): List<Date> {
        val dateRow = mutableListOf(
            Date(
                LocalUtils.getString(R.string.date_row_all),
                TimeUtil.getParlayAllTimeRangeParams()
            ), Date(
                androidContext.getString(R.string.date_row_live),
                object : TimeRangeParams {
                    override val startTime: String?
                        get() = null
                    override val endTime: String?
                        get() = null
                },
                MatchType.IN_PLAY.postValue
            ), Date(
                androidContext.getString(R.string.date_row_today),
                TimeUtil.getParlayTodayTimeRangeParams(),
                MatchType.TODAY.postValue
            ), Date(
                androidContext.getString(R.string.other),
                TimeUtil.getOtherEarlyDateTimeRangeParams(),
                MatchType.EARLY.postValue
            )
        )

        dateRow.addAll(3, TimeUtil.getFutureDate(
            6,
            when (LanguageManager.getSelectLanguage(androidContext)) {
                LanguageManager.Language.ZH -> {
                    Locale.CHINA
                }
                else -> {
                    Locale.getDefault()
                }
            }
        ).map {
            Date(
                it,
                TimeUtil.getDayDateTimeRangeParams(it),
                MatchType.EARLY.postValue,
                isDateFormat = true
            )
        })

        return dateRow
    }

    fun resetErrorDialogMsg() {
        _showErrorDialogMsg.value = ""
    }

    fun getCurrentTimeRangeParams(): TimeRangeParams? {
        return _curDate.value?.find {
            it.isSelected
        }?.timeRangeParams
    }

    fun switchMatchDate(matchType: MatchType, date: Date) {
        _curDate.value?.updateDateSelectedState(date)
        getGameHallList(false, date.date)
    }

    private fun List<Date>.updateDateSelectedState(date: Date): List<Date> {
        this.forEachIndexed { index, value ->
            run {
                value.isSelected = (value == date)
                if (value.isSelected) tempDatePosition = index
            }
        }

        _curDate.postValue(this)
        _curDatePosition.postValue(this.indexOf(date))
        return this
    }

    private fun List<SearchResponse.Row>.updateMatchType() {
        forEach { row ->
            row.leagueMatchList.forEach { leagueMatch ->
                leagueMatch.matchInfoList.forEach { matchInfo ->
                    matchInfo.isInPlay = System.currentTimeMillis() > matchInfo.startTime.toLong()
                }
            }
        }
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
                }
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

    fun filterLeague(leagueList: List<League>) {
        _leagueFilterList.postValue(leagueList)
    }

    fun setCurMatchType(matchType: MatchType) {
        this.matchType = matchType
    }

    fun updateOutrightOddsChange(context: Context?, oddsChangeEvent: OddsChangeEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            outrightMatchList.value?.peekContent()?.let { outrightList ->
                outrightList.filterIsInstance<OutrightItem>().forEach { outrightItem ->
                    SocketUpdateUtil.updateMatchOdds(
                        context, outrightItem.matchOdd, oddsChangeEvent
                    )
                }
                withContext(Dispatchers.Main) {
                    _outrightMatchList.value = Event(outrightList)
                }
            }
        }
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
                    body()?.sportMenuData?.sortSport().apply { sportMenuData = this }
                }
            }
        }
    }

    fun switchMatchType(matchType: MatchType) {
        viewModelScope.launch {
            val sportMenuResult = getSportMenuAll()
            sportMenuResult?.let {
                if (it.success) {
                    _sportMenuResult.postValue(it)     // 更新大廳上方球種數量、各MatchType下球種和數量
                    if (_sportMenuResult.value == null) {
                        updateSportInfo(matchType)     // 初次頁面進入
                    }
                } else {
                    return@launch
                }
            }
        }
        if (_sportMenuResult.value != null) {
            viewModelScope.launch {
                updateSportInfo(matchType)
            }
        }
    }

    private suspend fun updateSportInfo(matchType: MatchType) {
        setCurMatchType(matchType)
        // 無數量直接顯示無資料UI
        if (getMatchCount(matchType) < 1) {
            _isNoEvents.postValue(true)
            return
        }
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