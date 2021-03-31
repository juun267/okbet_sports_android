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
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.BetAddResult
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.league.LeagueListRequest
import org.cxct.sportlottery.network.league.LeagueListResult
import org.cxct.sportlottery.network.league.Row
import org.cxct.sportlottery.network.match.MatchPreloadRequest
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.odds.MatchInfo
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
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseNoticeViewModel
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.data.Date
import org.cxct.sportlottery.ui.game.home.gameTable.GameEntity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.odds.OddsDetailListData
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class GameViewModel(
    private val androidContext: Context,
    userInfoRepository: UserInfoRepository,
    private val sportMenuRepository: SportMenuRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    private val thirdGameRepository: ThirdGameRepository
) : BaseNoticeViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    val isLogin: LiveData<Boolean> by lazy {
        loginRepository.isLogin
    }

    val token = loginRepository.token
    val userId = loginRepository.userId
    var mathType: MatchType? = null

    val messageListResult: LiveData<MessageListResult>
        get() = _messageListResult

    val sportMenuResult: LiveData<SportMenuResult?>
        get() = _sportMenuResult

    private val _matchPreloadInPlay = MutableLiveData<Event<MatchPreloadResult>>()
    val matchPreloadInPlay: LiveData<Event<MatchPreloadResult>>
        get() = _matchPreloadInPlay

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

    val curOddsDetailParams: LiveData<List<String?>>
        get() = _curOddsDetailParams

    val matchTypeCardForParlay: LiveData<MatchType>
        get() = _matchTypeCardForParlay

    val isNoHistory: LiveData<Boolean>
        get() = _isNoHistory

    val openGameDetail: LiveData<Triple<String, String, String>>
        get() = _openGameDetail

    val openOutrightDetail: LiveData<Pair<String, String>>
        get() = _openOutrightDetail

    val userInfo: LiveData<UserInfo?> = userInfoRepository.userInfo.asLiveData()

    private val _messageListResult = MutableLiveData<MessageListResult>()
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
    private val _curOddsDetailParams = MutableLiveData<List<String?>>()
    private val _asStartCount = MutableLiveData<Int>()
    private val _matchTypeCardForParlay = MutableLiveData<MatchType>()
    private val _isNoHistory = MutableLiveData<Boolean>()

    private val _openGameDetail = MutableLiveData<Triple<String, String, String>>()
    private val _openOutrightDetail = MutableLiveData<Pair<String, String>>()

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

    private val _matchOddList =
        MutableLiveData<MutableList<org.cxct.sportlottery.network.bet.info.MatchOdd>>()
    val matchOddList: LiveData<MutableList<org.cxct.sportlottery.network.bet.info.MatchOdd>>
        get() = _matchOddList

    private val _newMatchOddList =
        MutableLiveData<MutableList<org.cxct.sportlottery.network.bet.info.MatchOdd>>()
    val newMatchOddList: LiveData<MutableList<org.cxct.sportlottery.network.bet.info.MatchOdd>>
        get() = _newMatchOddList

    private val _parlayList = MutableLiveData<MutableList<ParlayOdd>>()
    val parlayList: LiveData<MutableList<ParlayOdd>>
        get() = _parlayList

    private val _oddsDetailResult = MutableLiveData<OddsDetailResult?>()
    val oddsDetailResult: LiveData<OddsDetailResult?>
        get() = _oddsDetailResult

    private val _playCateListResult = MutableLiveData<PlayCateListResult?>()
    val playCateListResult: LiveData<PlayCateListResult?>
        get() = _playCateListResult

    private val _oddsDetailList = MutableLiveData<ArrayList<OddsDetailListData>>()
    val oddsDetailList: LiveData<ArrayList<OddsDetailListData>>
        get() = _oddsDetailList

    private val _betAddResult = MutableLiveData<Event<BetAddResult?>>()
    val betAddResult: LiveData<Event<BetAddResult?>>
        get() = _betAddResult

    private val _userMoney = MutableLiveData<Double?>()
    val userMoney: LiveData<Double?> //使用者餘額
        get() = _userMoney

    val gameCateDataList by lazy { thirdGameRepository.gameCateDataList }

    var gameCardList: MutableList<MatchOdd>? = null

    fun isParlayPage(boolean: Boolean) {
        betInfoRepository._isParlayPage.postValue(boolean)

        if (boolean) {
            //冠軍不加入串關, 離開串關後也不顯示, 直接將冠軍類注單移除
            cleanOutrightBetOrder()
        }
    }

    private fun cleanOutrightBetOrder() {
        val listWithOutOutright = mutableListOf<String>()
        betInfoRepository.betList.forEach {
            if (it.matchType == MatchType.OUTRIGHT) {
                listWithOutOutright.add(it.matchOdd.oddsId)
            }
        }
        listWithOutOutright.forEach {
            removeBetInfoItem(it)
        }
    }

    //獲取系統公告
    fun getAnnouncement() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                val typeList = arrayOf(1)
                OneBoSportApi.messageService.getPromoteNotice(typeList)
            }?.let { result -> _messageListResult.postValue(result) }
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
            _allFootballCount.postValue(getParlayCount(SportType.FOOTBALL, result))
            _allBasketballCount.postValue(getParlayCount(SportType.BASKETBALL, result))
            _allTennisCount.postValue(getParlayCount(SportType.TENNIS, result))
            _allBadmintonCount.postValue(getParlayCount(SportType.BADMINTON, result))
            _allVolleyballCount.postValue(getParlayCount(SportType.VOLLEYBALL, result))

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
                _sportMenuResult.postValue(it)
            }
        }
    }

    private fun getParlayCount(sportType: SportType, sportMenuResult: SportMenuResult?): Int =
        sportMenuResult?.sportMenuData?.menu?.parlay?.items?.find {
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
    }

    fun getMoney() {
        viewModelScope.launch {
            val userMoneyResult = doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }
            _userMoney.postValue(userMoneyResult?.money)
        }
    }

    fun getGameHallList(matchType: MatchType, sportCode: String?) {
        _sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.map {
            it.isSelected = it.code == sportCode
        }
        _matchTypeCardForParlay.postValue(matchType)
    }

    fun getGameHallList(matchType: MatchType, sportType: SportType) {
        getGameHallList(matchType, sportType.code)
    }

    fun getGameHallList(matchType: MatchType, item: Item) {
        updateSportSelectedState(matchType, item)
        getGameHallList(matchType, true)
    }

    fun getGameHallList(matchType: MatchType, date: Date) {
        updateDateSelectedState(date)
        getGameHallList(matchType, false)

        _curDatePosition.postValue(_curDate.value?.indexOf(date))
    }

    fun getGameHallList(matchType: MatchType, isReloadDate: Boolean) {
        mathType = matchType
        if (isReloadDate) {
            getDateRow(matchType)

            _curDatePosition.postValue(0)
        }

        when (matchType) {
            MatchType.IN_PLAY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.inPlay?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getOddsList(gameType, matchType.postValue)
                }

                _isNoHistory.postValue(gameType == null)
            }
            MatchType.TODAY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.today?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getLeagueList(gameType, matchType.postValue, getCurrentTimeRangeParams())
                }

                _isNoHistory.postValue(gameType == null)
            }
            MatchType.EARLY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.early?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getLeagueList(gameType, matchType.postValue, getCurrentTimeRangeParams())
                }

                _isNoHistory.postValue(gameType == null)
            }
            MatchType.PARLAY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getLeagueList(gameType, matchType.postValue, getCurrentTimeRangeParams())
                }

                _isNoHistory.postValue(gameType == null)
            }
            MatchType.OUTRIGHT -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.outright?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getOutrightSeasonList(it)
                }

                _isNoHistory.postValue(gameType == null)
            }
            MatchType.AT_START -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.atStart?.items?.find {
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
        when (matchType) {
            MatchType.TODAY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.today?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getOddsList(
                        gameType,
                        matchType.postValue,
                        getCurrentTimeRangeParams(),
                        leagueIdList
                    )

                    _openGameDetail.postValue(Triple(matchType.postValue, it, leagueId))
                }
            }

            MatchType.EARLY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.early?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getOddsList(
                        gameType,
                        matchType.postValue,
                        getCurrentTimeRangeParams(),
                        leagueIdList
                    )

                    _openGameDetail.postValue(Triple(matchType.postValue, it, leagueId))
                }
            }

            MatchType.PARLAY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getOddsList(
                        gameType,
                        matchType.postValue,
                        getCurrentTimeRangeParams(),
                        leagueIdList
                    )

                    _openGameDetail.postValue(Triple(matchType.postValue, it, leagueId))
                }
            }
            else -> {
            }
        }
    }

    fun getOutrightOddsList(leagueId: String) {
        val gameType = _sportMenuResult.value?.sportMenuData?.menu?.outright?.items?.find {
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

                        //add subtitle
                        if (!matchOdd.displayList.contains(mapSubTitleOdd.key)) {
                            val dynamicMarket = matchOdd.dynamicMarkets[mapSubTitleOdd.key]

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
                            odd?.let { it1 -> matchOdd.displayList.add(it1) }
                        }
                    }

                    matchOdd.startDate = TimeUtil.timeFormat(it.matchInfo.startTime, "MM/dd")
                    matchOdd.startTime = TimeUtil.timeFormat(it.matchInfo.startTime, "HH:mm")
                }

                _outrightOddsListResult.postValue(Event(result))
            }

            _openOutrightDetail.postValue(it to leagueId)
        }
    }

    fun updateOutrightOddsSelectedState(winner: org.cxct.sportlottery.network.odds.list.Odd) {
        val result = _outrightOddsListResult.value?.peekContent()

        val list =
            result?.outrightOddsListData?.leagueOdds?.get(0)?.matchOdds?.get(
                0
            )?.displayList

        val isBet =
            betInfoRepository.betInfoList.value?.any { it.matchOdd.oddsId == winner.id } ?: false

        val odd = list?.find {
            it is org.cxct.sportlottery.network.odds.list.Odd && it == winner
        } as org.cxct.sportlottery.network.odds.list.Odd

        if (!isBet) {
            odd.isSelected = true

            getBetInfoList(listOf(Odd(winner.id ?: "", winner.odds).apply {
                matchType = this@GameViewModel.mathType
            }))
        } else {
            odd.isSelected = false

            winner.id?.let { removeBetInfoItem(it) }
        }

        _outrightOddsListResult.postValue(Event(result))
    }

    //TODO Dean : 重構，整理、提取程式碼
    fun updateMatchBetList(
        matchOdd: MatchOdd,
        oddString: String,
        odd: org.cxct.sportlottery.network.odds.list.Odd
    ) {
        val isOutright = mathType == MatchType.OUTRIGHT
        val result =
            if (mathType == MatchType.IN_PLAY) _oddsListGameHallResult.value?.peekContent() else _oddsListResult.value?.peekContent()
        val match =
            result?.oddsListData?.leagueOdds?.find { leagueOdd ->
                leagueOdd.matchOdds.contains(
                    matchOdd
                )
            }?.matchOdds?.find {
                it.odds[oddString]?.contains(odd) ?: false
            }?.odds?.get(oddString)
                ?.find { it == odd }
        if (betInfoRepository._isParlayPage.value == true) {
            val isBetMatchId =
                betInfoRepository.betList.find { it.matchOdd.matchId == matchOdd.matchInfo?.id }
            val isBetOddId = betInfoRepository.betList.find { it.matchOdd.oddsId == odd.id }
            when {
                isBetMatchId == null -> {
                    match?.isSelected = true
                    getBetInfoList(listOf(Odd(odd.id ?: "", odd.odds ?: 0.0).apply {
                        matchType = this@GameViewModel.mathType
                    }))
                }
                isBetOddId != null -> {
                    match?.isSelected = false
                    odd.id?.let { removeBetInfoItem(it) }
                }
                else -> {
                    return
                }
            }
        } else {
            val betItem = betInfoRepository.betList.find { it.matchOdd.oddsId == odd.id }
            if (betItem == null) {
                match?.isSelected = true
                getBetInfoList(listOf(Odd(odd.id ?: "", odd.odds ?: 0.0).apply {
                    matchType = this@GameViewModel.mathType
                }))
            } else {
                match?.isSelected = false
                odd.id?.let { removeBetInfoItem(it) }
            }
        }

        if (mathType == MatchType.IN_PLAY) {
            _oddsListGameHallResult.value = Event(result)
        } else {
            _oddsListResult.value = Event(result)
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

        _sportMenuResult.postValue(result)
    }

    fun updateMatchOddExpandDetail(matchOdd: MatchOdd) {
        val result = _oddsListResult.value?.peekContent()

        result?.oddsListData?.leagueOdds?.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach {
                it.isExpand = it == matchOdd && !matchOdd.isExpand
            }
        }

        _oddsListResult.postValue(Event(result))
    }


    fun updateMatchOddExpandInPlay(matchOdd: MatchOdd) {
        val result = _oddsListGameHallResult.value?.peekContent()

        result?.oddsListData?.leagueOdds?.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach {
                it.isExpand = it == matchOdd && !matchOdd.isExpand
            }
        }

        _oddsListGameHallResult.postValue(Event(result))
    }

    private fun getOddsList(
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
                if (result?.oddsListData?.leagueOdds?.isNotEmpty() == true) {
                    result.oddsListData.leagueOdds[0].isExpand = true
                }
                _oddsListResult.postValue(Event(result))
            } else {
                _oddsListGameHallResult.postValue(Event(result))
            }
        }
    }

    private fun getLeagueList(
        gameType: String,
        matchType: String,
        timeRangeParams: TimeRangeParams?
    ) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.leagueService.getLeagueList(
                    LeagueListRequest(
                        gameType,
                        matchType,
                        startTime = timeRangeParams?.startTime,
                        endTime = timeRangeParams?.endTime
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
                TimeUtil.getFutureDate(7).forEach {
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
                        androidContext.getString(R.string.date_row_today),
                        TimeUtil.getParlayTodayTimeRangeParams()

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

    fun getOddsDetail(oddId: String?) {
        var item: Item? = null
        when (mathType) {
            MatchType.IN_PLAY -> {
                item = _sportMenuResult.value?.sportMenuData?.menu?.inPlay?.items?.find {
                    it.isSelected
                }
            }
            MatchType.TODAY -> {
                item = _sportMenuResult.value?.sportMenuData?.menu?.today?.items?.find {
                    it.isSelected
                }
            }
            MatchType.EARLY -> {
                item = _sportMenuResult.value?.sportMenuData?.menu?.early?.items?.find {
                    it.isSelected
                }
            }
            MatchType.PARLAY -> {
                item = _sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.find {
                    it.isSelected
                }
            }
            MatchType.OUTRIGHT -> {
                item = _sportMenuResult.value?.sportMenuData?.menu?.outright?.items?.find {
                    it.isSelected
                }
            }
        }
        getOddsDetail(item?.code, item?.name, oddId)
    }

    fun getOddsDetail(entity: GameEntity) {
        getOddsDetail(entity.code, entity.name, entity.match?.id)
    }

    fun getOddsDetail(gameType: String?, typeName: String?, matchId: String?) {
        _curOddsDetailParams.postValue(listOf(gameType, typeName, matchId))
    }

    fun setOddsDetailMoreList(list: List<*>) {
        _oddsDetailMoreList.postValue(list)
    }

    private val _updateOddList = MutableLiveData<MutableList<org.cxct.sportlottery.network.odds.list.Odd>>()
    val updateOddList: LiveData<MutableList<org.cxct.sportlottery.network.odds.list.Odd>>
        get() = _updateOddList


    fun updateOdd(it: OddsChangeEvent) {
        val newList: MutableList<org.cxct.sportlottery.network.odds.list.Odd> = mutableListOf()
        for ((_, value) in it.odds) {
            newList.addAll(value)
        }
        val status = newList.find { odd ->
            odd.status == BetStatus.LOCKED.code || odd.status == BetStatus.DEACTIVATED.code
        }
        if (status == null) {
            updateBetInfoListByOddChange(newList)
        } else {
            updateOddStatus(newList)
            val list: MutableList<org.cxct.sportlottery.network.odds.list.Odd> = mutableListOf()
            it.odds.forEach { map ->
                val value = map.value
                value.forEach { odd ->
                    val newOdd = org.cxct.sportlottery.network.odds.list.Odd(
                        odd.id,
                        odd.odds,
                        odd.producerId,
                        odd.spread,
                        odd.status,
                    )
                    newOdd.oddState = odd.oddState
                    list.add(newOdd)
                }
            }
            _updateOddList.postValue(list)
        }
    }

    private fun updateOddStatus(newList: List<org.cxct.sportlottery.network.odds.list.Odd>) {
        newList.forEach { newItem ->
            betInfoRepository.betList.forEach {
                try {
                    if (newItem.id == it.matchOdd.oddsId) {
                        newItem.odds?.let { newOdds -> it.matchOdd.odds = newOdds }
                        newItem.status.let { newStatus -> it.matchOdd.status = newStatus }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val sendRequest = betInfoRepository.betList.find {
            it.matchOdd.status == 1 || it.matchOdd.status == 2
        }
        if (sendRequest == null)
            getBetInfoListForParlay(true)
        else Timber.e("不執行 betInfo request")
    }


    private fun updateBetInfoListByOddChange(newList: List<org.cxct.sportlottery.network.odds.list.Odd>) {
        updateOddStatus(newList)
        val sendRequest = betInfoRepository.betList.find {
            it.matchOdd.status == BetStatus.LOCKED.code || it.matchOdd.status == BetStatus.DEACTIVATED.code
        }
        if (sendRequest == null)
            getBetInfoListForParlay(true)
        else Timber.e("不執行 betInfo request")
    }

    fun updateMatchOdd(it: MatchOddsChangeEvent) {
        val newList: MutableList<org.cxct.sportlottery.network.odds.detail.Odd> =
            mutableListOf()
        for ((key, value) in it.odds) {
            value.odds?.forEach { odd ->
                odd?.let { o ->
                    newList.add(o)
                }
            }
        }
        val status = newList.find { odd ->
            odd.status == BetStatus.LOCKED.code || odd.status == BetStatus.DEACTIVATED.code
        }
        if (status == null) {
            updateBetInfoListByMatchOddChange(newList)
        } else {
            updateMatchOddStatus(newList)
            val list: MutableList<org.cxct.sportlottery.network.odds.list.Odd> = mutableListOf()
            it.odds.forEach { map ->
                val value = map.value
                value.odds?.forEach { odd ->
                    val newOdd = odd?.status?.let { status ->
                        org.cxct.sportlottery.network.odds.list.Odd(
                            odd.id,
                            odd.odds,
                            odd.producerId,
                            odd.spread,
                            status
                        )
                    }
                    odd?.oddState?.let { oStatus ->
                        newOdd?.oddState = oStatus
                    }

                    newOdd?.let { l -> list.add(l) }
                }
            }
            _updateOddList.postValue(list)
        }
    }

    private fun updateMatchOddStatus(newList: List<org.cxct.sportlottery.network.odds.detail.Odd>) {
        newList.forEach { newItem ->
            betInfoRepository.betList.forEach {
                try {
                    if (newItem.id == it.matchOdd.oddsId) {
                        newItem.odds?.let { newOdds -> it.matchOdd.odds = newOdds }
                        newItem.status?.let { newStatus -> it.matchOdd.status = newStatus }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        val sendRequest = betInfoRepository.betList.find {
            it.matchOdd.status == 1 || it.matchOdd.status == 2
        }
        if (sendRequest == null)
            getBetInfoListForParlay(true)
        else Timber.e("不執行 betInfo request")

    }

    private fun updateBetInfoListByMatchOddChange(newList: List<org.cxct.sportlottery.network.odds.detail.Odd>) {
        updateMatchOddStatus(newList)
        val sendRequest = betInfoRepository.betList.find {
            it.matchOdd.status == BetStatus.LOCKED.code || it.matchOdd.status == BetStatus.DEACTIVATED.code
        }
        if (sendRequest == null)
            getBetInfoListForParlay(true)
        else Timber.e("不執行 betInfo request")
    }

    fun getBetInfoList(oddsList: List<Odd>) {

        if (betInfoRepository.betList.size >= 10) {
            return
        }

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                betInfoRepository.getBetInfo(oddsList)
            }
            result?.success?.let {
                if (it) {
                    betInfoRepository._betInfoList.postValue(betInfoRepository.betList)
                }
                _betInfoResult.postValue(Event(result))
            }
        }
    }

    fun getBetInfoListForParlay(isUpdate: Boolean) {
        val sendList: MutableList<Odd> = mutableListOf()
        betInfoRepository.betList.let { list ->

            //以matchId分組 key為matchOdd(object)
            val groupList = list.groupBy { data ->
                list.find { d -> data.matchOdd.matchId == d.matchOdd.matchId }
            }

            //各別取第一項做為串關項目送出
            groupList.keys.forEach {
                it?.matchOdd?.let { matchOdd ->
                    sendList.add(Odd(matchOdd.oddsId, matchOdd.odds))
                }
            }
        }

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                betInfoRepository.getBetInfoList(sendList)
            }
            result?.success?.let { success ->

                if (success) {

                    //回傳成功 兩個list不一定數量相等 各別載入列表
                    if (isUpdate) {
                        _newMatchOddList.postValue(betInfoRepository.matchOddList)
                    } else {
                        _matchOddList.postValue(betInfoRepository.matchOddList)
                    }

                    //將串起來的數量賠率移至第一項
                    val pOdd = betInfoRepository.parlayOddList.find {
                        betInfoRepository.matchOddList.size.toString() + "C1" == it.parlayType
                    }

                    betInfoRepository.parlayOddList.remove(pOdd)

                    pOdd?.let { po ->
                        betInfoRepository.parlayOddList.add(0, po)
                    }

                    _parlayList.postValue(betInfoRepository.parlayOddList)

                    //載入串關注單後比對一般注單
                    val newBetList: MutableList<BetInfoListData> = mutableListOf()
                    betInfoRepository.matchOddList.let { mList ->
                        for (i in mList.indices) {
                            betInfoRepository.betList.let { bList ->
                                val oid = mList[i].oddsId
                                val item = bList.find {
                                    it.matchOdd.oddsId == oid
                                }
                                item?.let {
                                    newBetList.add(it)
                                }
                            }
                        }
                        betInfoRepository.betList.clear()
                        betInfoRepository.betList.addAll(newBetList)
                    }
                    betInfoRepository._betInfoList.postValue(newBetList)
                }
            }
            _betInfoResult.postValue(Event(result))
        }
    }

    fun removeBetInfoItem(oddId: String?) {
        betInfoRepository.removeItem(oddId)
    }

    fun removeBetInfoItemAndRefresh(oddId: String) {
        removeBetInfoItem(oddId)
        if (betInfoRepository.betList.size != 0) {
            getBetInfoListForParlay(false)
        }
    }

    fun getOddsDetail(matchId: String, oddsType: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.oddsService.getOddsDetail(OddsDetailRequest(matchId, oddsType))
            }
            _oddsDetailResult.postValue(result)
            result?.success?.let {
                val list: ArrayList<OddsDetailListData> = ArrayList()
                if (it) {
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

                    _oddsDetailList.postValue(list)
                }
            }
        }
    }

    fun removeOddsDetailPageValue() {
        _playCateListResult.postValue(null)
        _oddsDetailResult.postValue(null)
        _oddsDetailList.postValue(ArrayList())
    }

    fun checkInBetInfo(matchId: String): Boolean {
        return betInfoRepository.betList.any {
            it.matchOdd.matchId == matchId
        }
    }

    fun getPlayCateList(gameType: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.playCateListService.getPlayCateList(gameType)
            }
            _playCateListResult.postValue(result)
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
            betInfoRepository.betList.clear()
            betInfoRepository._betInfoList.postValue(betInfoRepository.betList)
        }
    }

    fun sportMenuSelectFirstItem(matchType: MatchType) {

        val menuData = _sportMenuResult.value?.sportMenuData?.menu

        when (matchType) {
            MatchType.IN_PLAY -> {
                menuData?.inPlay?.items?.map { sport ->
                    sport.isSelected = (menuData.inPlay.items.indexOf(sport) == 0)
                }
            }
            MatchType.TODAY -> {
                menuData?.today?.items?.map { sport ->
                    sport.isSelected = (menuData.today.items.indexOf(sport) == 0)
                }
            }
            MatchType.EARLY -> {
                menuData?.early?.items?.map { sport ->
                    sport.isSelected = (menuData.early.items.indexOf(sport) == 0)
                }
            }
            MatchType.PARLAY -> {
                menuData?.parlay?.items?.map { sport ->
                    sport.isSelected = (menuData.parlay.items.indexOf(sport) == 0)
                }
            }
            MatchType.OUTRIGHT -> {
                menuData?.outright?.items?.map { sport ->
                    sport.isSelected = (menuData.outright.items.indexOf(sport) == 0)
                }
            }
            else -> {
            }
        }
    }

    fun getGameCard(): MutableList<MatchInfo?> {
        val matchOddList: MutableList<MatchInfo?> = mutableListOf()
        gameCardList?.forEach {
            matchOddList.add(it.matchInfo)
        }
        return matchOddList
    }

    fun setGoToThirdGamePage(catePage: ThirdGameCategory?) {
        thirdGameRepository.setGoToThirdGamePage(catePage)
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
}