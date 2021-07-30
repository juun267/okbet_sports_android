package org.cxct.sportlottery.ui.game

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.league.LeagueListRequest
import org.cxct.sportlottery.network.league.LeagueListResult
import org.cxct.sportlottery.network.league.Row
import org.cxct.sportlottery.network.match.MatchPreloadRequest
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.matchCategory.MatchCategoryRequest
import org.cxct.sportlottery.network.matchCategory.MatchRecommendRequest
import org.cxct.sportlottery.network.matchCategory.result.MatchCategoryResult
import org.cxct.sportlottery.network.matchCategory.result.MatchRecommendResult
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.detail.OddsDetailRequest
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.odds.list.*
import org.cxct.sportlottery.network.odds.quick.QuickListData
import org.cxct.sportlottery.network.odds.quick.QuickListRequest
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListRequest
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListResult
import org.cxct.sportlottery.network.outright.season.OutrightSeasonListRequest
import org.cxct.sportlottery.network.outright.season.OutrightSeasonListResult
import org.cxct.sportlottery.network.playcate.PlayCateListResult
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.order_settlement.OrderSettlementEvent
import org.cxct.sportlottery.network.service.order_settlement.SportBet
import org.cxct.sportlottery.network.service.order_settlement.Status
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.network.sport.query.SportQueryData
import org.cxct.sportlottery.network.sport.query.SportQueryRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseFavoriteViewModel
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.data.Date
import org.cxct.sportlottery.ui.game.data.SpecialEntrance
import org.cxct.sportlottery.ui.game.data.SpecialEntranceSource
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.odds.OddsDetailListData
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.TimeUtil.DMY_FORMAT
import org.cxct.sportlottery.util.TimeUtil.HM_FORMAT
import org.cxct.sportlottery.util.TimeUtil.getTodayTimeRangeParams
import java.util.*
import kotlin.collections.ArrayList


class GameViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    private val sportMenuRepository: SportMenuRepository,
    private val thirdGameRepository: ThirdGameRepository,
) : BaseFavoriteViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository
) {

    val showBetInfoSingle = betInfoRepository.showBetInfoSingle

    val betInfoList = betInfoRepository.betInfoList

    val matchOddList: LiveData<MutableList<org.cxct.sportlottery.network.bet.info.MatchOdd>>
        get() = betInfoRepository.matchOddList

    val parlayList: LiveData<MutableList<ParlayOdd>>
        get() = betInfoRepository.parlayList

    val gameCateDataList by lazy { thirdGameRepository.gameCateDataList }

    val messageListResult: LiveData<MessageListResult?>
        get() = _messageListResult

    val curMatchType: LiveData<MatchType?>
        get() = _curMatchType

    val sportMenuResult: LiveData<SportMenuResult?>
        get() = _sportMenuResult

    val oddsListGameHallResult: LiveData<Event<OddsListResult?>>
        get() = _oddsListGameHallResult

    val oddsListResult: LiveData<Event<OddsListResult?>>
        get() = _oddsListResult

    val leagueListResult: LiveData<Event<LeagueListResult?>>
        get() = _leagueListResult

    val outrightSeasonListResult: LiveData<Event<OutrightSeasonListResult?>>
        get() = _outrightSeasonListResult

    val outrightOddsListResult: LiveData<Event<OutrightOddsListResult?>>
        get() = _outrightOddsListResult

    val countryListSearchResult: LiveData<List<Row>>
        get() = _countryListSearchResult

    val outrightCountryListSearchResult: LiveData<List<org.cxct.sportlottery.network.outright.season.Row>>
        get() = _outrightCountryListSearchResult

    val leagueListSearchResult: LiveData<List<LeagueOdd>>
        get() = _leagueListSearchResult

    val curDate: LiveData<List<Date>>
        get() = _curDate

    val curDatePosition: LiveData<Int>
        get() = _curDatePosition

    val isNoHistory: LiveData<Boolean>
        get() = _isNoHistory

    val settlementNotificationMsg: LiveData<Event<SportBet>>
        get() = _settlementNotificationMsg

    val errorPromptMessage: LiveData<Event<String>>
        get() = _errorPromptMessage

    val specialEntrance: LiveData<SpecialEntrance?>
        get() = _specialEntrance

    val asStartCount: LiveData<Int> //即將開賽的數量
        get() = _asStartCount

    val leagueSelectedList: LiveData<List<League>>
        get() = _leagueSelectedList

    val leagueSubmitList: LiveData<Event<List<League>>>
        get() = _leagueSubmitList

    val playList: LiveData<List<Play>>
        get() = _playList

    val playCate: LiveData<String?>
        get() = _playCate

    private val _messageListResult = MutableLiveData<MessageListResult?>()
    private val _curMatchType = MutableLiveData<MatchType?>()
    private val _sportMenuResult = MutableLiveData<SportMenuResult?>()
    private val _oddsListGameHallResult = MutableLiveData<Event<OddsListResult?>>()
    private val _oddsListResult = MutableLiveData<Event<OddsListResult?>>()
    private val _leagueListResult = MutableLiveData<Event<LeagueListResult?>>()
    private val _outrightSeasonListResult = MutableLiveData<Event<OutrightSeasonListResult?>>()
    private val _outrightOddsListResult = MutableLiveData<Event<OutrightOddsListResult?>>()
    private val _countryListSearchResult = MutableLiveData<List<Row>>()
    private val _leagueListSearchResult = MutableLiveData<List<LeagueOdd>>()
    private val _curDate = MutableLiveData<List<Date>>()
    private val _curDatePosition = MutableLiveData<Int>()
    private val _asStartCount = MutableLiveData<Int>()
    private val _isNoHistory = MutableLiveData<Boolean>()
    private val _settlementNotificationMsg = MutableLiveData<Event<SportBet>>()
    private val _errorPromptMessage = MutableLiveData<Event<String>>()
    private val _specialEntrance = MutableLiveData<SpecialEntrance?>()
    private val _outrightCountryListSearchResult =
        MutableLiveData<List<org.cxct.sportlottery.network.outright.season.Row>>()
    private val _leagueSelectedList = MutableLiveData<List<League>>()
    private val _leagueSubmitList = MutableLiveData<Event<List<League>>>()
    private val _playList = MutableLiveData<List<Play>>()
    private val _playCate = MutableLiveData<String?>()

    private val _matchPreloadInPlay = MutableLiveData<Event<MatchPreloadResult>>()
    val matchPreloadInPlay: LiveData<Event<MatchPreloadResult>>
        get() = _matchPreloadInPlay

    private val _matchPreloadAtStart = MutableLiveData<Event<MatchPreloadResult>>()
    val matchPreloadAtStart: LiveData<Event<MatchPreloadResult>>
        get() = _matchPreloadAtStart

    private val _highlightMenuResult = MutableLiveData<Event<MatchCategoryResult>>()
    val highlightMenuResult: LiveData<Event<MatchCategoryResult>>
        get() = _highlightMenuResult

    private val _recommendMatchResult = MutableLiveData<Event<MatchRecommendResult>>()
    val recommendMatchResult: LiveData<Event<MatchRecommendResult>>
        get() = _recommendMatchResult

    private val _highlightMatchResult = MutableLiveData<Event<MatchCategoryResult>>()
    val highlightMatchResult: LiveData<Event<MatchCategoryResult>>
        get() = _highlightMatchResult

    private val _allFootballCount = MutableLiveData<Int>()
    val allFootballCount: LiveData<Int> //全部足球比賽的數量
        get() = _allFootballCount

    private val _allBasketballCount = MutableLiveData<Int>()
    val allBasketballCount: LiveData<Int> //全部籃球比賽的數量
        get() = _allBasketballCount

    private val _allTennisCount = MutableLiveData<Int>()
    val allTennisCount: LiveData<Int> //全部網球比賽的數量
        get() = _allTennisCount

    private val _allBadmintonCount = MutableLiveData<Int>()
    val allBadmintonCount: LiveData<Int> //全部羽毛球比賽的數量
        get() = _allBadmintonCount

    private val _allVolleyballCount = MutableLiveData<Int>()
    val allVolleyballCount: LiveData<Int> //全部排球比賽的數量
        get() = _allVolleyballCount

    private val _betInfoResult = MutableLiveData<Event<BetInfoResult?>>()
    val betInfoResult: LiveData<Event<BetInfoResult?>>
        get() = _betInfoResult

    private val _oddsDetailResult = MutableLiveData<Event<OddsDetailResult?>>()
    val oddsDetailResult: LiveData<Event<OddsDetailResult?>>
        get() = _oddsDetailResult

    private val _playCateListResult = MutableLiveData<Event<PlayCateListResult?>>()
    val playCateListResult: LiveData<Event<PlayCateListResult?>>
        get() = _playCateListResult

    private val _oddsDetailList = MutableLiveData<Event<ArrayList<OddsDetailListData>>>()
    val oddsDetailList: LiveData<Event<ArrayList<OddsDetailListData>>>
        get() = _oddsDetailList

    //Loading
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private var _isLoading = MutableLiveData<Boolean>()

    private var sportQueryData: SportQueryData? = null


    fun navSpecialEntrance(
        source: SpecialEntranceSource,
        matchType: MatchType,
        gameType: GameType?
    ) = when (source) {
        SpecialEntranceSource.HOME -> {
            getSpecEntranceFromHome(matchType, gameType)
        }
        SpecialEntranceSource.LEFT_MENU -> {
            getSpecEntranceFromLeftMenu(matchType, gameType)
        }
        SpecialEntranceSource.SHOPPING_CART -> {
            SpecialEntrance(matchType, gameType)
        }
    }?.let {
        _specialEntrance.postValue(it)
    }

    private fun getSpecEntranceFromHome(
        matchType: MatchType,
        gameType: GameType?
    ): SpecialEntrance? = when {
        matchType == MatchType.IN_PLAY && getSportCount(matchType, gameType) == 0 -> {
            _errorPromptMessage.postValue(Event(androidContext.getString(R.string.message_no_in_play)))
            null
        }
        matchType == MatchType.AT_START && getMatchCount(matchType) == 0 -> {
            _errorPromptMessage.postValue(Event(androidContext.getString(R.string.message_no_at_start)))
            null
        }
        else -> {
            SpecialEntrance(matchType, gameType)
        }
    }

    private fun getSpecEntranceFromLeftMenu(
        matchType: MatchType,
        gameType: GameType?
    ): SpecialEntrance? = when {
        getSportCount(matchType, gameType) != 0 -> {
            SpecialEntrance(matchType, gameType)
        }
        getSportCount(MatchType.TODAY, gameType) != 0 -> {
            SpecialEntrance(MatchType.TODAY, gameType)
        }
        getSportCount(MatchType.EARLY, gameType) != 0 -> {
            SpecialEntrance(MatchType.EARLY, gameType)
        }
        gameType != null -> {
            SpecialEntrance(MatchType.PARLAY, gameType)
        }
        else -> {
            null
        }
    }

    fun isParlayPage(isParlayPage: Boolean) {
        betInfoRepository._isParlayPage.postValue(isParlayPage)
        if (isParlayPage)
            checkShoppingCart()
    }

    fun switchMatchType(matchType: MatchType) {
        betInfoRepository._isParlayPage.postValue(matchType == MatchType.PARLAY)
        if (matchType == MatchType.PARLAY) {
            checkShoppingCart()
        }

        getSportMenu(matchType)
        getAllPlayCategory(matchType)
    }

    private fun checkShoppingCart() {
        val betList = betInfoList.value?.peekContent() ?: mutableListOf()
        val parlayList = betList.cleanOutrightBetOrder().groupBetInfoByMatchId()

        if (betList.size != parlayList.size) {
            betList.minus(parlayList.toHashSet()).forEach {
                removeBetInfoItem(it.matchOdd.oddsId)
            }

            _errorPromptMessage.postValue(Event(androidContext.getString(R.string.bet_info_system_close_incompatible_item)))
        }
    }

    private fun MutableList<BetInfoListData>.cleanOutrightBetOrder() = this.filter {
        it.matchType != MatchType.OUTRIGHT
    }.toMutableList()

    private fun MutableList<BetInfoListData>.groupBetInfoByMatchId() = this.groupBy { data ->
        this.find { d -> data.matchOdd.matchId == d.matchOdd.matchId }
    }.mapNotNull {
        it.key
    }.toMutableList()

    //獲取系統公告
    fun getAnnouncement() {
        if (isLogin.value == true) {
            viewModelScope.launch {
                doNetwork(androidContext) {
                    val typeList = arrayOf(1)
                    OneBoSportApi.messageService.getPromoteNotice(typeList)
                }?.let { result -> _messageListResult.postValue(result) }
            }
        } else {
            _messageListResult.value = null
        }
    }

    //獲取體育菜單
    fun getSportMenu() {
        getSportMenu(null)
    }

    private fun getSportMenu(matchType: MatchType?) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                sportMenuRepository.getSportMenu(
                    TimeUtil.getNowTimeStamp().toString(),
                    TimeUtil.getTodayStartTimeStamp().toString()
                )
            }
            postHomeCardCount(result)

            result?.let {
                it.sportMenuData?.sortSport()
                it.updateSportSelectState(
                    specialEntrance.value?.matchType,
                    specialEntrance.value?.gameType?.key
                ).run {
                    _specialEntrance.value = null
                }
            }
            _curMatchType.value = matchType
        }
        _isLoading.value = false
    }

    private fun getAllPlayCategory(matchType: MatchType) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.sportService.getQuery(
                    SportQueryRequest(
                        TimeUtil.getNowTimeStamp().toString(),
                        TimeUtil.getTodayStartTimeStamp().toString(),
                        matchType.postValue
                    )
                )
            }

            sportQueryData = result?.sportQueryData
        }
    }

    private fun postHomeCardCount(sportMenuResult: SportMenuResult?) {
        _asStartCount.postValue(
            getMatchCount(
                MatchType.AT_START,
                sportMenuResult
            )
        )
        _allFootballCount.postValue(
            getSportCount(
                MatchType.TODAY,
                GameType.FT,
                sportMenuResult
            )
        )
        _allBasketballCount.postValue(
            getSportCount(
                MatchType.TODAY,
                GameType.BK,
                sportMenuResult
            )
        )
        _allTennisCount.postValue(
            getSportCount(
                MatchType.TODAY,
                GameType.TN,
                sportMenuResult
            )
        )
        _allVolleyballCount.postValue(
            getSportCount(
                MatchType.TODAY,
                GameType.VB,
                sportMenuResult
            )
        )
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
        this.menu.parlay.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.outright.items.sortedBy { sport ->
            sport.sortNum
        }
        this.atStart.items.sortedBy { sport ->
            sport.sortNum
        }

        return this
    }

    //遊戲大廳首頁: 滾球盤、即將開賽盤
    fun getMatchPreload() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.matchService.getMatchPreload(
                    MatchPreloadRequest(MatchType.IN_PLAY.postValue)
                )
            }?.let { result ->
                //mapping 下注單裡面項目 & 賠率按鈕 選擇狀態
                result.matchPreloadData?.datas?.forEach { data ->
                    data.matchOdds.forEach { matchOdd ->
                        matchOdd.odds.forEach { map ->
                            map.value.forEach { odd ->
                                odd?.isSelected =
                                    betInfoRepository.betInfoList.value?.peekContent()?.any {
                                        it.matchOdd.oddsId == odd?.id
                                    }
                            }
                        }
                    }
                }

                _matchPreloadInPlay.postValue(Event(result))
            }
        }
        viewModelScope.launch {
            doNetwork(androidContext) {
                //即將開賽 query 參數：
                //"matchType": "ATSTART",
                //"startTime": 現在時間戳,
                //"endTime": 一小時後時間戳
                val startTime = System.currentTimeMillis()
                val endTime = startTime + 60 * 60 * 1000
                OneBoSportApi.matchService.getMatchPreload(
                    MatchPreloadRequest(
                        MatchType.AT_START.postValue,
                        startTime = startTime.toString(),
                        endTime = endTime.toString()
                    )
                )
            }?.let { result ->
                result.matchPreloadData?.datas?.forEach { data ->
                    data.matchOdds.forEach { matchOdd ->
                        //計算且賦值 即將開賽 的倒數時間
                        matchOdd.matchInfo?.apply {
                            remainTime = TimeUtil.getRemainTime(startTime.toLong())
                        }

                        //mapping 下注單裡面項目 & 賠率按鈕 選擇狀態
                        matchOdd.odds.forEach { map ->
                            map.value.forEach { odd ->
                                odd?.isSelected =
                                    betInfoRepository.betInfoList.value?.peekContent()?.any {
                                        it.matchOdd.oddsId == odd?.id
                                    }
                            }
                        }
                    }
                }

                _matchPreloadAtStart.postValue(Event(result))
            }
        }
    }

    //遊戲大廳首頁: 精選賽事菜單
    fun getHighlightMenu() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.matchCategoryService.getHighlightMenu(
                    MatchCategoryRequest(
                        startTime = TimeUtil.getTodayStartTimeStamp(),
                        endTime = TimeUtil.getTodayEndTimeStamp()
                    )
                )
            }?.let { result ->
                _highlightMenuResult.postValue(Event(result))
            }
        }
    }

    fun getRecommendMatch() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.matchCategoryService.getRecommendMatch(MatchRecommendRequest())
            }?.let { result ->
                //mapping 下注單裡面項目 & 賠率按鈕 選擇狀態
                result.rows?.forEach { row ->
                    row.leagueOdds?.matchOdds?.forEach { oddData ->
                        oddData.odds?.forEach { map ->
                            map.value.forEach { odd ->
                                odd.isSelected =
                                    betInfoRepository.betInfoList.value?.peekContent()?.any {
                                        it.matchOdd.oddsId == odd.id
                                    }
                            }
                        }
                    }
                }

                _recommendMatchResult.postValue(Event(result))
            }
        }
    }

    //遊戲大廳首頁: 精選賽事資料
    fun getHighlightMatch(gameType: String) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.matchCategoryService.getHighlightMatch(
                    MatchCategoryRequest(
                        startTime = TimeUtil.getTodayStartTimeStamp(),
                        endTime = TimeUtil.getTodayEndTimeStamp(),
                        gameType = gameType
                    )
                )
            }?.let { result ->
                //mapping 下注單裡面項目 & 賠率按鈕 選擇狀態
                result.t?.odds?.forEach { oddData ->
                    oddData.odds?.forEach { map ->
                        map.value.forEach { odd ->
                            odd.isSelected =
                                betInfoRepository.betInfoList.value?.peekContent()?.any {
                                    it.matchOdd.oddsId == odd.id
                                }
                        }
                    }
                }

                _highlightMatchResult.postValue(Event(result))
            }
        }
    }

    fun switchSportType(matchType: MatchType, item: Item) {
        _sportMenuResult.value?.updateSportSelectState(matchType, item.code)

        getGameHallList(matchType, true, isReloadPlayCate = true)
    }

    fun switchPlay(matchType: MatchType, play: Play) {
        updatePlaySelectedState(play)

        getGameHallList(matchType, false)
    }

    fun switchPlayCategory(matchType: MatchType, playCateCode: String?) {
        _playCate.value = playCateCode

        getGameHallList(matchType, false)
    }

    fun switchMatchDate(matchType: MatchType, date: Date) {
        _curDate.value?.updateDateSelectedState(date)

        getGameHallList(matchType, false, date.date)
    }

    fun getGameHallList(
        matchType: MatchType,
        isReloadDate: Boolean,
        date: String? = null,
        isReloadPlayCate: Boolean = false
    ) {
        if (isReloadPlayCate) {
            getPlayCategory(matchType)
        }

        if (isReloadDate) {
            getDateRow(matchType)
        }

        val sportItem = getSportSelected(matchType)

        sportItem?.let { item ->
            when (matchType) {
                MatchType.IN_PLAY -> {
                    getOddsList(item.code, matchType.postValue)
                }
                MatchType.TODAY -> {
                    getLeagueList(
                        item.code,
                        matchType.postValue,
                        getCurrentTimeRangeParams()
                    )
                }
                MatchType.EARLY -> {
                    getLeagueList(
                        item.code,
                        matchType.postValue,
                        getCurrentTimeRangeParams()
                    )
                }
                MatchType.PARLAY -> {
                    getLeagueList(
                        item.code,
                        matchType.postValue,
                        getCurrentTimeRangeParams(),
                        date
                    )

                }
                MatchType.OUTRIGHT -> {
                    getOutrightSeasonList(item.code)
                }
                MatchType.AT_START -> {
                    getOddsList(
                        item.code,
                        matchType.postValue,
                        getCurrentTimeRangeParams()
                    )
                }
            }
        }

        _isNoHistory.postValue(sportItem == null)
    }

    fun switchPlay(matchType: MatchType, leagueIdList: List<String>, play: Play) {
        updatePlaySelectedState(play)

        getLeagueOddsList(matchType, leagueIdList)
    }

    fun switchPlayCategory(
        matchType: MatchType,
        leagueIdList: List<String>,
        playCateCode: String?
    ) {
        _playCate.value = playCateCode

        getLeagueOddsList(matchType, leagueIdList)
    }

    fun getLeagueOddsList(
        matchType: MatchType,
        leagueIdList: List<String>,
        isReloadPlayCate: Boolean = false
    ) {

        if (isReloadPlayCate) {
            getPlayCategory(matchType)
        }

        getSportSelected(matchType)?.let { item ->
            getOddsList(
                item.code,
                matchType.postValue,
                getCurrentTimeRangeParams(),
                leagueIdList
            )
        }
    }

    fun getOutrightOddsList(leagueId: String) {
        getSportSelected(MatchType.OUTRIGHT)?.let { item ->
            viewModelScope.launch {
                val result = doNetwork(androidContext) {
                    OneBoSportApi.outrightService.getOutrightOddsList(
                        OutrightOddsListRequest(
                            item.code,
                            leagueIdList = listOf(leagueId)
                        )
                    )
                }

                result?.outrightOddsListData?.leagueOdds?.forEach { leagueOdd ->
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.odds.values.forEach { oddList ->
                            oddList.updateOddSelectState()
                        }
                    }
                }

                val matchOdd = result?.outrightOddsListData?.leagueOdds?.get(0)?.matchOdds?.get(0)
                matchOdd?.let {
                    matchOdd.startDate = TimeUtil.timeFormat(it.matchInfo.startTime.toLong(), DMY_FORMAT)
                    matchOdd.startTime = TimeUtil.timeFormat(it.matchInfo.startTime.toLong(), HM_FORMAT)
                }

                _outrightOddsListResult.postValue(Event(result))
            }
        }
    }

    fun getOddsList(
        gameType: String,
        matchType: String,
        timeRangeParams: TimeRangeParams? = null,
        leagueIdList: List<String>? = null
    ) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.oddsService.getOddsList(
                    OddsListRequest(
                        gameType,
                        matchType,
                        leagueIdList = leagueIdList,
                        startTime = timeRangeParams?.startTime,
                        endTime = timeRangeParams?.endTime,
                        playCateMenuCode = getPlayCateSelected()?.code ?: "",
                        playCateCodeList = getPlayCateCodeList()
                    )
                )
            }

            result?.oddsListData?.leagueOdds?.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.matchInfo?.let { matchInfo ->
                        matchInfo.startDateDisplay =
                            TimeUtil.timeFormat(matchInfo.startTime.toLong(), "MM/dd")

                        matchOdd.matchInfo.startTimeDisplay =
                            TimeUtil.timeFormat(matchInfo.startTime.toLong(), "HH:mm")

                        matchInfo.remainTime = TimeUtil.getRemainTime(matchInfo.startTime.toLong())
                    }

                    matchOdd.odds =
                        PlayCateUtils.filterOdds(matchOdd.odds, result.oddsListData.sport.code)
                    matchOdd.odds.forEach { map ->
                        map.value.updateOddSelectState()
                    }
                }
            }

            if (leagueIdList != null) {
                _oddsListResult.postValue(Event(result))
            } else {
                _oddsListGameHallResult.postValue(Event(result))
            }
        }
    }

    fun getQuickList(matchId: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.oddsService.getQuickList(
                    QuickListRequest(matchId)
                )
            }

            result?.quickListData?.let {
                _oddsListGameHallResult.postValue(
                    Event(
                        _oddsListGameHallResult.value?.peekContent()
                            ?.updateQuickPlayCate(matchId, it)
                    )
                )

                _oddsListResult.postValue(
                    Event(
                        _oddsListResult.value?.peekContent()
                            ?.updateQuickPlayCate(matchId, it)
                    )
                )
            }
        }
    }

    fun clearQuickPlayCateSelected() {
        _oddsListGameHallResult.postValue(
            Event(
                _oddsListGameHallResult.value?.peekContent()?.clearQuickPlayCateSelected()
            )
        )

        _oddsListResult.postValue(
            Event(
                _oddsListResult.value?.peekContent()?.clearQuickPlayCateSelected()
            )
        )
    }

    private fun getLeagueList(
        gameType: String,
        matchType: String,
        timeRangeParams: TimeRangeParams?,
        date: String? = null
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

            _leagueListResult.value = (Event(result))

            _leagueSelectedList.postValue(mutableListOf())

            notifyFavorite(FavoriteType.LEAGUE)
        }
    }

    private fun getOutrightSeasonList(gameType: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.outrightService.getOutrightSeasonList(
                    OutrightSeasonListRequest(gameType)
                )
            }

            _outrightSeasonListResult.postValue(Event(result))
        }
    }

    private fun getPlayCategory(matchType: MatchType) {
        sportQueryData?.let { sportQueryData ->
            sportQueryData.items?.find { item ->
                item.code == getSportSelected(matchType)?.code
            }?.play?.filter { play ->
                play.num != 0
            }?.let { playList ->
                playList.forEach {
                    it.isSelected = (it == playList.firstOrNull())
                }

                _playList.value = playList
            }
        }
    }

    private fun getDateRow(matchType: MatchType) {
        val dateRow = when (matchType) {
            MatchType.TODAY -> {
                listOf(Date("", getTodayTimeRangeParams()))
            }
            MatchType.EARLY -> {
                getDateRowEarly()
            }
            MatchType.PARLAY -> {
                getDateRowParlay()
            }
            MatchType.AT_START -> {
                listOf(Date("", TimeUtil.getAtStartTimeRangeParams()))
            }
            else -> {
                listOf()
            }
        }

        dateRow.firstOrNull()?.let {
            dateRow.updateDateSelectedState(it)
        }
    }

    private fun getDateRowEarly(): List<Date> {
        val dateRow = mutableListOf(
            Date(
                androidContext.getString(R.string.date_row_all),
                TimeUtil.getEarlyAllTimeRangeParams()
            ), Date(
                androidContext.getString(R.string.date_row_other),
                TimeUtil.getOtherEarlyDateTimeRangeParams()
            )
        )

        dateRow.addAll(1, TimeUtil.getFutureDate(
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
            Date(it, TimeUtil.getDayDateTimeRangeParams(it), isDateFormat = true)
        })

        return dateRow
    }

    private fun getDateRowParlay(): List<Date> {
        val dateRow = mutableListOf(
            Date(
                androidContext.getString(R.string.date_row_all),
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
                androidContext.getString(R.string.date_row_other),
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

    private fun getCurrentTimeRangeParams(): TimeRangeParams? {
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

        _leagueSelectedList.postValue(mutableListOf())
    }

    fun updateOddForOddsDetail(matchOdd: MatchOddsChangeEvent) {
        val newList = arrayListOf<OddsDetailListData>()
        matchOdd.odds?.forEach { map ->
            val key = map.key
            val value = map.value
            val filteredOddList = mutableListOf<Odd?>()
            value.odds?.forEach { odd ->
                filteredOddList.add(odd)
            }
            newList.add(
                OddsDetailListData(
                    key,
                    TextUtil.split(value.typeCodes),
                    value.name,
                    filteredOddList
                )
            )
        }

        _oddsDetailList.value?.peekContent()?.forEach {
            updateItemForOddsDetail(it, newList)
        }

        val list = _oddsDetailList.value?.peekContent() ?: arrayListOf()
        _oddsDetailList.postValue(Event(list))

    }

    fun updateMatchBetList(
        matchType: MatchType,
        gameType: GameType,
        playCateName: String,
        playName: String,
        matchInfo: MatchInfo,
        odd: Odd
    ) {
        val betItem = betInfoRepository.betInfoList.value?.peekContent()
            ?.find { it.matchOdd.oddsId == odd.id }

        if (betItem == null) {
            matchInfo.let {
                betInfoRepository.addInBetInfo(
                    matchType = matchType,
                    gameType = gameType,
                    playCateName = playCateName,
                    playName = playName,
                    matchInfo = matchInfo,
                    odd = odd
                )
            }
        } else {
            odd.id?.let { removeBetInfoItem(it) }
        }
    }

    fun updateMatchBetListForOutRight(
        matchType: MatchType,
        gameType: GameType,
        matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd,
        odd: Odd
    ) {
        val outrightCateName = matchOdd.dynamicMarkets[odd.outrightCateKey].let {
            when (LanguageManager.getSelectLanguage(androidContext)) {
                LanguageManager.Language.ZH -> {
                    it?.zh
                }
                else -> {
                    it?.en
                }
            }
        }

        val betItem = betInfoRepository.betInfoList.value?.peekContent()
            ?.find { it.matchOdd.oddsId == odd.id }

        if (betItem == null) {
            betInfoRepository.addInBetInfo(
                matchType = matchType,
                gameType = gameType,
                playCateName = outrightCateName ?: "",
                playName = odd.spread ?: "",
                matchInfo = matchOdd.matchInfo,
                odd = odd
            )
        } else {
            odd.id?.let { removeBetInfoItem(it) }
        }
    }

    private fun updateItemForOddsDetail(
        oddsDetail: OddsDetailListData,
        updatedOddsDetail: ArrayList<OddsDetailListData>
    ) {
        val oldOddList = oddsDetail.oddArrayList
        var newOddList = listOf<Odd?>()

        for (item in updatedOddsDetail) {
            if (item.gameType == oddsDetail.gameType) {
                newOddList = item.oddArrayList
                break
            }
        }

        oldOddList.forEach { oldOddData ->
            newOddList.forEach { newOddData ->

                if (oldOddData != null && newOddData != null) {
                    if (oldOddData.id == newOddData.id) {
                        oldOddData.spread = newOddData.spread

                        //先判斷大小
                        oldOddData.oddState = getOddState(
                            getOdds(oldOddData, loginRepository.mOddsType.value ?: OddsType.EU),
                            newOddData
                        )

                        //再帶入新的賠率
                        oldOddData.odds = newOddData.odds
                        oldOddData.hkOdds = newOddData.hkOdds

                        oldOddData.status = newOddData.status
                        oldOddData.producerId = newOddData.producerId
                    }
                }
            }
        }
    }

    private suspend fun getOddsDetail(matchId: String) {
        val result = doNetwork(androidContext) {
            OneBoSportApi.oddsService.getOddsDetail(OddsDetailRequest(matchId))
        }
        _oddsDetailResult.postValue(Event(result))
        result?.success?.let { success ->
            val list: ArrayList<OddsDetailListData> = ArrayList()
            if (success) {
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
                    list.add(
                        OddsDetailListData(
                            key,
                            TextUtil.split(value.typeCodes),
                            value.name,
                            filteredOddList,
                        )
                    )
                }

                list.find { it.gameType == PlayCate.EPS.value }.apply {
                    if (this != null) {
                        list.add(0, list.removeAt(list.indexOf(this)))
                    }
                }

                _oddsDetailList.postValue(Event(list))
            }
        }
    }

    fun getOddsDetailByMatchId(matchId: String) {
        viewModelScope.launch {
            getOddsDetail(matchId)
        }
    }

    fun getPlayCateListAndOddsDetail(gameType: String, matchId: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.playCateListService.getPlayCateList(gameType)
            }
            getOddsDetail(matchId)
            _playCateListResult.postValue(Event(result))
        }
    }

    fun searchLeague(matchType: MatchType, searchText: String) {
        when (matchType) {
            MatchType.TODAY, MatchType.EARLY, MatchType.PARLAY -> {

                val searchResult = _leagueListResult.value?.peekContent()?.rows?.filter {

                    it.searchList = it.list.filter { league ->
                        league.name.trim().toLowerCase(Locale.ENGLISH)
                            .contains(searchText.trim().toLowerCase(Locale.ENGLISH))
                    }

                    it.list.any { league ->
                        league.name.trim().toLowerCase(Locale.ENGLISH)
                            .contains(searchText.trim().toLowerCase(Locale.ENGLISH))
                    }
                }
                _countryListSearchResult.postValue(searchResult ?: listOf())
            }

            MatchType.OUTRIGHT -> {

                val searchResult =
                    _outrightSeasonListResult.value?.peekContent()?.rows?.filter {

                        it.searchList = it.list.filter { season ->
                            season.name.trim().toLowerCase(Locale.ENGLISH)
                                .contains(searchText.trim().toLowerCase(Locale.ENGLISH))
                        }

                        it.list.any { season ->
                            season.name.trim().toLowerCase(Locale.ENGLISH)
                                .contains(searchText.trim().toLowerCase(Locale.ENGLISH))
                        }
                    }
                _outrightCountryListSearchResult.postValue(searchResult ?: listOf())
            }
            else -> {
            }
        }
    }

    fun searchMatch(searchText: String) {
        val searchResult = _oddsListResult.value?.peekContent()?.oddsListData?.leagueOdds?.filter {

            it.searchMatchOdds = it.matchOdds.filter { matchOdd ->
                (matchOdd.matchInfo?.homeName?.trim()?.toLowerCase(Locale.ENGLISH)?.contains(
                    searchText.trim().toLowerCase(Locale.ENGLISH)
                ) ?: false) ||

                        (matchOdd.matchInfo?.awayName?.trim()?.toLowerCase(Locale.ENGLISH)
                            ?.contains(searchText.trim().toLowerCase(Locale.ENGLISH)) ?: false)
            }

            it.matchOdds.any { matchOdd ->
                (matchOdd.matchInfo?.homeName?.trim()?.toLowerCase(Locale.ENGLISH)?.contains(
                    searchText.trim().toLowerCase(Locale.ENGLISH)
                ) ?: false) ||

                        (matchOdd.matchInfo?.awayName?.trim()?.toLowerCase(Locale.ENGLISH)
                            ?.contains(searchText.trim().toLowerCase(Locale.ENGLISH)) ?: false)

            }
        }

        _leagueListSearchResult.postValue(searchResult ?: listOf())
    }

    fun getSettlementNotification(event: OrderSettlementEvent?) {
        event?.sportBet?.let {
            when (it.status) {
                Status.WIN.code, Status.WIN_HALF.code, Status.CANCEL.code -> {
                    _settlementNotificationMsg.value = Event(it)
                }
            }
        }
    }

    private fun getMatchCount(matchType: MatchType, sportMenuResult: SportMenuResult? = null): Int {
        val sportMenuRes = sportMenuResult ?: _sportMenuResult.value

        return when (matchType) {
            MatchType.IN_PLAY -> {
                sportMenuRes?.sportMenuData?.menu?.inPlay?.items?.size ?: 0
            }
            MatchType.TODAY -> {
                sportMenuRes?.sportMenuData?.menu?.today?.items?.size ?: 0
            }
            MatchType.EARLY -> {
                sportMenuRes?.sportMenuData?.menu?.early?.items?.size ?: 0
            }
            MatchType.PARLAY -> {
                sportMenuRes?.sportMenuData?.menu?.parlay?.items?.size ?: 0
            }
            MatchType.OUTRIGHT -> {
                sportMenuRes?.sportMenuData?.menu?.outright?.items?.size ?: 0
            }
            MatchType.AT_START -> {
                sportMenuRes?.sportMenuData?.atStart?.items?.size ?: 0
            }
        }
    }

    private fun getSportCount(
        matchType: MatchType,
        gameType: GameType?,
        sportMenuResult: SportMenuResult? = null
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
        MatchType.PARLAY -> {
            sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.find { it.isSelected }
        }
        MatchType.OUTRIGHT -> {
            sportMenuResult.value?.sportMenuData?.menu?.outright?.items?.find { it.isSelected }
        }
        MatchType.AT_START -> {
            sportMenuResult.value?.sportMenuData?.atStart?.items?.find { it.isSelected }
        }
    }

    private fun getPlayCateSelected(): Play? = _playList.value?.find { it.isSelected }

    private fun getPlayCateCodeList(): List<String>? {
        _playCate.value?.let {
            return listOf(it)
        }
        return null
    }

    private fun SportMenuData.updateSportSelectState(
        matchType: MatchType?,
        gameTypeCode: String?
    ): SportMenuData {
        this.menu.inPlay.items.map { sport ->
            sport.isSelected = when {
                ((matchType == MatchType.IN_PLAY) && gameTypeCode != null) -> {
                    sport.code == gameTypeCode
                }
                else -> {
                    this.menu.inPlay.items.indexOf(sport) == 0
                }
            }
        }
        this.menu.today.items.map { sport ->
            sport.isSelected = when {
                ((matchType == MatchType.TODAY) && gameTypeCode != null) -> {
                    sport.code == gameTypeCode
                }
                else -> {
                    this.menu.today.items.indexOf(sport) == 0
                }
            }
        }
        this.menu.early.items.map { sport ->
            sport.isSelected = when {
                ((matchType == MatchType.EARLY) && gameTypeCode != null) -> {
                    sport.code == gameTypeCode
                }
                else -> {
                    this.menu.early.items.indexOf(sport) == 0
                }
            }
        }
        this.menu.parlay.items.map { sport ->
            sport.isSelected = when {
                ((matchType == MatchType.PARLAY) && gameTypeCode != null) -> {
                    sport.code == gameTypeCode
                }
                else -> {
                    this.menu.parlay.items.indexOf(sport) == 0
                }
            }
        }
        this.menu.outright.items.map { sport ->
            sport.isSelected = when {
                ((matchType == MatchType.OUTRIGHT) && gameTypeCode != null) -> {
                    sport.code == gameTypeCode
                }
                else -> {
                    this.menu.outright.items.indexOf(sport) == 0
                }
            }
        }
        this.atStart.items.map { sport ->
            sport.isSelected = when {
                ((matchType == MatchType.AT_START) && gameTypeCode != null) -> {
                    sport.code == gameTypeCode
                }
                else -> {
                    this.atStart.items.indexOf(sport) == 0
                }
            }
        }

        return this
    }

    private fun SportMenuResult.updateSportSelectState(
        matchType: MatchType?,
        gameTypeCode: String?
    ) {
        this.sportMenuData?.updateSportSelectState(matchType, gameTypeCode)
        _sportMenuResult.postValue(this)
    }

    private fun List<Date>.updateDateSelectedState(date: Date) {
        this.forEach {
            it.isSelected = (it == date)
        }

        _curDate.postValue(this)
        _curDatePosition.postValue(this.indexOf(date))
    }

    private fun updatePlaySelectedState(play: Play) {
        val playList = _playList.value

        playList?.forEach {
            it.isSelected = (it == play)
        }

        playList?.let {
            _playList.value = it
            _playCate.value = (
                    when (play.selectionType == SelectionType.SELECTABLE.code) {
                        true -> {
                            it.find { play ->
                                play.isSelected
                            }?.playCateList?.find { playCate ->
                                playCate.isSelected
                            }?.code ?: it.find { play ->
                                play.isSelected
                            }?.playCateList?.firstOrNull()?.code
                        }
                        false -> {
                            null
                        }
                    }
                    )
        }
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
        quickListData: QuickListData
    ): OddsListResult {
        this.oddsListData?.leagueOdds?.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                matchOdd.quickPlayCateList?.forEach { quickPlayCate ->

                    quickPlayCate.isSelected =
                        (quickPlayCate.isSelected && (matchOdd.matchInfo?.id == matchId))

                    quickPlayCate.quickOdds = PlayCateUtils.filterQuickOdds(
                        quickListData.quickOdds?.get(quickPlayCate.code),
                        quickPlayCate.gameType ?: ""
                    )
                }
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
}