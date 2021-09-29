package org.cxct.sportlottery.ui.game

import android.app.Application
import android.content.Context
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
import org.cxct.sportlottery.network.matchLiveInfo.MatchLiveUrlRequest
import org.cxct.sportlottery.network.matchLiveInfo.Response
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.detail.OddsDetailRequest
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.odds.eps.OddsEpsListRequest
import org.cxct.sportlottery.network.odds.eps.OddsEpsListResult
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.OddsListRequest
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.network.odds.quick.QuickListData
import org.cxct.sportlottery.network.odds.quick.QuickListRequest
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListRequest
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListResult
import org.cxct.sportlottery.network.outright.season.OutrightLeagueListRequest
import org.cxct.sportlottery.network.outright.season.OutrightLeagueListResult
import org.cxct.sportlottery.network.playcate.PlayCateListResult
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.network.sport.query.SportQueryData
import org.cxct.sportlottery.network.sport.query.SportQueryRequest
import org.cxct.sportlottery.network.today.MatchCategoryQueryRequest
import org.cxct.sportlottery.network.today.MatchCategoryQueryResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.ui.game.data.Date
import org.cxct.sportlottery.ui.game.data.SpecialEntrance
import org.cxct.sportlottery.ui.odds.OddsDetailListData
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.TimeUtil.DMY_FORMAT
import org.cxct.sportlottery.util.TimeUtil.HM_FORMAT
import org.cxct.sportlottery.util.TimeUtil.getTodayTimeRangeParams
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class GameViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    myFavoriteRepository: MyFavoriteRepository,
    private val sportMenuRepository: SportMenuRepository,
    private val thirdGameRepository: ThirdGameRepository,
) : BaseBottomNavViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    myFavoriteRepository
) {
    companion object {
        const val GameLiveSP = "GameLiveSharedPreferences"
    }

    private val gameLiveSharedPreferences by lazy {
        androidContext.getSharedPreferences(
            GameLiveSP,
            Context.MODE_PRIVATE
        )
    }

    val parlayList: LiveData<MutableList<ParlayOdd>>
        get() = betInfoRepository.parlayList

    val gameCateDataList by lazy { thirdGameRepository.gameCateDataList }

    val messageListResult: LiveData<MessageListResult?>
        get() = _messageListResult

    val curMatchType: LiveData<MatchType?>
        get() = _curMatchType

    val curChildMatchType: LiveData<MatchType?>
        get() = _curChildMatchType

    val sportMenuResult: LiveData<SportMenuResult?>
        get() = _sportMenuResult

    val oddsListGameHallResult: LiveData<Event<OddsListResult?>>
        get() = _oddsListGameHallResult

    val oddsListResult: LiveData<Event<OddsListResult?>>
        get() = _oddsListResult

    val leagueListResult: LiveData<Event<LeagueListResult?>>
        get() = _leagueListResult

    val outrightLeagueListResult: LiveData<Event<OutrightLeagueListResult?>>
        get() = _outrightLeagueListResult

    val outrightOddsListResult: LiveData<Event<OutrightOddsListResult?>>
        get() = _outrightOddsListResult

    val matchCategoryQueryResult: LiveData<Event<MatchCategoryQueryResult?>>
        get() = _matchCategoryQueryResult

    val epsListResult: LiveData<Event<OddsEpsListResult?>>
        get() = _epsListResult

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

    val leagueFilterList: LiveData<List<League>>
        get() = _leagueFilterList

    val playList: LiveData<List<Play>>
        get() = _playList

    val playCate: LiveData<String?>
        get() = _playCate

    val showBetUpperLimit = betInfoRepository.showBetUpperLimit

    private val _messageListResult = MutableLiveData<MessageListResult?>()
    private val _curMatchType = MutableLiveData<MatchType?>()
    private val _curChildMatchType = MutableLiveData<MatchType?>()
    private val _sportMenuResult = MutableLiveData<SportMenuResult?>()
    private val _oddsListGameHallResult = MutableLiveData<Event<OddsListResult?>>()
    private val _oddsListResult = MutableLiveData<Event<OddsListResult?>>()
    private val _leagueListResult = MutableLiveData<Event<LeagueListResult?>>()
    private val _outrightLeagueListResult = MutableLiveData<Event<OutrightLeagueListResult?>>()
    private val _outrightOddsListResult = MutableLiveData<Event<OutrightOddsListResult?>>()
    private val _epsListResult = MutableLiveData<Event<OddsEpsListResult?>>()
    private val _countryListSearchResult = MutableLiveData<List<Row>>()
    private val _leagueListSearchResult = MutableLiveData<List<LeagueOdd>>()
    private val _matchCategoryQueryResult = MutableLiveData<Event<MatchCategoryQueryResult?>>()
    private val _curDate = MutableLiveData<List<Date>>()
    private val _curDatePosition = MutableLiveData<Int>()
    private val _asStartCount = MutableLiveData<Int>()
    private val _isNoHistory = MutableLiveData<Boolean>()
    private val _errorPromptMessage = MutableLiveData<Event<String>>()
    private val _specialEntrance = MutableLiveData<SpecialEntrance?>()
    private val _outrightCountryListSearchResult =
        MutableLiveData<List<org.cxct.sportlottery.network.outright.season.Row>>()
    private val _leagueSelectedList = MutableLiveData<List<League>>()
    private val _leagueSubmitList = MutableLiveData<Event<List<League>>>()
    private val _leagueFilterList = MutableLiveData<List<League>>()
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

    private val _cardMatchTypeFT = MutableLiveData<MatchType?>()
    val cardMatchTypeFT: LiveData<MatchType?> //目前有資料的足球ＭatchType
        get() = _cardMatchTypeFT

    private val _allBasketballCount = MutableLiveData<Int>()
    val allBasketballCount: LiveData<Int> //全部籃球比賽的數量
        get() = _allBasketballCount

    private val _cardMatchTypeBK = MutableLiveData<MatchType?>()
    val cardMatchTypeBK: LiveData<MatchType?> //目前有資料的籃球ＭatchType
        get() = _cardMatchTypeBK

    private val _allTennisCount = MutableLiveData<Int>()
    val allTennisCount: LiveData<Int> //全部網球比賽的數量
        get() = _allTennisCount

    private val _cardMatchTypeTN = MutableLiveData<MatchType?>()
    val cardMatchTypeTN: LiveData<MatchType?> //目前有資料的網球ＭatchType
        get() = _cardMatchTypeTN

    private val _allBadmintonCount = MutableLiveData<Int>()
    val allBadmintonCount: LiveData<Int> //全部羽毛球比賽的數量
        get() = _allBadmintonCount

    private val _allVolleyballCount = MutableLiveData<Int>()
    val allVolleyballCount: LiveData<Int> //全部排球比賽的數量
        get() = _allVolleyballCount

    private val _cardMatchTypeVB = MutableLiveData<MatchType?>()
    val cardMatchTypeVB: LiveData<MatchType?> //目前有資料的排球ＭatchType
        get() = _cardMatchTypeVB

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

    //賽事直播網址
    private val _matchLiveInfo = MutableLiveData<Event<LiveStreamInfo>>()
    val matchLiveInfo: LiveData<Event<LiveStreamInfo>>
        get() = _matchLiveInfo

    //Loading
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private var _isLoading = MutableLiveData<Boolean>()

    private var sportQueryData: SportQueryData? = null

    private var lastSportTypeHashMap: HashMap<String, String?> = hashMapOf(
        MatchType.IN_PLAY.postValue to null,
        MatchType.AT_START.postValue to null,
        MatchType.TODAY.postValue to null,
        MatchType.EARLY.postValue to null,
        MatchType.OUTRIGHT.postValue to null,
        MatchType.PARLAY.postValue to null,
        MatchType.EPS.postValue to null
    )

    fun navSpecialEntrance(matchType: MatchType, gameType: GameType?) {
        _specialEntrance.postValue(getSpecEntranceFromHome(matchType, gameType))
        gameType?.let { recordSportType(matchType, it.key) }
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

    fun setSportClosePromptMessage(sport:String){
        _errorPromptMessage.postValue(Event(String.format(androidContext.getString(R.string.message_no_sport_game),sport)))
    }

    fun switchMatchType(matchType: MatchType) {
        _curChildMatchType.value = null
        _oddsListGameHallResult.value = Event(null)
        _oddsListResult.value = Event(null)

        getSportMenu(matchType)
        getAllPlayCategory(matchType)
        filterLeague(listOf())
    }

    fun switchChildMatchType(childMatchType: MatchType? = null) {
        _curChildMatchType.value = childMatchType
        _oddsListGameHallResult.value = Event(null)
        _oddsListResult.value = Event(null)

        curMatchType.value?.let {
            getGameHallList(matchType = it, isReloadDate = true, isReloadPlayCate = true)
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
            checkLastSportType(matchType, sportQueryData)
        }
    }

    private fun checkLastSportType(matchType: MatchType, sportQueryData: SportQueryData?) {
        var isContain = false

        sportQueryData?.items?.forEach { item ->
            if (item.code == lastSportTypeHashMap[matchType.postValue])
                isContain = true
        }
        if (!isContain)
            lastSportTypeHashMap[matchType.postValue] = sportQueryData?.items?.firstOrNull()?.code
    }

    private fun postHomeCardCount(sportMenuResult: SportMenuResult?) {
        _asStartCount.postValue(
            getMatchCount(
                MatchType.AT_START,
                sportMenuResult
            )
        )

        when {
            getSportCount(MatchType.TODAY, GameType.FT, sportMenuResult) != 0 -> {
                _cardMatchTypeFT.postValue(MatchType.TODAY)
            }
            getSportCount(MatchType.EARLY, GameType.FT, sportMenuResult) != 0 -> {
                _cardMatchTypeFT.postValue(MatchType.EARLY)
            }
            getSportCount(MatchType.PARLAY, GameType.FT, sportMenuResult) != 0 -> {
                _cardMatchTypeFT.postValue(MatchType.PARLAY)
            }
            else ->  _cardMatchTypeFT.postValue(null)
        }
        _allFootballCount.postValue(getSportCount(MatchType.TODAY, GameType.FT, sportMenuResult))

        when {
            getSportCount(MatchType.TODAY, GameType.BK, sportMenuResult) != 0 -> {
                _cardMatchTypeBK.postValue(MatchType.TODAY)
            }
            getSportCount(MatchType.EARLY, GameType.BK, sportMenuResult) != 0 -> {
                _cardMatchTypeBK.postValue(MatchType.EARLY)
            }
            getSportCount(MatchType.PARLAY, GameType.BK, sportMenuResult) != 0 -> {
                _cardMatchTypeBK.postValue(MatchType.PARLAY)
            }
            else ->  _cardMatchTypeBK.postValue(null)
        }
        _allBasketballCount.postValue(getSportCount(MatchType.TODAY, GameType.BK, sportMenuResult))


        when {
            getSportCount(MatchType.TODAY, GameType.TN, sportMenuResult) != 0 -> {
                _cardMatchTypeTN.postValue(MatchType.TODAY)
            }
            getSportCount(MatchType.EARLY, GameType.TN, sportMenuResult) != 0 -> {
                _cardMatchTypeTN.postValue(MatchType.EARLY)
            }
            getSportCount(MatchType.PARLAY, GameType.TN, sportMenuResult) != 0 -> {
                _cardMatchTypeTN.postValue(MatchType.PARLAY)
            }
            else ->  _cardMatchTypeTN.postValue(null)
        }
        _allTennisCount.postValue(getSportCount(MatchType.TODAY, GameType.TN, sportMenuResult))

        when {
            getSportCount(MatchType.TODAY, GameType.VB, sportMenuResult) != 0 -> {
                _cardMatchTypeVB.postValue(MatchType.TODAY)
            }
            getSportCount(MatchType.EARLY, GameType.VB, sportMenuResult) != 0 -> {
                _cardMatchTypeVB.postValue(MatchType.EARLY)
            }
            getSportCount(MatchType.PARLAY, GameType.VB, sportMenuResult) != 0 -> {
                _cardMatchTypeVB.postValue(MatchType.PARLAY)
            }
            else ->  _cardMatchTypeVB.postValue(null)
        }
        _allVolleyballCount.postValue(getSportCount(MatchType.TODAY, GameType.VB, sportMenuResult))
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
        this.menu.eps.items.sortedBy { sport ->
            sport.sortNum
        }

        return this
    }

    //遊戲大廳首頁: 滾球盤、即將開賽盤
    fun getMatchPreload() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.matchService.getMatchPreload(
                    MatchPreloadRequest(
                        MatchType.IN_PLAY.postValue,
                        MenuCode.HOME_INPLAY_MOBILE.code
                    )
                )
            }?.let { result ->
                //mapping 下注單裡面項目 & 賠率按鈕 選擇狀態
                result.matchPreloadData?.datas?.forEach { data ->
                    data.matchOdds.forEach { matchOdd ->
                        matchOdd.oddsMap.forEach { map ->
                            map.value?.forEach { odd ->
                                odd?.isSelected =
                                    betInfoRepository.betInfoList.value?.peekContent()?.any {
                                        it.matchOdd.oddsId == odd?.id
                                    }
                            }
                        }
                        matchOdd.updateOddStatus()
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
                        MenuCode.HOME_ATSTART_MOBILE.code,
                        startTime = startTime.toString(),
                        endTime = endTime.toString()
                    )
                )
            }?.let { result ->
                result.matchPreloadData?.datas?.forEach { data ->
                    data.matchOdds.forEach { matchOdd ->
                        //計算且賦值 即將開賽 的倒數時間
                        matchOdd.matchInfo?.apply {
                            remainTime = startTime?.let { TimeUtil.getRemainTime(it) }
                        }

                        //mapping 下注單裡面項目 & 賠率按鈕 選擇狀態
                        matchOdd.oddsMap.forEach { map ->
                            map.value?.forEach { odd ->
                                odd?.isSelected =
                                    betInfoRepository.betInfoList.value?.peekContent()?.any {
                                        it.matchOdd.oddsId == odd?.id
                                    }
                            }
                        }
                        matchOdd.updateOddStatus()
                    }
                }

                _matchPreloadAtStart.postValue(Event(result))

                notifyFavorite(FavoriteType.MATCH)
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
                        oddData.oddsMap.forEach { map ->
                            map.value?.forEach { odd ->
                                odd?.isSelected =
                                    betInfoRepository.betInfoList.value?.peekContent()?.any {
                                        it.matchOdd.oddsId == odd?.id
                                    }
                            }
                        }
                        oddData.playCateMappingList = playCateMappingList
                        oddData.updateOddStatus()
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
                    oddData.oddsMap.forEach { map ->
                        map.value?.forEach { odd ->
                            odd?.isSelected =
                                betInfoRepository.betInfoList.value?.peekContent()?.any {
                                    it.matchOdd.oddsId == odd?.id
                                }
                        }
                    }
                    oddData.updateOddStatus()
                }

                _highlightMatchResult.postValue(Event(result))

                notifyFavorite(FavoriteType.MATCH)
            }
        }
    }

    fun switchSportType(matchType: MatchType, item: Item) {
        _sportMenuResult.value?.updateSportSelectState(matchType, item.code)
        _curChildMatchType.value = null
        _oddsListGameHallResult.value = Event(null)
        _oddsListResult.value = Event(null)

        recordSportType(matchType, item.code)
        getGameHallList(matchType, true, isReloadPlayCate = true)
        getMatchCategoryQuery(matchType)
        filterLeague(listOf())
    }

    private fun recordSportType(matchType: MatchType, sportType: String) {
        lastSportTypeHashMap[matchType.postValue] = sportType
    }

    fun switchPlay(matchType: MatchType, play: Play) {
        updatePlaySelectedState(play)
        getGameHallList(matchType, false)
    }

    fun switchPlayCategory(matchType: MatchType,play: Play, playCateCode: String?) {
        _playList.value?.forEach {
            it.isSelected = (it == play)
        }
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
        isReloadPlayCate: Boolean = false,
        isLastSportType: Boolean = false
    ) {

        val nowMatchType = curChildMatchType.value ?: matchType

        if (isReloadPlayCate) {
            getPlayCategory(nowMatchType)
        }

        if (isReloadDate) {
            getDateRow(nowMatchType)
        }


        if (isLastSportType)
            _sportMenuResult.value?.updateSportSelectState(
                matchType,
                lastSportTypeHashMap[matchType.postValue]
            )

        val sportItem = getSportSelected(matchType)

        sportItem?.let { item ->
            when (nowMatchType) {
                MatchType.IN_PLAY -> {
                    getOddsList(item.code, nowMatchType.postValue)
                }
                MatchType.TODAY -> {
                    getLeagueList(
                        item.code,
                        nowMatchType.postValue,
                        getCurrentTimeRangeParams()
                    )
                }
                MatchType.EARLY -> {
                    getLeagueList(
                        item.code,
                        nowMatchType.postValue,
                        getCurrentTimeRangeParams()
                    )
                }
                MatchType.PARLAY -> {
                    getLeagueList(
                        item.code,
                        nowMatchType.postValue,
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
                        nowMatchType.postValue,
                        getCurrentTimeRangeParams()
                    )
                }
                MatchType.EPS -> {
                    val time = TimeUtil.timeFormat(TimeUtil.getNowTimeStamp(), TimeUtil.YMD_FORMAT)
                    getEpsList(item.code, startTime = time)
                }
                else -> {
                }
            }
        }

        _isNoHistory.postValue(sportItem == null)
    }

    fun switchPlay(
        matchType: MatchType,
        leagueIdList: List<String>,
        matchIdList: List<String>,
        play: Play
    ) {
        updatePlaySelectedState(play)

        getLeagueOddsList(matchType, leagueIdList, matchIdList)
    }

    fun switchPlayCategory(
        matchType: MatchType,
        leagueIdList: List<String>,
        matchIdList: List<String>,
        playCateCode: String?
    ) {
        _playCate.value = playCateCode

        getLeagueOddsList(matchType, leagueIdList, matchIdList)
    }

    fun getLeagueOddsList(
        matchType: MatchType,
        leagueIdList: List<String>,
        matchIdList: List<String>,
        isReloadPlayCate: Boolean = false
    ) {

        if (isReloadPlayCate) {
            getPlayCategory(matchType)
        }

        val nowMatchType = curChildMatchType.value ?: matchType

        getSportSelected(nowMatchType)?.let { item ->
            getOddsList(
                item.code,
                matchType.postValue,
                getCurrentTimeRangeParams(),
                leagueIdList,
                matchIdList
            )
        }
    }

    fun getMatchCategoryQuery(matchType: MatchType) {
        viewModelScope.launch {
            getSportSelected(matchType)?.code?.let { gameType ->

                val result = doNetwork(androidContext) {
                    OneBoSportApi.matchCategoryService.getMatchCategoryQuery(
                        MatchCategoryQueryRequest(gameType)
                    )
                }

                _matchCategoryQueryResult.value = Event(result)

            }
        }
    }

    fun getOutrightOddsList(gameType: GameType, leagueId: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.outrightService.getOutrightOddsList(
                    OutrightOddsListRequest(
                        gameType.key,
                        matchType = MatchType.OUTRIGHT.postValue,
                        leagueIdList = listOf(leagueId)
                    )
                )
            }

            result?.outrightOddsListData?.leagueOdds?.forEach { leagueOdd ->
                leagueOdd.matchOdds?.forEach { matchOdd ->
                    matchOdd?.oddsMap?.values?.forEach { oddList ->
                        oddList?.updateOddSelectState()
                    }

                    matchOdd?.sortOdds()
                }
            }

            val matchOdd =
                result?.outrightOddsListData?.leagueOdds?.firstOrNull()?.matchOdds?.firstOrNull()
            matchOdd?.let {
                matchOdd.startDate = TimeUtil.timeFormat(it.matchInfo?.endTime, DMY_FORMAT)
                matchOdd.startTime = TimeUtil.timeFormat(it.matchInfo?.endTime, HM_FORMAT)
                matchOdd.playCateMappingList = playCateMappingList
                matchOdd.updateOddStatus()
            }

            _outrightOddsListResult.postValue(Event(result))
        }
    }

    private fun getOddsList(
        gameType: String,
        matchType: String,
        timeRangeParams: TimeRangeParams? = null,
        leagueIdList: List<String>? = null,
        matchIdList: List<String>? = null,
    ) {
        when (matchType) {
            MatchType.IN_PLAY.postValue, MatchType.AT_START.postValue -> {
                _oddsListResult.value = Event(null)
            }
            MatchType.TODAY.postValue, MatchType.EARLY.postValue, MatchType.PARLAY.postValue -> {
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

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.oddsService.getOddsList(
                    OddsListRequest(
                        gameType,
                        matchTypeFilter(matchType),
                        leagueIdList = emptyFilter(leagueIdList),
                        matchIdList = emptyFilter(matchIdList),
                        startTime = timeFilter(timeRangeParams?.startTime),
                        endTime = timeFilter(timeRangeParams?.endTime),
                        playCateMenuCode = getPlayCateSelected()?.code ?: ""
                    )
                )
            }

            result?.oddsListData?.leagueOdds?.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.matchInfo?.let { matchInfo ->
                        matchInfo.startDateDisplay =
                            TimeUtil.timeFormat(matchInfo.startTime, "MM/dd")

                        matchOdd.matchInfo.startTimeDisplay =
                            TimeUtil.timeFormat(matchInfo.startTime, "HH:mm")

                        matchInfo.remainTime = TimeUtil.getRemainTime(matchInfo.startTime)
                    }

                    matchOdd.playCateMappingList = playCateMappingList
                    
                    matchOdd.oddsMap.forEach { map ->
                        map.value?.updateOddSelectState()
                    }

                    matchOdd.sortOdds()

                    if (!getPlayCateCodeList().isNullOrEmpty())
                        matchOdd.oddsMap.entries.retainAll { getPlayCateCodeList()?.contains(it.key) == true }

                    matchOdd.updateOddStatus()
                }
            }

            when (matchType) {
                MatchType.IN_PLAY.postValue, MatchType.AT_START.postValue -> {
                    if (_leagueFilterList.value?.isNotEmpty() == true) {
                        result?.oddsListData?.leagueOddsFilter =
                            result?.oddsListData?.leagueOdds?.filter {
                                leagueFilterList.value?.map { league -> league.id }
                                    ?.contains(it.league.id) ?: false
                            }
                    }

                    _oddsListGameHallResult.postValue(Event(result))
                }

                MatchType.TODAY.postValue, MatchType.EARLY.postValue, MatchType.PARLAY.postValue -> {
                    _oddsListResult.postValue(Event(result))
                }
            }

            notifyFavorite(FavoriteType.MATCH)
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

    fun getLeagueList(
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

            clearSelectedLeague()

            notifyFavorite(FavoriteType.LEAGUE)
        }
    }

    private fun getOutrightSeasonList(gameType: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.outrightService.getOutrightSeasonList(
                    OutrightLeagueListRequest(gameType)
                )
            }

            _outrightLeagueListResult.postValue(Event(result))

            notifyFavorite(FavoriteType.LEAGUE)
        }
    }

    private fun getEpsList(
        gameType: String,
        matchType: String = MatchType.EPS.postValue,
        startTime: String
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
                        matchOddsItem.playCateMappingList = playCateMappingList
                        matchOddsItem.updateOddStatus()
                    }
                }
            }

            _epsListResult.postValue(Event(result))
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
                _playCate.value = null
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
                androidContext.getString(R.string.other),
                TimeUtil.getOtherEarlyDateTimeRangeParams()
            )
        )

        dateRow.addAll(1, TimeUtil.getFutureDate(
            7,
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

        clearSelectedLeague()
    }

    fun filterLeague(leagueList: List<League>) {
        _leagueFilterList.value = leagueList

        clearSelectedLeague()
    }

    fun clearSelectedLeague() {
        _leagueSelectedList.postValue(mutableListOf())
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
                            value.nameMap
                        )
                    )
                }

                result.oddsDetailData?.matchOdd?.updateOddStatus()

                //因UI需求 特優賠率移到第一項
                list.find { it.gameType == PlayCate.EPS.value }.apply {
                    if (this != null) {
                        list.add(0, list.removeAt(list.indexOf(this)))
                    }
                }

                list.forEach {
                    it.originPosition = list.indexOf(it)
                }

                _oddsDetailList.postValue(Event(list))

                notifyFavorite(FavoriteType.PLAY_CATE)
            }
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
            MatchType.EPS -> {
                sportMenuRes?.sportMenuData?.menu?.eps?.items?.size ?: 0
            }
            else -> {
                0
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
        else -> null
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
        this.menu.eps.items.map { sport ->
            sport.isSelected = when {
                ((matchType == MatchType.EPS) && gameTypeCode != null) -> {
                    sport.code == gameTypeCode
                }
                else -> {
                    this.menu.eps.items.indexOf(sport) == 0
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

        _curDate.value = this
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
                            }?.code
                        }
                        false -> {
                            null
                        }
                    }
                    )
        }
    }

    /**
     * 根據賽事的oddsSort將盤口重新排序
     */
    private fun MatchOdd.sortOdds() {
        val sortOrder = this.oddsSort?.split(",")
        this.oddsMap = this.oddsMap.toSortedMap(compareBy<String> {
            val oddsIndex = sortOrder?.indexOf(it)
            oddsIndex
        }.thenBy { it })
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
                    val quickOddsApi = when (quickPlayCate.code) {
                        QuickPlayCate.QUICK_CORNERS.value, QuickPlayCate.QUICK_PENALTY.value, QuickPlayCate.QUICK_ADVANCE.value -> {
                            quickListData.quickOdds?.get(quickPlayCate.code)
                        }
                        else -> {
                            quickListData.quickOdds?.get(quickPlayCate.code)
                        }
                    }

                    quickPlayCate.isSelected =
                        (quickPlayCate.isSelected && (matchOdd.matchInfo?.id == matchId))

                    quickPlayCate.quickOdds.putAll(
                        quickOddsApi?.toMutableFormat() ?: mutableMapOf()
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

    /**
     * @param matchId 獲取直播的賽事id
     * @param getNewest 是否要獲取最新 true: api請求獲取最新的直播地址 false: 讀取暫存直播地址
     */
    fun getLiveInfo(matchId: String, getNewest: Boolean = false) {
        //同樣賽事已經請求過最新地址則不再請求
        val nowMatchLiveInfo = matchLiveInfo.value?.peekContent()
        if (nowMatchLiveInfo?.matchId == matchId && nowMatchLiveInfo.isNewest) return

        val tempLiveStreamUrl = gameLiveSharedPreferences.getString(matchId, null)

        //沒有暫存網址時請求最新網址
        if (getNewest || tempLiveStreamUrl.isNullOrBlank()) {
            viewModelScope.launch {
                val result = doNetwork(androidContext) {
                    OneBoSportApi.matchService.getMatchLiveUrl(MatchLiveUrlRequest(1, matchId))
                }

                result?.t?.let {
                    val matchLiveInfo = OneBoSportApi.matchService.getMatchLiveInfo(it)

                    if (matchLiveInfo.isSuccessful && matchLiveInfo.body()?.isSuccess == true) {
                        matchLiveInfo.body()?.response?.let { response ->
                            val streamUrl = getStreamUrl(response)?.let { streamRealUrl ->
                                val editor = gameLiveSharedPreferences.edit()
                                editor.putString(matchId, streamRealUrl).apply()
                                streamRealUrl
                            } ?: ""

                            _matchLiveInfo.postValue(
                                Event(
                                    LiveStreamInfo(
                                        matchId,
                                        streamUrl,
                                        true
                                    )
                                )
                            )
                        }
                    }
                }
            }
        } else {
            _matchLiveInfo.postValue(Event(LiveStreamInfo(matchId, tempLiveStreamUrl, false)))
        }
    }

    /**
     * resource type
     * p2: 需要将返回的accessToken作为请求头，请求streamURL
     * i: 直接请求streamURL, 它的请求url不包含协议部分，需要加上https
     * s: 以xml形式请求streamURL
     * 其他: 直接使用streamURL
     *
     * 20210831 s型態還未有相關賽事，等相关比赛出现后，再解析
     */
    private suspend fun getStreamUrl(response: Response): String? {
        return when (response.videoProvider) {
            VideoProvider.P2.code -> {
                val liveUrlResponse = OneBoSportApi.matchService.getLiveP2Url(
                    response.accessToken,
                    response.streamURL
                )
                liveUrlResponse.body()?.launchInfo?.streamLauncher?.find { it.launcherURL.isNotEmpty() }?.launcherURL
            }
            VideoProvider.I.code, VideoProvider.S.code -> {
                val liveUrlResponse =
                    OneBoSportApi.matchService.getLiveIUrl("https://${response.streamURL}")
                liveUrlResponse.body()?.hlsUrl
            }
            else -> {
                response.streamURL
            }
        }
    }
}