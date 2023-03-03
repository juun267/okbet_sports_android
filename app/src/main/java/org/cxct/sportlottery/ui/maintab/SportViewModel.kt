package org.cxct.sportlottery.ui.maintab

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.OddSpreadForSCO
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.common.QuickPlayCate
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.league.LeagueListRequest
import org.cxct.sportlottery.network.league.LeagueListResult
import org.cxct.sportlottery.network.manager.RequestManager
import org.cxct.sportlottery.network.match.MatchRound
import org.cxct.sportlottery.network.match.MatchService
import org.cxct.sportlottery.network.matchLiveInfo.ChatLiveLoginData
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.detail.OddsDetailRequest
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.odds.eps.OddsEpsListRequest
import org.cxct.sportlottery.network.odds.eps.OddsEpsListResult
import org.cxct.sportlottery.network.odds.list.*
import org.cxct.sportlottery.network.odds.quick.QuickListData
import org.cxct.sportlottery.network.outright.odds.OutrightItem
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListRequest
import org.cxct.sportlottery.network.service.league_change.LeagueChangeEvent
import org.cxct.sportlottery.network.sport.*
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.network.sport.coupon.SportCouponMenuResult
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.network.sport.query.*
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.network.third_game.ThirdLoginResult
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.network.today.MatchCategoryQueryRequest
import org.cxct.sportlottery.network.today.MatchCategoryQueryResult
import org.cxct.sportlottery.network.user.info.LiveSyncUserInfoVO
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.ui.game.data.Date
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.odds.OddsDetailListData
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.px
import org.cxct.sportlottery.util.DisplayUtil.pxToDp
import org.cxct.sportlottery.util.TimeUtil.DMY_FORMAT
import org.cxct.sportlottery.util.TimeUtil.HM_FORMAT
import org.cxct.sportlottery.util.TimeUtil.getTodayTimeRangeParams
import java.util.*

class SportViewModel(
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
    companion object {
        const val GameLiveSP = "GameLiveSharedPreferences"
        const val GameFastBetOpenedSP = "GameFastBetOpenedSharedPreferences"
    }

    val token = loginRepository.token

    val gotConfig: LiveData<Event<Boolean>>
        get() = _gotConfig
    private val _gotConfig = MutableLiveData<Event<Boolean>>()


    val messageListResult: LiveData<Event<MessageListResult?>>
        get() = _messageListResult

    val curMatchType: LiveData<MatchType?>
        get() = _curMatchType

    val sportMenuResult: LiveData<SportMenuResult?>
        get() = _sportMenuResult

    val curDate: LiveData<List<Date>>
        get() = _curDate

    val curDatePosition: LiveData<Int>
        get() = _curDatePosition
    var tempDatePosition: Int = 0 //早盤的日期選擇切頁後要記憶的問題，切換球種要清除記憶


    val leagueSubmitList: LiveData<Event<List<League>>>
        get() = _leagueSubmitList

    val leagueFilterList: LiveData<List<League>>
        get() = _leagueFilterList

    val playList: LiveData<Event<List<Play>>>
        get() = PlayRepository.playList

    val playCate: LiveData<Event<String?>>
        get() = _playCate

    val searchResult: LiveData<Event<List<SearchResult>?>>
        get() = _searchResult

    val showBetUpperLimit = betInfoRepository.showBetUpperLimit

    private val _messageListResult = MutableLiveData<Event<MessageListResult?>>()
    private val _curMatchType = MutableLiveData<MatchType?>()
    private val _curChildMatchType = MutableLiveData<MatchType?>()
    private val _sportMenuResult = MutableLiveData<SportMenuResult?>()
    private val _sportCouponMenuResult = MutableLiveData<Event<SportCouponMenuResult>>()
    private val _oddsListGameHallResult = MutableLiveData<Event<OddsListResult?>>()

    private val _oddsListGameHallIncrementResult =
        MutableLiveData<Event<OddsListIncrementResult?>>()
    private val _oddsListResult = MutableLiveData<Event<OddsListResult?>>()
    private val _leagueListResult = MutableLiveData<Event<LeagueListResult?>>()
    private val _outrightMatchList = MutableLiveData<Event<List<Any>>>()
    private val _epsListResult = MutableLiveData<Event<OddsEpsListResult?>>()
    private val _matchCategoryQueryResult = MutableLiveData<Event<MatchCategoryQueryResult?>>()
    private val _curDate = MutableLiveData<List<Date>>()
    private val _curDatePosition = MutableLiveData<Int>()
    private val _isNoHistory = MutableLiveData<Boolean>()
    private var _isNoEvents = MutableLiveData<Boolean>()
    private val _leagueSelectedList = MutableLiveData<List<League>>()
    private val _leagueSubmitList = MutableLiveData<Event<List<League>>>()
    private val _leagueFilterList = MutableLiveData<List<League>>()
    private val _playList = PlayRepository.mPlayList
    private val _playCate = MutableLiveData<Event<String?>>()
    private val _searchResult = MutableLiveData<Event<List<SearchResult>?>>()



    private val _betInfoResult = MutableLiveData<Event<BetInfoResult?>>()
    val betInfoResult: LiveData<Event<BetInfoResult?>>
        get() = _betInfoResult

    private val _oddsDetailResult = MutableLiveData<Event<OddsDetailResult?>?>()
    val oddsDetailResult: LiveData<Event<OddsDetailResult?>?>
        get() = _oddsDetailResult

    private val _oddsDetailList = MutableLiveData<Event<ArrayList<OddsDetailListData>>>()
    val oddsDetailList: LiveData<Event<ArrayList<OddsDetailListData>>>
        get() = _oddsDetailList

    //賽事直播網址
    private val _matchLiveInfo = MutableLiveData<Event<MatchRound>?>()
    val matchLiveInfo: LiveData<Event<MatchRound>?>
        get() = _matchLiveInfo

    private val _videoUrl = MutableLiveData<Event<String?>>()
    val videoUrl: LiveData<Event<String?>>
        get() = _videoUrl

    private val _animeUrl = MutableLiveData<Event<String?>>()
    val animeUrl: LiveData<Event<String?>>
        get() = _animeUrl

    //Loading
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private var _isLoading = MutableLiveData<Boolean>()

    //ErrorDialog
    val showErrorDialogMsg: LiveData<String>
        get() = _showErrorDialogMsg
    private var _showErrorDialogMsg = MutableLiveData<String>()

    private val _publicityRecommend = MutableLiveData<Event<List<Recommend>>>()
    val publicityRecommend: LiveData<Event<List<Recommend>>>
        get() = _publicityRecommend

    private val _enterThirdGameResult = MutableLiveData<EnterThirdGameResult>()
    val enterThirdGameResult: LiveData<EnterThirdGameResult>
        get() = _enterThirdGameResult

    //優惠活動文字跑馬燈
    private val _liveLoginInfo = MutableLiveData<Event<ChatLiveLoginData>>()
    val liveLoginInfo: LiveData<Event<ChatLiveLoginData>>
        get() = _liveLoginInfo

    var sportQueryData: SportQueryData? = null
    var specialMenuData: SportQueryData? = null
    var allSearchData: List<SearchResponse.Row>? = null

    private var sportMenuData: SportMenuData? = null //球種菜單資料

    private var lastSportTypeHashMap: HashMap<String, String?> = hashMapOf(
        MatchType.IN_PLAY.postValue to null,
        MatchType.AT_START.postValue to null,
        MatchType.TODAY.postValue to null,
        MatchType.EARLY.postValue to null,
        MatchType.CS.postValue to null,
        MatchType.OUTRIGHT.postValue to null,
        MatchType.PARLAY.postValue to null,
        MatchType.EPS.postValue to null
    )

    //獲取系統公告
    fun getAnnouncement() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                val typeList = arrayOf(1, 2, 3)
                OneBoSportApi.messageService.getPromoteNotice(typeList)
            }?.let { result -> _messageListResult.postValue(Event(result)) }
        }
    }

    fun getSearchResult() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.sportService.getSearchResult(
                    SearchRequest(
                        TimeUtil.getNowTimeStamp().toString(),
                        TimeUtil.getTodayStartTimeStamp().toString()
                    )
                )
            }
            result?.let { it ->
                it.rows.updateMatchType()

                allSearchData = it.rows
            }
        }
    }

    fun getSportSearch(key: String) {
        if (key.isNotEmpty()) {
            //[Martin] 小弟愚鈍 搜尋無法一次Filter所有資料(待強人捕)
            // 所以下面的做法總共分三次去Filter資料 然後再合併
            // 1.篩選球種 2.篩選聯賽 3.篩選比賽
            var finalResult: MutableList<SearchResult> = arrayListOf()
            //1.篩選球種
            var searchResult = allSearchData?.filter { row ->
                row.leagueMatchList.any { leagueMatch ->
                    leagueMatch.matchInfoList.any { matchInfo ->
                        leagueMatch.leagueName.contains(key, true) || matchInfo.homeName.contains(
                            key,
                            true) ||
                                matchInfo.awayName.contains(key, true)
                    }
                }
            }
            searchResult?.forEach {
                var searchResult: SearchResult = SearchResult(it.gameName)
                searchResult.sportTitle = it.gameName
                searchResult.gameType = it.gameType
                finalResult?.add(searchResult)
            }
            //2.篩選聯賽
            var leagueMatchSearchResult = searchResult?.map { row ->
                row.leagueMatchList.filter { leagueMatch ->
                    leagueMatch.matchInfoList.any { matchInfo ->
                        leagueMatch.leagueName.contains(key, true) || matchInfo.homeName.contains(
                            key,
                            true) ||
                                matchInfo.awayName.contains(key, true)
                    }
                }
            }
            leagueMatchSearchResult?.forEachIndexed { index, league ->
                var searchResultLeagueList: MutableList<SearchResult.SearchResultLeague> =
                    arrayListOf()
                league.forEach { leagueMatch ->
                    var searchResultLeague = SearchResult.SearchResultLeague(leagueMatch.leagueName)
                    searchResultLeagueList.add(searchResultLeague)
                }
                finalResult?.get(index).searchResultLeague = searchResultLeagueList
            }
            //3.篩選比賽
            var matchSearchResult = leagueMatchSearchResult?.map { row ->
                row.map { leagueMatch ->
                    leagueMatch.matchInfoList.filter { matchInfo ->
                        leagueMatch.leagueName.contains(key, true) || matchInfo.homeName.contains(
                            key,
                            true) ||
                                matchInfo.awayName.contains(key, true)
                    }
                }
            }
            matchSearchResult?.forEachIndexed { index0, row ->
                row.forEachIndexed { index1, league ->
                    var matchList: MutableList<SearchResponse.Row.LeagueMatch.MatchInfo> =
                        arrayListOf()

                    league.forEachIndexed { index, matchInfo ->
                        matchList.add(matchInfo)
                    }
                    finalResult?.get(index0).searchResultLeague.get(index1).leagueMatchList =
                        matchList
                }
            }
            _searchResult.postValue(Event(finalResult))
        }
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


    var currentSpecialCode = ""


    private fun checkLastSportType(matchType: MatchType, sportQueryData: SportQueryData?) {
        var isContain = false

        sportQueryData?.items?.forEach { item ->
            if (item.code == lastSportTypeHashMap[matchType.postValue])
                isContain = true
        }
        if (!isContain)
            lastSportTypeHashMap[matchType.postValue] = sportQueryData?.items?.firstOrNull()?.code
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

    //TODO 與switchSportType(matchType: MatchType, gameType: String)功能相近但邏輯有些許不同, 不確定是不是邏輯沒同步到, 看能不能整併成一個
    fun switchSportType(matchType: MatchType, item: Item) {
        if (matchType == MatchType.OTHER) {
            specialMenuData?.updateSportSelectState(item.code)
        } else {
            _sportMenuResult.value?.updateSportSelectState(matchType, item.code)
        }
        _playList.value = Event(listOf())
        _playCate.value = Event(null)
        _curChildMatchType.value = null
        _oddsListGameHallResult.value = Event(null)
        _oddsListResult.value = Event(null)

        recordSportType(matchType, item.code)
        if (matchType == MatchType.OTHER) {
            if (!item.hasPlay) {
                getLeagueList(
                    item.code,
                    currentSpecialCode,
                    getCurrentTimeRangeParams(),
                    isIncrement = false
                )
            } else {
                getGameHallList(
                    matchType = MatchType.OTHER,
                    isReloadDate = true,
                    isReloadPlayCate = true,
                    isLastSportType = true
                )
            }
        } else {
            getGameHallList(matchType, true, isReloadPlayCate = true)
        }
        getMatchCategoryQuery(matchType)
        filterLeague(listOf())
    }

    fun switchSportType(matchType: MatchType, gameType: String) {
        if (matchType == MatchType.OTHER) {
            specialMenuData?.updateSportSelectState(gameType)
        } else {
            _sportMenuResult.value?.updateSportSelectState(matchType, gameType)
        }
        _curChildMatchType.postValue(null)
        _oddsListGameHallResult.postValue(Event(null))
        //_quickOddsListGameHallResult.value = Event(null)
        _oddsListResult.postValue(Event(null))

        recordSportType(matchType, gameType)
        if (matchType == MatchType.OTHER) {
            getLeagueList(
                gameType,
                currentSpecialCode,
                getCurrentTimeRangeParams(),
                isIncrement = false
            )
        } else {
            getGameHallList(matchType, true, isReloadPlayCate = true)
        }
        getMatchCategoryQuery(matchType)
        filterLeague(listOf())
    }

    fun recordSportType(matchType: MatchType, sportType: String) {
        lastSportTypeHashMap[matchType.postValue] = sportType
    }


    fun getGameHallList(
        matchType: MatchType,
        isReloadDate: Boolean,
        date: String? = null,
        leagueIdList: List<String>? = null,
        isReloadPlayCate: Boolean = false,
        isLastSportType: Boolean = false,
        isIncrement: Boolean = false,
    ) {

        if (getMatchCount(matchType) < 1) {
            return
        }
        val nowMatchType = curMatchType.value ?: matchType
        var reloadedDateRow: List<Date>? = null

        if (isReloadDate) {
            reloadedDateRow = getDateRow(matchType)
        }

        //20220422 若重新讀取日期列(isReloadDate == true)時，會因postValue 比getCurrentTimeRangeParams取當前日期慢導致取到錯誤的時間
        val reloadedTimeRange = reloadedDateRow?.find {
            it.isSelected
        }?.timeRangeParams

        val sportCode = getSportSelectedCode(nowMatchType)

        //20220422 若重新讀取日期列(isReloadDate == true)時，會因postValue 比getCurrentTimeRangeParams取當前日期慢導致取到錯誤的時間\
        val requestTimeRangeParams = reloadedTimeRange ?: getCurrentTimeRangeParams()
        sportCode?.let { code ->
            when (nowMatchType) {
                MatchType.IN_PLAY -> {
                    getOddsList(
                        code,
                        matchType.postValue,
                        leagueIdList = leagueIdList,
                        isIncrement = isIncrement
                    )
                }
                MatchType.TODAY -> {
                    getOddsList(
                        gameType = code,
                        matchType.postValue,
                        getCurrentTimeRangeParams(),
                        leagueIdList = leagueIdList,
                        isIncrement = isIncrement
                    )
                }
                MatchType.EARLY -> {
                    getOddsList(
                        gameType = code,
                        matchType.postValue,
                        requestTimeRangeParams,
                        leagueIdList = leagueIdList,
                        isIncrement = isIncrement
                    )
                }
                MatchType.CS -> {
                    getOddsList(
                        code,
                        matchType.postValue,
                        requestTimeRangeParams,
                        leagueIdList = leagueIdList,
                    )
                }
                MatchType.PARLAY -> {
                    getOddsList(
                        code,
                        matchType.postValue,
                        requestTimeRangeParams,
                        leagueIdList = leagueIdList,
                        isIncrement = isIncrement
                    )

                }
                MatchType.OUTRIGHT -> {
                    getOutrightOddsList(code)
                    //getOutrightSeasonList(code, false)
                }
                MatchType.AT_START -> {
                    getOddsList(
                        code,
                        matchType.postValue,
                        TimeUtil.getAtStartTimeRangeParams(),
                        leagueIdList = leagueIdList,
                        isIncrement = isIncrement
                    )
                }
                MatchType.EPS -> {
                    getEpsList(code, startTime = TimeUtil.getTodayStartTimeStamp())
                }
                MatchType.OTHER_OUTRIGHT -> {
                    //getOutrightSeasonList(code, true)
                    getOutrightOddsList(code)

                }
                MatchType.OTHER_EPS -> {

                }
                else -> {
                }
            }
        }

        _isNoHistory.postValue(sportCode == null)
    }

    fun getMatchCategoryQuery(matchType: MatchType) {
        viewModelScope.launch {
            getSportSelected(matchType)?.code?.let { gameType ->

                val result = doNetwork(androidContext) {
                    OneBoSportApi.matchCategoryService.getMatchCategoryQuery(
                        MatchCategoryQueryRequest(
                            gameType,
                            matchType.postValue,
                            TimeUtil.getNowTimeStamp().toString(),
                            TimeUtil.getTodayStartTimeStamp().toString()
                        )
                    )
                }

                _matchCategoryQueryResult.value = Event(result)
            }
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
                                    odd.leagueExpanded = matchOddNotNull.isExpanded
                                    odd.belongMatchOdd = matchOddNotNull
                                    odd.isExpand = true
                                    odd
                                } ?: listOf<Odd>())
                            //endregion

                            //region 顯示更多選項(大於五項才需要此功能)
//                            if (oddMap.value?.filterNotNull()?.size ?: 0 > 5) {
//                                //Triple(玩法key, MatchOdd, 該玩法是否需要展開)
//                                oddsList.add(
//                                    OutrightShowMoreItem(
//                                        oddMap.key,
//                                        matchOddNotNull,
//                                        playCateExpand,
//                                        isExpanded = false,
//                                        leagueExpanded = matchOddNotNull.isExpand
//                                    )
//                                )
//                            }
                            //endregion
                        }
                        //region 玩法標題
                        oddsList.add(
                            OutrightItem(
                                matchOdd = matchOddNotNull,
                                playCateCodeList = playCateCodeList,
                                subTitleList = subTitleList,
                                leagueExpanded = matchOddNotNull.isExpanded,
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

    private fun getOddsList(
        gameType: String,
        matchType: String,
        timeRangeParams: TimeRangeParams? = null,
        leagueIdList: List<String>? = null,
        matchIdList: List<String>? = null,
        isIncrement: Boolean = false,
    ) {
        var currentTimeRangeParams: TimeRangeParams? = null
        when (matchType) {
            MatchType.IN_PLAY.postValue, MatchType.AT_START.postValue, MatchType.OTHER.postValue -> {
                _oddsListResult.postValue(Event(null))
                currentTimeRangeParams = timeRangeParams
            }
            MatchType.TODAY.postValue, MatchType.CS.postValue, MatchType.EARLY.postValue, MatchType.PARLAY.postValue -> {
                _oddsListGameHallResult.value = Event(null)
                currentTimeRangeParams = timeRangeParams
            }
            else -> { // 特殊賽事要給特殊代碼 Ex: matchType: "sc:QAtest"
                _oddsListGameHallResult.value = Event(null)
                //currentTimeRangeParams = timeRangeParams
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
            val result = doNetwork(androidContext) {
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
            }?.updateMatchType()

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

                    if (isIncrement) {
                        _oddsListGameHallIncrementResult.postValue(
                            Event(
                                OddsListIncrementResult(
                                    leagueIdList,
                                    result
                                )
                            )
                        )
                    } else {
                        _oddsListGameHallResult.postValue(Event(result))
                    }
                }

                MatchType.OTHER.postValue -> {
                    if (isIncrement)
                        _oddsListGameHallIncrementResult.postValue(
                            Event(
                                OddsListIncrementResult(
                                    leagueIdList,
                                    result
                                )
                            )
                        )
                    else {
                        _oddsListResult.postValue(Event(result))
                    }
                }
                else -> {
                    _oddsListGameHallResult.postValue(Event(result))
                }
            }

            notifyFavorite(FavoriteType.MATCH)
        }
    }

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

    fun getLeagueList(
        gameType: String,
        matchType: String,
        timeRangeParams: TimeRangeParams?,
        date: String? = null,
        isIncrement: Boolean = false,
    ) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.leagueService.getLeagueList(
                    LeagueListRequest(
                        gameType,
                        matchType,
                        startTime = timeRangeParams?.startTime,
                        endTime = timeRangeParams?.endTime,
                        date = date
                    )
                )
            }


            if (isIncrement) {
                result?.rows?.forEach { row ->
                    row.list.forEach { league ->
                        league.isSelected = _leagueSelectedList.value?.contains(league) == true
                    }
                }
            } else
                clearSelectedLeague()

            _leagueListResult.value = (Event(result))

            notifyFavorite(FavoriteType.LEAGUE)
        }
    }

    private fun getLeagueList(
        gameType: String,
        matchType: String,
        startTime: String,
        endTime: String?,
        date: String? = null,
        isIncrement: Boolean = false,
    ) {

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.leagueService.getLeagueList(
                    LeagueListRequest(
                        gameType,
                        matchType,
                        startTime = startTime,
                        endTime = endTime,
                        date = date
                    )
                )
            }


            if (isIncrement) {
                result?.rows?.forEach { row ->
                    row.list.forEach { league ->
                        league.isSelected = _leagueSelectedList.value?.contains(league) == true
                    }
                }
            } else
                clearSelectedLeague()

            _leagueListResult.value = (Event(result))

            notifyFavorite(FavoriteType.LEAGUE)
        }
    }

    private fun getEpsList(
        gameType: String,
        matchType: String = MatchType.EPS.postValue,
        startTime: Long,
    ) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.oddsService.getEpsList(
                    OddsEpsListRequest(
                        gameType = gameType,
                        matchType = matchType,
                        startTime = startTime
                    )
                )
            }

            result?.rows?.forEach {
                it.leagueOdd.forEach { leagueOdds ->
                    leagueOdds?.matchOdds?.forEach { matchOddsItem ->
                        matchOddsItem.setupOddDiscount()
                        matchOddsItem.updateOddStatus()
                    }
                }
            }

            _epsListResult.postValue(Event(result))
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

    private fun getDateRowEarly(): List<Date> {
        val locale = LanguageManager.getSetLanguageLocale(androidContext)
        val dateRow = mutableListOf(
            Date(
                LocalUtils.getString(R.string.date_row_all),
                TimeUtil.getEarlyAllTimeRangeParams()
            ), Date(
                LocalUtils.getString(R.string.other),
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
                LocalUtils.getString(R.string.other),
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

    fun getCurrentTimeRangeParams(): TimeRangeParams? {
        return _curDate.value?.find {
            it.isSelected
        }?.timeRangeParams
    }

    fun selectLeague(league: League) {
        val list = _leagueSelectedList.value?.toMutableList() ?: mutableListOf()

        when (list.contains(league)) {
            true -> {
                list.remove(league)
            }
            false -> {
                list.add(league)
            }
        }

        _leagueSelectedList.postValue(list)
    }

    fun submitLeague() {
        _leagueSelectedList.value?.let {
            _leagueSubmitList.postValue(Event(it))
        }
        clearSelectedLeague()
    }

    fun filterLeague(leagueList: List<League>) {
        _leagueFilterList.postValue(leagueList)
        clearSelectedLeague()
    }

    fun clearSelectedLeague() {
        _leagueSelectedList.postValue(mutableListOf())
    }

    fun getOddsDetail(matchId: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.oddsService.getOddsDetail(OddsDetailRequest(matchId))
            }
            //MatchInfo中聯賽名稱為null, 為配合注單取用欄位, 將聯賽名稱塞入
            result?.oddsDetailData?.matchOdd?.matchInfo?.leagueName =
                result?.oddsDetailData?.league?.name

            _oddsDetailResult.postValue(Event(result))
            result?.success?.let { success ->
                val list: ArrayList<OddsDetailListData> = ArrayList()
                if (success) {
                    result.oddsDetailData?.matchOdd?.sortOddsMap()
                    result.oddsDetailData?.matchOdd?.odds?.forEach { (key, value) ->
                        betInfoRepository.betInfoList.value?.peekContent()?.let { list ->
                            value.odds.forEach { odd ->
                                odd?.isSelected = list.any {
                                    it.matchOdd.oddsId == odd?.id
                                }
                            }
                        }
                        val filteredOddList =
                            mutableListOf<Odd?>()
                        value.odds.forEach { detailOdd ->
                            //因排版問題 null也需要添加
                            filteredOddList.add(detailOdd)
                        }

                        val oddsDetail = OddsDetailListData(
                            key,
                            TextUtil.split(value.typeCodes),
                            value.name,
                            filteredOddList,
                            value.nameMap,
                            value.rowSort,
                            matchInfo = result.oddsDetailData?.matchOdd.matchInfo
                        )
                        //球員玩法邏輯
                        if (PlayCate.getPlayCate(key) == PlayCate.SCO) {
                            oddsDetail.setSCOTeamNameList(filteredOddList,
                                result.oddsDetailData.matchOdd.matchInfo.homeName)
                            oddsDetail.homeMap = setItemMap(filteredOddList,
                                result.oddsDetailData.matchOdd.matchInfo.homeName)
                            oddsDetail.awayMap = setItemMap(filteredOddList,
                                result.oddsDetailData.matchOdd.matchInfo.awayName)
                            oddsDetail.scoItem = oddsDetail.homeMap // default
                        }

                        list.add(oddsDetail)
                    }

                    result.oddsDetailData?.matchOdd?.odds?.sortPlayCate()
                    result.oddsDetailData?.matchOdd?.setupOddDiscount()
                    result.oddsDetailData?.matchOdd?.updateOddStatus()

                    //因UI需求 特優賠率移到第一項 需求先隱藏特優賠率
//                    list.find { it.gameType == PlayCate.EPS.value }.apply {
//                        if (this != null) {
//                            list.add(0, list.removeAt(list.indexOf(this)))
//                        }
//                    }

                    list.forEach {
                        it.originPosition = list.indexOf(it)
                    }

                    _oddsDetailList.postValue(Event(list))
                    //舊 動畫獲取url
                    /*if (MultiLanguagesApplication.getInstance()?.getGameDetailAnimationNeedShow() == true) {
                        val animationTrackerId = result.oddsDetailData?.matchOdd?.matchInfo?.trackerId
                        if (!animationTrackerId.isNullOrEmpty()) {
                            doNetwork(androidContext) {
                                OneBoSportApi.matchService.getMatchTrackerUrl(animationTrackerId)
                            }?.let { result ->
                                if (result.success) {
                                    _matchTrackerUrl.postValue(Event(result.matchTrackerUrl))
                                }
                            }
                        }
                    }*/
                    result.oddsDetailData?.matchOdd?.matchInfo?.let {
                        //賽事動畫網址
                        val eventId = it.trackerId
                        val screenWidth = MetricsUtil.getScreenWidth()
                        val animationHeight = (LiveUtil.getAnimationHeightFromWidth(screenWidth)).px
                        val languageParams =
                            LanguageManager.getLanguageString(MultiLanguagesApplication.appContext)

                        val videoUrl =
                            "${sConfigData?.sportStream}/animation/?matchId=${matchId}&lang=${languageParams}&mode=video"
                        val animeUrl =
                            "${sConfigData?.sportAnimation}/animation/?eventId=${eventId}&width=${screenWidth.pxToDp}&height=${animationHeight}&lang=${languageParams}&mode=widget"
                        _videoUrl.postValue(Event(videoUrl))
                        _animeUrl.postValue(Event(animeUrl))
                        notifyFavorite(FavoriteType.PLAY_CATE)
                        it.roundNo?.let {
                            getLiveInfo(it)
                        }
                    }
                }
            }
        }
    }

    private fun OddsDetailListData.setSCOTeamNameList(
        oddList: MutableList<Odd?>,
        homeName: String?,
    ) {
        val groupTeamName = oddList.groupBy {
            it?.extInfoMap?.get(LanguageManager.getSelectLanguage(androidContext).key)
        }.filterNot {
            it.key.isNullOrBlank()
        }
        val teamNameList = mutableListOf<String>().apply {
            groupTeamName.forEach {
                it.key?.let { key -> add(key) }
            }
        }.apply {
            if (firstOrNull() != homeName) reverse()
        }
        this.teamNameList = teamNameList
    }

    private fun setItemMap(
        oddList: MutableList<Odd?>,
        teamName: String?,
    ): HashMap<String, List<Odd?>> {
        //建立球員列表(一個球員三個賠率)
        var map: HashMap<String, List<Odd?>> = HashMap()

        //過濾掉 其他:(第一、任何、最後), 无進球
        //依隊名分開
        oddList.filterNot { odd ->
            odd?.playCode == OddSpreadForSCO.SCORE_1ST_O.playCode ||
                    odd?.playCode == OddSpreadForSCO.SCORE_ANT_O.playCode ||
                    odd?.playCode == OddSpreadForSCO.SCORE_LAST_O.playCode ||
                    odd?.playCode == OddSpreadForSCO.SCORE_N.playCode
        }.groupBy {
            it?.extInfoMap?.get(LanguageManager.getSelectLanguage(androidContext).key)
        }.forEach {
            if (it.key == teamName) {
                map = it.value.groupBy { odd -> odd?.name } as HashMap<String, List<Odd?>>
            }
        }
        //保留 其他:(第一、任何、最後), 无進球
        //依球員名稱分開
        //倒序排列 多的在前(無進球只有一種賠率 放最後面)
        //添加至球員列表內
        oddList.filter { odd ->
            odd?.playCode == OddSpreadForSCO.SCORE_1ST_O.playCode ||
                    odd?.playCode == OddSpreadForSCO.SCORE_ANT_O.playCode ||
                    odd?.playCode == OddSpreadForSCO.SCORE_LAST_O.playCode ||
                    odd?.playCode == OddSpreadForSCO.SCORE_N.playCode
        }.groupBy {
            it?.name
        }.entries.sortedByDescending {
            it.value.size
        }.associateBy(
            { it.key }, { it.value }
        ).forEach {
            map[it.key ?: ""] = it.value
        }
        return map
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

    private fun getSportCount(
        matchType: MatchType,
        gameType: GameType?,
        sportMenuResult: SportMenuResult? = null,
    ): Int {
        if (gameType == null)
            return 0

        val sportMenuRes = sportMenuResult ?: _sportMenuResult.value

        return when (matchType) {
            MatchType.IN_PLAY -> {
                sportMenuRes?.sportMenuData?.menu?.inPlay?.items?.find { it.code == gameType.key }?.num
                    ?: 0
            }
            MatchType.TODAY -> {
                sportMenuRes?.sportMenuData?.menu?.today?.items?.find { it.code == gameType.key }?.num
                    ?: 0
            }
            MatchType.EARLY -> {
                sportMenuRes?.sportMenuData?.menu?.early?.items?.find { it.code == gameType.key }?.num
                    ?: 0
            }
            MatchType.CS -> {
                sportMenuRes?.sportMenuData?.menu?.cs?.items?.find { it.code == gameType.key }?.num
                    ?: 0
            }
            MatchType.PARLAY -> {
                sportMenuRes?.sportMenuData?.menu?.parlay?.items?.find { it.code == gameType.key }?.num
                    ?: 0
            }
            MatchType.OUTRIGHT -> {
                sportMenuRes?.sportMenuData?.menu?.outright?.items?.find { it.code == gameType.key }?.num
                    ?: 0
            }
            MatchType.AT_START -> {
                sportMenuRes?.sportMenuData?.atStart?.items?.find { it.code == gameType.key }?.num
                    ?: 0
            }
            MatchType.EPS -> {
                sportMenuRes?.sportMenuData?.menu?.eps?.items?.find { it.code == gameType.key }?.num
                    ?: 0
            }
            else -> {
                0
            }
        }
    }

    private fun getSportSelected(matchType: MatchType): Item? = when (matchType) {
        MatchType.IN_PLAY -> {
            sportMenuResult.value?.sportMenuData?.menu?.inPlay?.items?.find { it.isSelected }
        }
        MatchType.TODAY -> {
            sportMenuResult.value?.sportMenuData?.menu?.today?.items?.find { it.isSelected }
        }
        MatchType.EARLY -> {
            sportMenuResult.value?.sportMenuData?.menu?.early?.items?.find { it.isSelected }
        }
        MatchType.CS -> {
            sportMenuResult.value?.sportMenuData?.menu?.cs?.items?.find { it.isSelected }
        }
        MatchType.PARLAY -> {
            sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.find { it.isSelected }
        }
        MatchType.OUTRIGHT -> {
            sportMenuResult.value?.sportMenuData?.menu?.outright?.items?.find { it.isSelected }
        }
        MatchType.AT_START -> {
            sportMenuResult.value?.sportMenuData?.atStart?.items?.find { it.isSelected }
        }
        MatchType.EPS -> {
            sportMenuResult.value?.sportMenuData?.menu?.eps?.items?.find { it.isSelected }
        }
        MatchType.OTHER -> {
            null
        }
        else -> null
    }

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
        MatchType.OTHER -> {
            specialMenuData?.items?.find { it.isSelected }?.code
        }
        MatchType.OTHER_OUTRIGHT -> {
            specialMenuData?.items?.find { it.isSelected }?.code
        }
        MatchType.MY_EVENT -> {
            specialMenuData?.items?.find { it.isSelected }?.code
        }
        else -> {
            null
        }
    }



    private fun SportQueryData.updateSportSelectState(
        gameTypeCode: String?,
    ): SportQueryData {
        this.items?.map { sport ->
            sport.isSelected = when {
                (gameTypeCode != null) -> {
                    sport.code == gameTypeCode
                }
                else -> {
                    this.items.indexOf(sport) == 0
                }
            }
        }
        return this
    }

    //20220507 原本除了matchType之外的玩法都會被調整至第一個球類, 調整為僅對matchType做選中球種更新
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

    /**
     * 根據記錄的賽事種類選中球種更新至新獲取的SportMenuResult資料
     * @see lastSportTypeHashMap
     */
    private fun SportMenuResult.setupSportSelectState() {
        this.sportMenuData?.let { menuData ->
            //滾球
            setupMatchTypeSelectState(MatchType.IN_PLAY, menuData.menu.inPlay)

            //今日
            setupMatchTypeSelectState(MatchType.TODAY, menuData.menu.today)

            //早盤
            setupMatchTypeSelectState(MatchType.EARLY, menuData.menu.early)

            //波胆
            setupMatchTypeSelectState(MatchType.CS, menuData.menu.cs)

            //串關
            setupMatchTypeSelectState(MatchType.PARLAY, menuData.menu.parlay)

            //冠軍
            setupMatchTypeSelectState(MatchType.OUTRIGHT, menuData.menu.outright)

            //即將開賽
            setupMatchTypeSelectState(MatchType.AT_START, menuData.atStart)

            //特優賠率 需求先隱藏特優賠率
//            menuData.menu.eps?.let { epsSport ->
//                setupMatchTypeSelectState(MatchType.EPS, epsSport)
//            }
        }
    }

    /**
     * 設置賽事種類選中球種狀態
     * @see setupSportSelectState
     */
    private fun setupMatchTypeSelectState(matchType: MatchType, matchTypeSport: Sport) {

        matchTypeSport.items.let { matchTypeItems ->
            matchTypeItems.forEach { sport ->
                sport.isSelected = when {
                    ((lastSportTypeHashMap[matchType.postValue]) == sport.code && sport.num > 0) -> true
                    else -> false
                }
            }
            if (!matchTypeItems.any { it.isSelected }) {
                matchTypeItems.firstOrNull { it.num > 0 }?.isSelected = true
            }
        }
    }

    private fun SportMenuResult.updateSportSelectState(
        matchType: MatchType?,
        gameTypeCode: String?,
    ) {
        this.sportMenuData?.updateSportSelectState(matchType, gameTypeCode)
        _sportMenuResult.postValue(this)
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

    private fun List<Odd?>.updateOddSelectState() {
        this.forEach { odd ->
            odd?.isSelected = betInfoRepository.betInfoList.value?.peekContent()
                ?.any { betInfoListData ->
                    betInfoListData.matchOdd.oddsId == odd?.id
                }
        }
    }

    private fun OddsListResult.updateQuickPlayCate(
        matchId: String,
        quickListData: QuickListData,
        quickPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    ): OddsListResult {
        this.oddsListData?.leagueOdds?.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                    val quickOddsApi = when (quickPlayCate.code) {
                        QuickPlayCate.QUICK_CORNERS.value, QuickPlayCate.QUICK_PENALTY.value, QuickPlayCate.QUICK_ADVANCE.value -> {
                            quickListData.quickOdds?.get(quickPlayCate.code)
                        }
                        else -> {
                            quickListData.quickOdds?.get(quickPlayCate.code)
                        }
                    }?.apply {
                        quickPlayCate.code?.let {
                            setupQuickPlayCate(quickPlayCate.code)
                            sortQuickPlayCate(quickPlayCate.code)
                        }
                    }

                    quickPlayCate.isSelected =
                        (quickPlayCate.isSelected && (matchOdd.matchInfo?.id == matchId))

                    quickPlayCate.quickOdds.putAll(
                        quickOddsApi?.toMutableFormat_1() ?: mutableMapOf()
                    )
                }
                matchOdd.quickPlayCateNameMap = quickPlayCateNameMap
            }
        }
        return this
    }

    private fun OddsListResult.clearQuickPlayCateSelected(): OddsListResult {
        this.oddsListData?.leagueOdds?.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                    quickPlayCate.isSelected = false
                }
            }
        }

        return this
    }

    /**
     * @param matchId 獲取直播的賽事id
     * @param getNewest 是否要獲取最新 true: api請求獲取最新的直播地址 false: 讀取暫存直播地址
     */
    fun getLiveInfo(roundNo: String) {
        viewModelScope.launch {
            var result = doNetwork(androidContext) {
                OneBoSportApi.matchService.getMatchLiveRound(roundNo)
            }
            result?.matchRound?.let {
                _matchLiveInfo.postValue(Event(it))
            }
        }

    }


    fun clearLiveInfo() {
        _matchLiveInfo.postValue(null)
        _oddsDetailResult.postValue(null)
    }

    //region 宣傳頁推薦賽事資料處理
    /**
     * 設置賽事類型參數(滾球、即將、今日、早盤,波胆)
     */
    private fun Recommend.setupMatchType() {
        matchType = when (status) {
            1 -> {
                matchInfo?.isInPlay = true
                MatchType.IN_PLAY
            }
            else -> {
                when {
                    TimeUtil.isTimeAtStart(startTime) -> {
                        matchInfo?.isAtStart = true
                        MatchType.AT_START
                    }
                    TimeUtil.isTimeToday(startTime) -> {
                        MatchType.TODAY
                    }
                    else -> {
                        MatchType.EARLY
                    }
                }
            }
        }
    }

    /**
     * 設置賽事時間參數
     */
    private fun Recommend.setupMatchTime() {
        matchInfo?.startDateDisplay = TimeUtil.timeFormat(matchInfo?.startTime, "MM/dd")

        matchInfo?.startTimeDisplay = TimeUtil.timeFormat(matchInfo?.startTime, "HH:mm")

        matchInfo?.remainTime = TimeUtil.getRemainTime(matchInfo?.startTime)
    }

    /**
     * 玩法數量設置，因matchInfo中沒有傳入playCateNum，故由外層代入
     */
    private fun Recommend.setupPlayCateNum() {
        matchInfo?.playCateNum = playCateNum
    }

    /**
     * 聯賽名稱，因matchInfo中後端沒有配置值，故由外層傳入
     */
    private fun Recommend.setupLeagueName() {
        matchInfo?.leagueName = leagueName
    }

    /**
     * 賽事狀態
     */
    private fun Recommend.setupSocketMatchStatus() {
        matchInfo?.let {
            /* 將賽事狀態(先前socket回傳取得)放入當前取得的賽事 */
            val status = _publicityRecommend.value?.peekContent()?.find { recommend ->
                recommend.leagueId == leagueId
            }?.matchInfo?.socketMatchStatus
            matchInfo?.socketMatchStatus = status
        }
    }

    /**
     * 設置大廳獲取的玩法排序、玩法名稱
     */
    private fun Recommend.setupOddsSort() {
        val nowGameType = gameType
        val playCateMenuCode = menuList.firstOrNull()?.code
        val oddsSortFilter = PlayCateMenuFilterUtils.filterOddsSort(nowGameType, playCateMenuCode)
        val playCateNameMapFilter =
            PlayCateMenuFilterUtils.filterPlayCateNameMap(nowGameType, playCateMenuCode)

        oddsSort = oddsSortFilter
        playCateNameMap = playCateNameMapFilter
    }
    //endregion

    //region 進入宣傳頁重新獲取config.json
    fun getConfigData() {
        //若是第一次啟動app則不再重新獲取一次config.json
        if (gotConfigData) {
            gotConfigData = false
            _gotConfig.postValue(Event(true))
            return
        }

        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.indexService.getConfig()
            }?.let { configResult ->
                if (configResult.success) {
                    sConfigData = configResult.configData
                    setupDefaultHandicapType()
                    _gotConfig.postValue(Event(true))
                }
            }
        }
    }

    private suspend fun thirdGameLogin(gameData: ThirdDictValues): ThirdLoginResult? {
        return doNetwork(androidContext) {
            OneBoSportApi.thirdGameService.thirdLogin(gameData.firmType, gameData.gameCode)
        }
    }

    private suspend fun autoTransfer(gameData: ThirdDictValues) {
        if (isThirdTransferOpen()) {
            //若自動轉換功能開啟，要先把錢都轉過去再進入遊戲
            val result = doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.autoTransfer(gameData.firmType)
            }
            if (result?.success == true) getMoney() //金額有變動，通知刷新
        }
    }

    //避免多次请求游戏
    var jumpingGame = false
    private fun requestEnterThirdGame(gameData: ThirdDictValues?) {
//        Timber.e("gameData: $gameData")
        when {
            gameData == null -> {
                _enterThirdGameResult.postValue(
                    EnterThirdGameResult(
                        resultType = EnterThirdGameResult.ResultType.FAIL,
                        url = null,
                        errorMsg = androidContext.getString(R.string.hint_game_maintenance)
                    )
                )
            }
            loginRepository.isLogin.value != true -> {
                _enterThirdGameResult.postValue(
                    EnterThirdGameResult(
                        resultType = EnterThirdGameResult.ResultType.NEED_REGISTER,
                        url = null,
                        errorMsg = null
                    )
                )
            }
            else -> {
                if (jumpingGame) {
                    return
                }
                jumpingGame = true
                viewModelScope.launch {
                    val thirdLoginResult = thirdGameLogin(gameData)

                    //20210526 result == null，代表 webAPI 處理跑出 exception，exception 處理統一在 BaseActivity 實作，這邊 result = null 直接略過
                    thirdLoginResult?.let {
                        if (it.success) {
                            //先调用三方游戏的登入接口, 确认返回成功200之后再接著调用自动转换额度的接口, 如果没有登入成功, 后面就不做额度自动转换的调用了
                            autoTransfer(gameData) //第三方自動轉換

                            _enterThirdGameResult.postValue(
                                EnterThirdGameResult(
                                    resultType = EnterThirdGameResult.ResultType.SUCCESS,
                                    url = thirdLoginResult.msg,
                                    thirdGameCategoryCode = gameData.gameCategory
                                )
                            )
                        } else {
                            _enterThirdGameResult.postValue(
                                EnterThirdGameResult(
                                    resultType = EnterThirdGameResult.ResultType.FAIL,
                                    url = null,
                                    errorMsg = thirdLoginResult?.msg
                                )
                            )
                        }
                        jumpingGame = false
                    }

                }
            }
        }

    }

    //20200302 記錄問題：新增一個 NONE type，來清除狀態，避免 fragment 畫面重啟馬上就會觸發 observe，重複開啟第三方遊戲
    fun clearThirdGame() {
        _enterThirdGameResult.postValue(
            EnterThirdGameResult(
                resultType = EnterThirdGameResult.ResultType.NONE,
                url = null,
                errorMsg = null
            )
        )
    }
    //endregion

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

    private suspend fun getMatchCategory(matchType: MatchType): MatchCategoryQueryResult? {
        return doNetwork(androidContext) {
            val gameType: String = getSportSelected(matchType)?.code ?: ""
            OneBoSportApi.matchCategoryService.getMatchCategoryQuery(
                MatchCategoryQueryRequest(
                    gameType,
                    matchType.postValue,
                    TimeUtil.getNowTimeStamp().toString(),
                    TimeUtil.getTodayStartTimeStamp().toString()
                )
            ).apply {
                if (isSuccessful && body()?.success == true) {
                    // 每次執行必做
                    _matchCategoryQueryResult.postValue(Event(body()))
                }
            }
        }
    }

    private suspend fun getSportQuery(matchType: MatchType): SportQueryResult? {
        return doNetwork(androidContext) {
            OneBoSportApi.sportService.getQuery(
                SportQueryRequest(
                    TimeUtil.getNowTimeStamp().toString(),
                    TimeUtil.getTodayStartTimeStamp().toString(),
                    matchType.postValue
                )
            ).apply {
                if (isSuccessful && body()?.success == true) {
                    // 每次執行必做
                    sportQueryData = body()?.sportQueryData
                    checkLastSportType(matchType, sportQueryData)
                }
            }
        }
    }

    private suspend fun getSportCouponMenu(): SportCouponMenuResult? {
        return doNetwork(androidContext) {
            sportMenuRepository.getSportCouponMenu()
        }
    }

    suspend fun getOddsList(
        gameType: String,
        matchType: String,
        leagueIdList: List<String>? = null,
    ): OddsListResult? {
        return doNetwork(androidContext) {
            OneBoSportApi.oddsService.getOddsList(
                OddsListRequest(
                    gameType,
                    matchType,
                    leagueIdList = leagueIdList,
                    playCateMenuCode = MenuCode.MAIN.code,
                )
            )
        }
    }

    private fun clearGameHallContent() {
        _curChildMatchType.postValue(null)           // 清除今日、早盤、串關第二層match type (賽事、冠軍)
        _oddsListGameHallResult.postValue(Event(null))
        _oddsListResult.postValue(Event(null))
        filterLeague(listOf())
    }

    private fun clearGameHallContentBySwitchGameType() {
        _playList.postValue(Event(listOf()))
        _playCate.postValue(Event(null))

        if (_curMatchType.value == MatchType.TODAY ||
            _curMatchType.value == MatchType.EARLY ||
            _curMatchType.value == MatchType.CS ||
            _curMatchType.value == MatchType.PARLAY
        ) {
            _curChildMatchType.postValue(_curMatchType.value)  // 清除今日、早盤、串關設定預設match type
        } else {
            _curChildMatchType.postValue(null)           // 清除今日、早盤、串關第二層match type (賽事、冠軍)
        }
        _oddsListGameHallResult.postValue(Event(null))
        _oddsListResult.postValue(Event(null))
        filterLeague(listOf())
    }

    fun setCurMatchType(matchType: MatchType?) {
        _curMatchType.postValue(matchType)
    }

    fun switchMatchType(matchType: MatchType?) {
        clearGameHallContent()
        when (matchType) {
            /* 初次進入主頁 */
            null -> {
                viewModelScope.launch {
                    getSportMenuAll()?.let {
                        if (it.success) {
                            it.setupSportSelectState()         // 根據lastSportTypeHashMap設置賽事種類選中球種狀態
                            _sportMenuResult.postValue(it)     // 更新大廳上方球種數量、各MatchType下球種和數量
                        }
                    }
                }
            }

            /* 滾球、即將、今日、早盤、冠軍、串關、其他 */
            else -> {
                viewModelScope.launch {
                    val sportMenuResult = getSportMenuAll()
                    sportMenuResult?.let {
                        if (it.success) {
                            it.setupSportSelectState()         // 根據lastSportTypeHashMap設置賽事種類選中球種狀態
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
        }
    }

    private suspend fun updateSportInfo(matchType: MatchType) {
        getSportQuery(matchType)?.let {
            if (!it.success) {
                _showErrorDialogMsg.postValue(it.msg)
                return
            }
        }
        setCurMatchType(matchType)

        // 無數量直接顯示無資料UI
        if (getMatchCount(matchType) < 1) {
            _isNoEvents.postValue(true)
            return
        }

        getSportCouponMenu()?.let {
            _sportCouponMenuResult.postValue(Event(it))
        }
    }

    fun switchGameType(item: Item) {
        if (jobSwitchGameType?.isActive == true) {
            jobSwitchGameType?.cancel()
        }

        val matchType = curMatchType.value ?: return

        //視覺上需要優先跳轉 tab
        _sportMenuResult.value?.updateSportSelectState(matchType, item.code)

        jobSwitchGameType = viewModelScope.launch {
            //原有邏輯暫時不動
            if (matchType == MatchType.OTHER) {
                switchSportType(matchType, item)
                return@launch
            }

            getSportQuery(matchType)?.let {
                if (!it.success) {
                    _showErrorDialogMsg.postValue(it.msg)
                    return@launch
                }
            }

            // 無數量直接顯示無資料UI
            if (getMatchCount(matchType) < 1) {
                _isNoEvents.postValue(true)
                return@launch
            }

//            // 今日、串關頁面下賽事選擇 (今日、所有)
//            if (matchType == MatchType.TODAY || matchType == MatchType.PARLAY) {
//                getMatchCategory(matchType)
//            }

            clearGameHallContentBySwitchGameType()
            recordSportType(matchType, item.code)

            getGameHallList(matchType, true, isReloadPlayCate = true)
        }
    }

    private var jobSwitchGameType: Job? = null

    private fun checkOddsList(
        gameType: String,
        matchTypeString: String,
        leagueChangeEvent: LeagueChangeEvent?,
    ) {
        viewModelScope.launch {

            val matchType = curMatchType.value ?: return@launch

            /* 1. 刷新上方選單 */
            val sportMenuResult = getSportMenuAll()
            sportMenuResult?.let {
                if (it.success) {
                    it.setupSportSelectState()         // 根據lastSportTypeHashMap設置賽事種類選中球種狀態
                    _sportMenuResult.postValue(it)     // 更新大廳上方球種數量、各MatchType下球種和數量
                } else {
                    return@launch
                }
            }

            getSportQuery(matchType)?.let {
                if (!it.success) {
                    _showErrorDialogMsg.postValue(it.msg)
                    return@launch
                }
            }

            // 無數量直接顯示無資料UI
            if (getMatchCount(matchType) < 1) {
                _isNoEvents.postValue(true)
                return@launch
            }

            getSportCouponMenu()?.let {
                _sportCouponMenuResult.postValue(Event(it))
            }

            // 今日、串關頁面下賽事選擇 (今日、所有)
            if (matchType == MatchType.TODAY || matchType == MatchType.PARLAY) {
                getMatchCategory(matchType)
            }

            // 如選擇球種的線程正在執行 則不需執行 league change 後的流程
            if (jobSwitchGameType?.isActive == true) return@launch

            /* 2. 確認 league change 聯賽列表有無 */
            val oddsListResult = getOddsList(gameType,
                matchTypeString,
                leagueIdList = leagueChangeEvent?.leagueIdList)?.apply {
                let {
                    if (!it.success) {
                        return@launch
                    }
                }
            } ?: return@launch

            if (oddsListResult.oddsListData?.leagueOdds.isNullOrEmpty()) return@launch

        }
    }

    /**
     * 更新翻譯
     */
    private fun OddsListData?.getPlayCateNameMap(matchType: String) {
        this?.leagueOdds?.onEach { LeagueOdd ->
            LeagueOdd.matchOdds.onEach { matchOdd ->
                if (matchType == MatchType.CS.postValue) {
                    matchOdd.playCateNameMap =
                        PlayCateMenuFilterUtils.filterList?.get(GameType.FT.name)
                            ?.get(PlayCate.CS.value)?.playCateNameMap
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

    fun loginLive() {
        if (sConfigData?.liveChatOpen == 0) return
        var spf = MultiLanguagesApplication.mInstance.getSharedPreferences(NAME_LOGIN,
            Context.MODE_PRIVATE)
        var spValue = spf.getString(KEY_LIVE_USER_INFO, "")
        if (spValue.isNullOrEmpty()) return
        var liveSyncUserInfoVO = Gson().fromJson(spValue, LiveSyncUserInfoVO::class.java)
        if (liveSyncUserInfoVO == null) return
        val hostUrl = sConfigData?.liveChatHost
        if (hostUrl.isNullOrEmpty()) return
        viewModelScope.launch {
            hostUrl?.let {
                val retrofit = RequestManager.instance.createRetrofit(hostUrl)
                val result = doNetwork(androidContext) {
                    retrofit.create(MatchService::class.java).liveLogin(
                        liveSyncUserInfoVO
                    )
                }
                result?.chatLiveLoginData?.let {
                    _liveLoginInfo.postValue(Event(it))
                }
            }
        }
    }
}