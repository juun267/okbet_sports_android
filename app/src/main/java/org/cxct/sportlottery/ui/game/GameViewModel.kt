package org.cxct.sportlottery.ui.game

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.BetAddResult
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.league.LeagueListRequest
import org.cxct.sportlottery.network.league.LeagueListResult
import org.cxct.sportlottery.network.league.Row
import org.cxct.sportlottery.network.match.MatchPreloadRequest
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.odds.detail.OddsDetailRequest
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.odds.list.*
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListRequest
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListResult
import org.cxct.sportlottery.network.outright.season.OutrightSeasonListRequest
import org.cxct.sportlottery.network.outright.season.OutrightSeasonListResult
import org.cxct.sportlottery.network.playcate.PlayCateListResult
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.service.order_settlement.OrderSettlementEvent
import org.cxct.sportlottery.network.service.order_settlement.SportBet
import org.cxct.sportlottery.network.service.order_settlement.Status
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseNoticeViewModel
import org.cxct.sportlottery.ui.game.data.Date
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.odds.OddsDetailListAdapter
import org.cxct.sportlottery.ui.odds.OddsDetailListData
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.TimeUtil.getTodayTimeRangeParams
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

const val BET_INFO_MAX_COUNT = 10

class GameViewModel(
    private val androidContext: Context,
    userInfoRepository: UserInfoRepository,
    private val sportMenuRepository: SportMenuRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    private val thirdGameRepository: ThirdGameRepository,
) : BaseNoticeViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    val isLogin: LiveData<Boolean> by lazy {
        loginRepository.isLogin
    }

    val token = loginRepository.token
    val userId = loginRepository.userId
    var matchType: MatchType? = null

    val messageListResult: LiveData<MessageListResult?>
        get() = _messageListResult

    val sportMenuResult: LiveData<SportMenuResult?>
        get() = _sportMenuResult

    private val _matchPreloadInPlay = MutableLiveData<Event<MatchPreloadResult>>()
    val matchPreloadInPlay: LiveData<Event<MatchPreloadResult>>
        get() = _matchPreloadInPlay

    private val _matchPreloadEarly = MutableLiveData<Event<MatchPreloadResult>>()
    val matchPreloadEarly: LiveData<Event<MatchPreloadResult>>
        get() = _matchPreloadEarly

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

    val curPlayType: LiveData<PlayType>
        get() = _curPlayType

    val curDate: LiveData<List<Date>>
        get() = _curDate

    val curDatePosition: LiveData<Int>
        get() = _curDatePosition

    val matchTypeCardForParlay: LiveData<Event<Pair<MatchType, SportType?>>?>
        get() = _matchTypeCardForParlay

    val isNoHistory: LiveData<Boolean>
        get() = _isNoHistory

    val settlementNotificationMsg: LiveData<Event<SportBet>>
        get() = _settlementNotificationMsg

    val errorPromptMessage: LiveData<Event<String>>
        get() = _errorPromptMessage

    val userInfo: LiveData<UserInfo?> = userInfoRepository.userInfo.asLiveData()

    val betInfoList = betInfoRepository.betInfoList

    private val _messageListResult = MutableLiveData<MessageListResult?>()
    private val _sportMenuResult = MutableLiveData<SportMenuResult?>()
    private val _oddsListGameHallResult = MutableLiveData<Event<OddsListResult?>>()
    private val _oddsListResult = MutableLiveData<Event<OddsListResult?>>()
    private val _leagueListResult = MutableLiveData<Event<LeagueListResult?>>()
    private val _outrightSeasonListResult = MutableLiveData<Event<OutrightSeasonListResult?>>()
    private val _outrightOddsListResult = MutableLiveData<Event<OutrightOddsListResult?>>()

    private val _countryListSearchResult = MutableLiveData<List<Row>>()
    private val _outrightCountryListSearchResult =
        MutableLiveData<List<org.cxct.sportlottery.network.outright.season.Row>>()
    private val _leagueListSearchResult = MutableLiveData<List<LeagueOdd>>()

    private val _curPlayType = MutableLiveData<PlayType>().apply {
        value = PlayType.OU_HDP
    }
    private val _curDate = MutableLiveData<List<Date>>()
    private val _curDatePosition = MutableLiveData<Int>()
    private val _asStartCount = MutableLiveData<Int>()
    private val _matchTypeCardForParlay = MutableLiveData<Event<Pair<MatchType, SportType?>>?>()
    private val _isNoHistory = MutableLiveData<Boolean>()
    private val _settlementNotificationMsg = MutableLiveData<Event<SportBet>>()
    private val _errorPromptMessage = MutableLiveData<Event<String>>()

    val asStartCount: LiveData<Int> //即將開賽的數量
        get() = _asStartCount

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

    private val _oddsDetailMoreList = MutableLiveData<List<*>>()
    val oddsDetailMoreList: LiveData<List<*>?>
        get() = _oddsDetailMoreList

    private val _betInfoResult = MutableLiveData<Event<BetInfoResult?>>()
    val betInfoResult: LiveData<Event<BetInfoResult?>>
        get() = _betInfoResult

    val matchOddList: LiveData<MutableList<org.cxct.sportlottery.network.bet.info.MatchOdd>>
        get() = betInfoRepository.matchOddList

    val parlayList: LiveData<MutableList<ParlayOdd>>
        get() = betInfoRepository.parlayList

    private val _oddsDetailResult = MutableLiveData<Event<OddsDetailResult?>>()
    val oddsDetailResult: LiveData<Event<OddsDetailResult?>>
        get() = _oddsDetailResult

    private val _playCateListResult = MutableLiveData<Event<PlayCateListResult?>>()
    val playCateListResult: LiveData<Event<PlayCateListResult?>>
        get() = _playCateListResult

    private val _oddsDetailList = MutableLiveData<Event<ArrayList<OddsDetailListData>>>()
    val oddsDetailList: LiveData<Event<ArrayList<OddsDetailListData>>>
        get() = _oddsDetailList

    private val _betAddResult = MutableLiveData<Event<BetAddResult?>>()
    val betAddResult: LiveData<Event<BetAddResult?>>
        get() = _betAddResult

    private val _userMoney = MutableLiveData<Double?>()
    val userMoney: LiveData<Double?> //使用者餘額
        get() = _userMoney

    private val _systemDelete = MutableLiveData<Boolean>()
    val systemDelete: LiveData<Boolean>
        get() = _systemDelete

    val gameCateDataList by lazy { thirdGameRepository.gameCateDataList }

    var menuEntrance = false

    fun isParlayPage(boolean: Boolean) {
        betInfoRepository._isParlayPage.postValue(boolean)
        if (boolean) {
            //冠軍不加入串關, 離開串關後也不顯示, 直接將冠軍類注單移除
            cleanOutrightBetOrder()
            getBetInfoListForParlay()
        }
    }

    private fun cleanOutrightBetOrder() {
        val listWithOutOutright = mutableListOf<String>()
        betInfoRepository.betInfoList.value?.forEach {
            if (it.matchType == MatchType.OUTRIGHT) {
                listWithOutOutright.add(it.matchOdd.oddsId)
            }
        }
        if (listWithOutOutright.size > 0) _systemDelete.postValue(true)
        listWithOutOutright.forEach {
            removeBetInfoItem(it)
        }
    }

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
        val now = TimeUtil.getNowTimeStamp()
        val todayStart = TimeUtil.getTodayStartTimeStamp()

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                sportMenuRepository.getSportMenu(
                    now.toString(),
                    todayStart.toString()
                )
            }

            val asStartCount = result?.sportMenuData?.atStart?.num ?: 0
            _asStartCount.postValue(asStartCount)
            _allFootballCount.postValue(getTodayCount(SportType.FOOTBALL, result))
            _allBasketballCount.postValue(getTodayCount(SportType.BASKETBALL, result))
            _allTennisCount.postValue(getTodayCount(SportType.TENNIS, result))
            _allBadmintonCount.postValue(getTodayCount(SportType.BADMINTON, result))
            _allVolleyballCount.postValue(getTodayCount(SportType.VOLLEYBALL, result))

            result?.let {
                if (it.sportMenuData != null)
                    initSportMenuSelectedState(it.sportMenuData)
                it.sportMenuData?.menu?.inPlay?.items?.sortedBy { item ->
                    item.sortNum
                }
                it.sportMenuData?.menu?.today?.items?.sortedBy { item ->
                    item.sortNum
                }
                it.sportMenuData?.menu?.early?.items?.sortedBy { item ->
                    item.sortNum
                }
                it.sportMenuData?.menu?.parlay?.items?.sortedBy { item ->
                    item.sortNum
                }
                it.sportMenuData?.menu?.outright?.items?.sortedBy { item ->
                    item.sortNum
                }
                _sportMenuResult.value = it
            }
        }
    }

    private fun getTodayCount(sportType: SportType, sportMenuResult: SportMenuResult?): Int =
        sportMenuResult?.sportMenuData?.menu?.today?.items?.find {
            it.code == sportType.code
        }?.num ?: 0

    private fun initSportMenuSelectedState(sportMenuData: SportMenuData) {
        sportMenuData.menu.inPlay.items.map { sport ->
            sport.isSelected = (sportMenuData.menu.inPlay.items.indexOf(sport) == 0)
        }
        sportMenuData.menu.today.items.map { sport ->
            sport.isSelected = (sportMenuData.menu.today.items.indexOf(sport) == 0)
        }
        sportMenuData.menu.early.items.map { sport ->
            sport.isSelected = (sportMenuData.menu.early.items.indexOf(sport) == 0)
        }
        sportMenuData.menu.parlay.items.map { sport ->
            sport.isSelected = (sportMenuData.menu.parlay.items.indexOf(sport) == 0)
        }
        sportMenuData.menu.outright.items.map { sport ->
            sport.isSelected = (sportMenuData.menu.outright.items.indexOf(sport) == 0)
        }
        sportMenuData.atStart.items.map { sport ->
            sport.isSelected = (sportMenuData.atStart.items.indexOf(sport) == 0)
        }
    }

    private fun updateSportMenuSelectedState(sportMenuData: SportMenuData) {
        val matchType = _matchTypeCardForParlay.value?.peekContent()?.first
        val sportType = _matchTypeCardForParlay.value?.peekContent()?.second

        sportType?.let {
            when (matchType) {
                MatchType.IN_PLAY -> {
                    sportMenuData.menu.inPlay.items.map { sport ->
                        sport.isSelected = (sport.code == sportType.code)
                    }
                }
                MatchType.TODAY -> {
                    sportMenuData.menu.today.items.map { sport ->
                        sport.isSelected = (sport.code == sportType.code)
                    }
                }
                MatchType.EARLY -> {
                    sportMenuData.menu.early.items.map { sport ->
                        sport.isSelected = (sport.code == sportType.code)
                    }
                }
                MatchType.PARLAY -> {
                    sportMenuData.menu.parlay.items.map { sport ->
                        sport.isSelected = (sport.code == sportType.code)
                    }
                }
                MatchType.OUTRIGHT -> {
                    sportMenuData.menu.outright.items.map { sport ->
                        sport.isSelected = (sport.code == sportType.code)
                    }
                }
                MatchType.AT_START -> {
                    sportMenuData.atStart.items.map { sport ->
                        sport.isSelected = (sport.code == sportType.code)
                    }
                }
                else -> {
                }
            }
        }

        _matchTypeCardForParlay.value = null
    }

    fun getInPlayMatchPreload() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.matchService.getMatchPreload(
                    MatchPreloadRequest(MatchType.IN_PLAY.postValue)
                )
            }?.let { result ->
                _matchPreloadInPlay.postValue(Event(result))
            }
        }
        viewModelScope.launch {
            doNetwork(androidContext) {
                val nowTimeStamp = getTodayTimeRangeParams()
                OneBoSportApi.matchService.getMatchPreload(
                    MatchPreloadRequest(
                        MatchType.TODAY.postValue,
                        startTime = nowTimeStamp.startTime,
                        endTime = nowTimeStamp.endTime
                    )
                )
            }?.let { result ->
                _matchPreloadEarly.postValue(Event(result))
            }
        }
    }

    fun getMoney() {
        if (isLogin.value == false) return
        viewModelScope.launch {
            val userMoneyResult = doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }
            _userMoney.postValue(userMoneyResult?.money)
        }
    }

    fun getMatchTypeList(matchType: MatchType, isReloadDate: Boolean, date: String? = null) {
        val now = TimeUtil.getNowTimeStamp()
        val todayStart = TimeUtil.getTodayStartTimeStamp()

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                sportMenuRepository.getSportMenu(
                    now.toString(),
                    todayStart.toString()
                )
            }

            val asStartCount = result?.sportMenuData?.atStart?.num ?: 0
            _asStartCount.postValue(asStartCount)
            _allFootballCount.postValue(getTodayCount(SportType.FOOTBALL, result))
            _allBasketballCount.postValue(getTodayCount(SportType.BASKETBALL, result))
            _allTennisCount.postValue(getTodayCount(SportType.TENNIS, result))
            _allBadmintonCount.postValue(getTodayCount(SportType.BADMINTON, result))
            _allVolleyballCount.postValue(getTodayCount(SportType.VOLLEYBALL, result))

            result?.let {
                if (it.sportMenuData != null) {
                    initSportMenuSelectedState(it.sportMenuData)
                    updateSportMenuSelectedState(it.sportMenuData)
                }
                it.sportMenuData?.menu?.inPlay?.items?.sortedBy { item ->
                    item.sortNum
                }
                it.sportMenuData?.menu?.today?.items?.sortedBy { item ->
                    item.sortNum
                }
                it.sportMenuData?.menu?.early?.items?.sortedBy { item ->
                    item.sortNum
                }
                it.sportMenuData?.menu?.parlay?.items?.sortedBy { item ->
                    item.sortNum
                }
                it.sportMenuData?.menu?.outright?.items?.sortedBy { item ->
                    item.sortNum
                }
                _sportMenuResult.value = it

                getGameHallList(matchType, isReloadDate, date)
            }
        }
    }

    fun getGameHallList(
        matchType: MatchType,
        sportType: SportType?,
        isLeftMenu: Boolean = false,
        isPreloadTable: Boolean = false
    ) {
        if (sportType == null) {
            if (!haveAtStart())
                return
        }
        menuEntrance = (this.matchType != matchType) || isPreloadTable//標記為卡片或菜單跳轉不同的類別

        if (isLeftMenu) {
            _sportMenuResult.value = _sportMenuResult.value
        }

        _matchTypeCardForParlay.postValue(Event(matchType to sportType))
    }

    //判斷即將開賽是否可以跳轉
    private fun haveAtStart(): Boolean {
        val haveGame = (asStartCount.value ?: 0) > 0
        if (!haveGame)
            _errorPromptMessage.value =
                Event(androidContext.getString(R.string.message_no_at_start))
        return haveGame
    }

    fun getGameHallList(matchType: MatchType, item: Item) {
        updateSportSelectedState(matchType, item)
        getGameHallList(matchType, true)
    }

    fun getGameHallList(matchType: MatchType, date: Date) {
        updateDateSelectedState(date)
        getGameHallList(matchType, false, date.date)

        _curDatePosition.postValue(_curDate.value?.indexOf(date))
    }

    fun getGameHallList(matchType: MatchType, isReloadDate: Boolean, date: String? = null) {
        this.matchType = matchType
        if (isReloadDate) {
            getDateRow(matchType)

            _curDatePosition.postValue(0)
        }

        when (matchType) {
            MatchType.IN_PLAY -> {
                val gameType =
                    _sportMenuResult.value?.sportMenuData?.menu?.inPlay?.items?.find {
                        it.isSelected
                    }?.code

                gameType?.let {
                    getOddsList(gameType, matchType.postValue)
                }

                _isNoHistory.postValue(gameType == null)
            }
            MatchType.TODAY -> {
                val gameType =
                    _sportMenuResult.value?.sportMenuData?.menu?.today?.items?.find {
                        it.isSelected
                    }?.code

                gameType?.let {
                    getLeagueList(gameType, matchType.postValue, getCurrentTimeRangeParams())
                }

                _isNoHistory.postValue(gameType == null)
            }
            MatchType.EARLY -> {
                val gameType =
                    _sportMenuResult.value?.sportMenuData?.menu?.early?.items?.find {
                        it.isSelected
                    }?.code

                gameType?.let {
                    getLeagueList(gameType, matchType.postValue, getCurrentTimeRangeParams())
                }

                _isNoHistory.postValue(gameType == null)
            }
            MatchType.PARLAY -> {
                val gameType =
                    _sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.find {
                        it.isSelected
                    }?.code
                gameType?.let {
                    getLeagueList(gameType, matchType.postValue, getCurrentTimeRangeParams(), date)
                }

                _isNoHistory.postValue(gameType == null)
            }
            MatchType.OUTRIGHT -> {
                val gameType =
                    _sportMenuResult.value?.sportMenuData?.menu?.outright?.items?.find {
                        it.isSelected
                    }?.code

                gameType?.let {
                    getOutrightSeasonList(it)
                }

                _isNoHistory.postValue(gameType == null)
            }
            MatchType.AT_START -> {
                val gameType =
                    _sportMenuResult.value?.sportMenuData?.atStart?.items?.find {
                        it.isSelected
                    }?.code

                gameType?.let {
                    getOddsList(gameType, matchType.postValue, getCurrentTimeRangeParams())
                }

                _isNoHistory.postValue(gameType == null)
            }
        }
    }

    fun getLeagueOddsList(matchType: MatchType, leagueId: String) {
        val leagueIdList by lazy {
            listOf(leagueId)
        }

        val gameType = when (matchType) {
            MatchType.TODAY -> _sportMenuResult.value?.sportMenuData?.menu?.today?.items?.find { it.isSelected }?.code
            MatchType.EARLY -> _sportMenuResult.value?.sportMenuData?.menu?.early?.items?.find { it.isSelected }?.code
            MatchType.PARLAY -> _sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.find { it.isSelected }?.code
            MatchType.IN_PLAY -> _sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.find { it.isSelected }?.code
            else -> null
        }

        gameType?.let {
            getOddsList(
                gameType,
                matchType.postValue,
                getCurrentTimeRangeParams(),
                leagueIdList
            )
        }
    }

    fun getOutrightOddsList(leagueId: String) {
        val gameType =
            _sportMenuResult.value?.sportMenuData?.menu?.outright?.items?.find {
                it.isSelected
            }?.code

        gameType?.let {
            viewModelScope.launch {
                val result = doNetwork(androidContext) {
                    OneBoSportApi.outrightService.getOutrightOddsList(
                        OutrightOddsListRequest(
                            gameType,
                            leagueIdList = listOf(leagueId)
                        )
                    )
                }

                result?.outrightOddsListData?.leagueOdds?.forEach { leagueOdd ->
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.odds.values.forEach { oddList ->
                            oddList.forEach { odd ->
                                odd?.isSelected =
                                    betInfoRepository.betInfoList.value?.any { betInfoListData ->
                                        betInfoListData.matchOdd.oddsId == odd?.id
                                    }
                            }
                        }
                    }
                }

                val matchOdd = result?.outrightOddsListData?.leagueOdds?.get(0)?.matchOdds?.get(0)
                matchOdd?.let {
                    it.odds.forEach { mapSubTitleOdd ->
                        val dynamicMarket = matchOdd.dynamicMarkets[mapSubTitleOdd.key]

                        //add subtitle
                        if (!matchOdd.displayList.contains(mapSubTitleOdd.key)) {
                            dynamicMarket?.let {
                                when (LanguageManager.getSelectLanguage(androidContext)) {
                                    LanguageManager.Language.ZH -> {
                                        matchOdd.displayList.add(dynamicMarket.zh)
                                    }
                                    else -> {
                                        matchOdd.displayList.add(dynamicMarket.en)
                                    }
                                }
                            }
                        }

                        //add odd
                        mapSubTitleOdd.value.forEach { odd ->
                            odd?.outrightCateName =
                                when (LanguageManager.getSelectLanguage(androidContext)) {
                                    LanguageManager.Language.ZH -> {
                                        dynamicMarket?.zh
                                    }
                                    else -> {
                                        dynamicMarket?.en
                                    }
                                }
                            odd?.let { it1 -> matchOdd.displayList.add(it1) }
                        }
                    }

                    matchOdd.startDate = TimeUtil.timeFormat(it.matchInfo.startTime, "MM/dd")
                    matchOdd.startTime = TimeUtil.timeFormat(it.matchInfo.startTime, "HH:mm")
                }

                _outrightOddsListResult.postValue(Event(result))
            }
        }
    }

    private fun updateSportSelectedState(matchType: MatchType, item: Item) {
        val result = _sportMenuResult.value

        when (matchType) {
            MatchType.IN_PLAY -> {
                result?.sportMenuData?.menu?.inPlay?.items?.map {
                    it.isSelected = (it == item)
                }
            }
            MatchType.TODAY -> {
                result?.sportMenuData?.menu?.today?.items?.map {
                    it.isSelected = (it == item)
                }
            }
            MatchType.EARLY -> {
                result?.sportMenuData?.menu?.early?.items?.map {
                    it.isSelected = (it == item)
                }
            }
            MatchType.PARLAY -> {
                result?.sportMenuData?.menu?.parlay?.items?.map {
                    it.isSelected = (it == item)
                }
            }
            MatchType.OUTRIGHT -> {
                result?.sportMenuData?.menu?.outright?.items?.map {
                    it.isSelected = (it == item)
                }
            }
            MatchType.AT_START -> {
                result?.sportMenuData?.atStart?.items?.map {
                    it.isSelected = (it == item)
                }
            }
        }

        _sportMenuResult.value = result
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
                        endTime = timeRangeParams?.endTime
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

                    matchOdd.odds.forEach { map ->
                        map.value.forEach { odd ->
                            odd?.isSelected = betInfoRepository.betInfoList.value?.any {
                                it.matchOdd.oddsId == odd?.id
                            }
                        }
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
            _leagueListResult.postValue(Event(result))
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

    private fun getDateRow(matchType: MatchType) {
        val dateRow = mutableListOf<Date>()

        when (matchType) {
            MatchType.TODAY -> {
                dateRow.add(Date("", TimeUtil.getTodayTimeRangeParams()))
            }
            MatchType.EARLY -> {
                dateRow.add(
                    Date(
                        androidContext.getString(R.string.date_row_all),
                        TimeUtil.getParlayAllTimeRangeParams()
                    )
                )
                TimeUtil.getFutureDate(6).forEach {
                    dateRow.add(Date(it, TimeUtil.getDayDateTimeRangeParams(it)))
                }
                dateRow.add(
                    Date(
                        androidContext.getString(R.string.date_row_other),
                        TimeUtil.getOtherEarlyDateTimeRangeParams()
                    )
                )
            }
            MatchType.PARLAY -> {
                dateRow.add(
                    Date(
                        androidContext.getString(R.string.date_row_all),
                        TimeUtil.getParlayAllTimeRangeParams()
                    )
                )
                dateRow.add(
                    Date(
                        androidContext.getString(R.string.date_row_live),
                        object : TimeRangeParams {
                            override val startTime: String?
                                get() = null
                            override val endTime: String?
                                get() = null
                        },
                        MatchType.IN_PLAY.postValue
                    )
                )
                dateRow.add(
                    Date(
                        androidContext.getString(R.string.date_row_today),
                        TimeUtil.getParlayTodayTimeRangeParams(),
                        MatchType.TODAY.postValue
                    )
                )
                TimeUtil.getFutureDate(6).forEach {
                    dateRow.add(
                        Date(
                            it,
                            TimeUtil.getDayDateTimeRangeParams(it),
                            MatchType.EARLY.postValue
                        )
                    )
                }

                dateRow.add(
                    Date(
                        androidContext.getString(R.string.date_row_other),
                        TimeUtil.getOtherEarlyDateTimeRangeParams(),
                        MatchType.EARLY.postValue
                    )
                )
            }
            MatchType.AT_START -> {
                dateRow.add(Date("", TimeUtil.getAtStartTimeRangeParams()))
            }
            else -> {
            }
        }

        dateRow.map {
            it.isSelected = (dateRow.indexOf(it) == 0)
        }

        _curDate.value = dateRow
    }

    private fun updateDateSelectedState(date: Date) {
        val dateRow = _curDate.value

        dateRow?.forEach {
            it.isSelected = (it == date)
        }

        dateRow?.let {
            _curDate.postValue(it)
        }
    }

    private fun getCurrentTimeRangeParams(): TimeRangeParams? {
        return _curDate.value?.find {
            it.isSelected
        }?.timeRangeParams
    }

    fun setPlayType(playType: PlayType) {
        _curPlayType.postValue(playType)
    }

    fun setOddsDetailMoreList(list: List<*>) {
        _oddsDetailMoreList.postValue(list)
    }

    fun updateOddForOddsDetail(matchOdd: MatchOddsChangeEvent){
        val newList = arrayListOf<OddsDetailListData>()
        matchOdd.odds?.forEach { map ->
            val key = map.key
            val value = map.value
            val filteredOddList = mutableListOf<org.cxct.sportlottery.network.odds.detail.Odd>()
            value.odds?.forEach { odd ->
                if (odd != null)
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

        val list = _oddsDetailList.value?.peekContent()?: arrayListOf()
        _oddsDetailList.postValue(Event(list))

    }

    fun updateMatchOddForParlay(matchOdd: MatchOddsChangeEvent) {
        val newList: MutableList<org.cxct.sportlottery.network.odds.detail.Odd> =
            mutableListOf()
        for ((_, value) in matchOdd.odds ?: mapOf()) {
            value.odds?.forEach { odd ->
                odd?.let { o ->
                    newList.add(o)
                }
            }
        }
        updateBetInfoListByMatchOddChange(newList)
    }

    fun updateMatchOdd(changeEvent: Any) {
        val newList: MutableList<org.cxct.sportlottery.network.odds.detail.Odd> = mutableListOf()
        when (changeEvent) {
            is OddsChangeEvent -> {
                changeEvent.odds?.forEach { map ->
                    val value = map.value
                    value.forEach { odd ->
                        odd?.let {
                            val newOdd = org.cxct.sportlottery.network.odds.detail.Odd(
                                null,
                                odd.id,
                                null,
                                odd.odds,
                                odd.hkOdds,
                                odd.producerId,
                                odd.spread,
                                odd.status,
                            )
                            newList.add(newOdd)
                        }
                    }
                }
            }

            is MatchOddsChangeEvent -> {
                for ((_, value) in changeEvent.odds ?: mapOf()) {
                    value.odds?.forEach { odd ->
                        odd?.let { o ->
                            newList.add(o)
                        }
                    }
                }
            }
        }

        betInfoRepository.betInfoList.value?.forEach {
            updateItem(it.matchOdd, newList)
        }
        betInfoRepository.notifyBetInfoChanged()
    }

    private fun updateBetInfoListByMatchOddChange(newListFromSocket: List<org.cxct.sportlottery.network.odds.detail.Odd>) {
        //判斷當前有沒有關閉的賠率
        val sendRequest = betInfoRepository.matchOddList.value?.find {
            it.status == BetStatus.LOCKED.code || it.status == BetStatus.DEACTIVATED.code
        }
        if (sendRequest == null) {
            betInfoRepository.matchOddList.value?.forEach {
                updateItem(it, newListFromSocket)
            }

            getBetInfoListForParlay()
        } else {
            Timber.e("不執行 betInfo 計算")
        }
    }

    fun saveOddsHasChanged(matchOdd: org.cxct.sportlottery.network.bet.info.MatchOdd) {
        betInfoRepository.saveOddsHasChanged(matchOdd)
    }

    fun updateMatchBetList(
        matchType: MatchType,
        sportType: SportType,
        playCateName: String,
        playName: String,
        matchOdd: MatchOdd,
        odd: org.cxct.sportlottery.network.odds.list.Odd
    ) {
        val betItem = betInfoRepository.betInfoList.value?.find { it.matchOdd.oddsId == odd.id }

        if (betItem == null) {
            betInfoRepository.addInBetInfo(
                matchType,
                sportType,
                playCateName,
                playName,
                matchOdd,
                odd
            )
        } else {
            odd.id?.let { removeBetInfoItem(it) }
        }
    }

    fun updateMatchBetList(
        matchType: MatchType,
        sportType: SportType,
        matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd,
        odd: org.cxct.sportlottery.network.odds.list.Odd
    ) {
        val betItem = betInfoRepository.betInfoList.value?.find { it.matchOdd.oddsId == odd.id }

        if (betItem == null) {
            betInfoRepository.addInBetInfo(
                matchType,
                sportType,
                odd.outrightCateName,
                odd.spread,
                matchOdd,
                odd
            )
        } else {
            odd.id?.let { removeBetInfoItem(it) }
        }
    }

    fun updateMatchBetList(
        matchType: MatchType,
        sportType: SportType,
        playCateName: String,
        matchOdd: org.cxct.sportlottery.network.odds.detail.MatchOdd,
        odd: org.cxct.sportlottery.network.odds.detail.Odd
    ) {
        val betItem = betInfoRepository.betInfoList.value?.find { it.matchOdd.oddsId == odd.id }

        if (betItem == null) {
            betInfoRepository.addInBetInfo(
                matchType, sportType, playCateName, matchOdd, odd
            )
        } else {
            odd.id?.let { removeBetInfoItem(it) }
        }
    }

    private fun updateItem(
        oldItem: org.cxct.sportlottery.network.bet.info.MatchOdd,
        newList: List<org.cxct.sportlottery.network.odds.detail.Odd>
    ) {
        for (newItem in newList) {
            try {
                newItem.let {
                    if (it.id == oldItem.oddsId) {
                        oldItem.oddState = getOddState(
                            getOdds(
                                oldItem,
                                loginRepository.mOddsType.value ?: OddsType.EU
                            ), newItem
                        )
                        newItem.odds.let { odds -> oldItem.odds = odds ?: 0.0 }
                        newItem.hkOdds.let { hkOdds -> oldItem.hkOdds = hkOdds ?: 0.0 }
                        newItem.status.let { status -> oldItem.status = status }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateItemForOddsDetail(oddsDetail: OddsDetailListData, updatedOddsDetail: ArrayList<OddsDetailListData>){
        val oldOddList = oddsDetail.oddArrayList
        var newOddList = listOf<org.cxct.sportlottery.network.odds.detail.Odd>()

        for (item in updatedOddsDetail) {
            if (item.gameType == oddsDetail.gameType) {
                newOddList = item.oddArrayList
                break
            }
        }

        oldOddList.forEach { oldOddData ->
            newOddList.forEach { newOddData ->
                if (oldOddData.id == newOddData.id) {

                    //如果是球員 忽略名字替換
                    if (!TextUtil.compareWithGameKey(oddsDetail.gameType, OddsDetailListAdapter.GameType.SCO.value)) {
                        if (newOddData.name?.isNotEmpty() == true) {
                            oldOddData.name = newOddData.name
                        }
                    }

                    if (newOddData.extInfo?.isNotEmpty() == true) {
                        oldOddData.extInfo = newOddData.extInfo
                    }

                    oldOddData.spread = newOddData.spread

                    //先判斷大小
                    oldOddData.oddState = getOddState(
                        getOdds(oldOddData, loginRepository.mOddsType.value ?: OddsType.EU), newOddData)

                    //再帶入新的賠率
                    oldOddData.odds = newOddData.odds
                    oldOddData.hkOdds = newOddData.hkOdds

                    oldOddData.status = newOddData.status
                    oldOddData.producerId = newOddData.producerId
                }
            }
        }
    }

    private fun getOddState(
        oldItemOdds: Double,
        newOdd: org.cxct.sportlottery.network.odds.detail.Odd
    ): Int {
        val odds = when (loginRepository.mOddsType.value) {
            OddsType.EU -> newOdd.odds
            OddsType.HK -> newOdd.hkOdds
            else -> null
        }
        val newOdds = odds ?: 0.0
        return when {
            newOdds == oldItemOdds -> OddState.SAME.state
            newOdds > oldItemOdds -> OddState.LARGER.state
            newOdds < oldItemOdds -> OddState.SMALLER.state
            else -> OddState.SAME.state
        }
    }

    fun getBetInfoListForParlay() {
        betInfoRepository.addInBetInfoParlay()
    }

    fun removeBetInfoItem(oddId: String?) {
        betInfoRepository.removeItem(oddId)
    }

    fun removeBetInfoItemAndRefresh(oddId: String) {
        removeBetInfoItem(oddId)
        if (betInfoRepository.betInfoList.value?.size != 0) {
            getBetInfoListForParlay()
        }
    }

    fun removeBetInfoAll() {
        betInfoRepository.clear()
    }

    private suspend fun getOddsDetail(matchId: String){
        val result = doNetwork(androidContext) {
            OneBoSportApi.oddsService.getOddsDetail(OddsDetailRequest(matchId))
        }
        _oddsDetailResult.postValue(Event(result))
        result?.success?.let { success ->
            val list: ArrayList<OddsDetailListData> = ArrayList()
            if (success) {
                result.oddsDetailData?.matchOdd?.odds?.forEach { (key, value) ->
                    var odd: org.cxct.sportlottery.network.odds.detail.Odd?
                    betInfoRepository.betInfoList.value?.let { list ->
                        for (i in list.indices) {

                            //server目前可能會回傳null
                            try {
                                odd = value.odds.find { v ->
                                    v?.id?.let { id ->
                                        id == betInfoRepository.betInfoList.value?.get(
                                            i
                                        )?.matchOdd?.oddsId
                                    } ?: return@find false
                                }
                                odd?.isSelect = true
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    }
                    val filteredOddList =
                        mutableListOf<org.cxct.sportlottery.network.odds.detail.Odd>()
                    value.odds.forEach { detailOdd ->
                        if (detailOdd != null)
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

    fun addBet(betAddRequest: BetAddRequest, matchType: MatchType?) {
        viewModelScope.launch {
            val result = getBetApi(matchType, betAddRequest)
            Event(result).let {
                _betAddResult.postValue(it)
            }

            Event(result).getContentIfNotHandled()?.success?.let {
                if (it) {
                    afterBet(matchType, result)
                }
            }
        }
    }

    private suspend fun getBetApi(
        matchType: MatchType?,
        betAddRequest: BetAddRequest
    ): BetAddResult? {
        //冠軍的投注要使用不同的api
        return if (matchType == MatchType.OUTRIGHT) {
            doNetwork(androidContext) {
                OneBoSportApi.outrightService.addOutrightBet(betAddRequest)
            }
        } else {
            doNetwork(androidContext) {
                OneBoSportApi.betService.addBet(betAddRequest)
            }
        }
    }

    private fun afterBet(matchType: MatchType?, result: BetAddResult?) {
        if (matchType != MatchType.PARLAY) {
            result?.rows?.let { rowList ->
                removeBetInfoItem(rowList[0].matchOdds[0].oddsId)
            }
        } else {
            betInfoRepository.clear()
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
}