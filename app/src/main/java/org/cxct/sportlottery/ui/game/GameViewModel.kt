package org.cxct.sportlottery.ui.game

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.bekawestberg.loopinglayout.library.addViewsAtAnchorEdge
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.common.GameType.Companion.getGameTypeMenuIcon
import org.cxct.sportlottery.network.common.GameType.Companion.getSpecificLanguageString
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
import org.cxct.sportlottery.network.matchTracker.MatchTrackerUrl
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.detail.OddsDetailRequest
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.odds.eps.OddsEpsListRequest
import org.cxct.sportlottery.network.odds.eps.OddsEpsListResult
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.OddsListIncrementResult
import org.cxct.sportlottery.network.odds.list.OddsListRequest
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.network.odds.quick.QuickListData
import org.cxct.sportlottery.network.odds.quick.QuickListRequest
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListRequest
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListResult
import org.cxct.sportlottery.network.outright.season.OutrightLeagueListRequest
import org.cxct.sportlottery.network.outright.season.OutrightLeagueListResult
import org.cxct.sportlottery.network.sport.*
import org.cxct.sportlottery.network.sport.coupon.SportCouponMenuResult
import org.cxct.sportlottery.network.sport.publicityRecommend.PublicityRecommendRequest
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.network.sport.publicityRecommend.RecommendResult
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.network.sport.query.SearchRequest
import org.cxct.sportlottery.network.sport.query.SportQueryData
import org.cxct.sportlottery.network.sport.query.SportQueryRequest
import org.cxct.sportlottery.network.today.MatchCategoryQueryRequest
import org.cxct.sportlottery.network.today.MatchCategoryQueryResult
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.ui.game.data.Date
import org.cxct.sportlottery.ui.game.data.SpecialEntrance
import org.cxct.sportlottery.ui.odds.OddsDetailListData
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.MatchOddUtil.applyDiscount
import org.cxct.sportlottery.util.MatchOddUtil.applyHKDiscount
import org.cxct.sportlottery.util.TimeUtil.DMY_FORMAT
import org.cxct.sportlottery.util.TimeUtil.HM_FORMAT
import org.cxct.sportlottery.util.TimeUtil.getTodayTimeRangeParams
import timber.log.Timber
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GameViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    myFavoriteRepository: MyFavoriteRepository,
    private val sportMenuRepository: SportMenuRepository,
    private val thirdGameRepository: ThirdGameRepository,
    private val withdrawRepository: WithdrawRepository
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
        const val GameFastBetOpenedSP = "GameFastBetOpenedSharedPreferences"
    }

    val token = loginRepository.token

    private val gameLiveSharedPreferences by lazy {
        androidContext.getSharedPreferences(
            GameLiveSP,
            Context.MODE_PRIVATE
        )
    }

    val parlayList: LiveData<MutableList<ParlayOdd>>
        get() = betInfoRepository.parlayList

    val gameCateDataList by lazy { thirdGameRepository.gameCateDataList }

    val messageListResult: LiveData<Event<MessageListResult?>>
        get() = _messageListResult

    val curMatchType: LiveData<MatchType?>
        get() = _curMatchType

    val curChildMatchType: LiveData<MatchType?>
        get() = _curChildMatchType

    val sportMenuResult: LiveData<SportMenuResult?>
        get() = _sportMenuResult

    val sportCouponMenuResult: LiveData<Event<SportCouponMenuResult>>
        get() = _sportCouponMenuResult


    val oddsListGameHallResult: LiveData<Event<OddsListResult?>>
        get() = _oddsListGameHallResult

//    val quickOddsListGameHallResult: LiveData<Event<OddsListResult?>>
//        get() = _quickOddsListGameHallResult

    val oddsListGameHallIncrementResult: LiveData<Event<OddsListIncrementResult?>>
        get() = _oddsListGameHallIncrementResult

    val oddsListResult: LiveData<Event<OddsListResult?>>
        get() = _oddsListResult

    val oddsListIncrementResult: LiveData<Event<OddsListIncrementResult?>>
        get() = _oddsListIncrementResult

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

    val isNoEvents: LiveData<Boolean>
        get() = _isNoEvents

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
    val searchResult: LiveData<Event<List<SearchResult>?>>
        get() = _searchResult


    val withdrawSystemOperation =
        withdrawRepository.withdrawSystemOperation
    val rechargeSystemOperation =
        withdrawRepository.rechargeSystemOperation
    val needToUpdateWithdrawPassword =
        withdrawRepository.needToUpdateWithdrawPassword //提款頁面是否需要更新提款密碼 true: 需要, false: 不需要
    val settingNeedToUpdateWithdrawPassword =
        withdrawRepository.settingNeedToUpdateWithdrawPassword //提款設置頁面是否需要更新提款密碼 true: 需要, false: 不需要
    val settingNeedToCompleteProfileInfo =
        withdrawRepository.settingNeedToCompleteProfileInfo //提款設置頁面是否需要完善個人資料 true: 需要, false: 不需要
    val needToCompleteProfileInfo =
        withdrawRepository.needToCompleteProfileInfo //提款頁面是否需要完善個人資料 true: 需要, false: 不需要
    val needToBindBankCard =
        withdrawRepository.needToBindBankCard //提款頁面是否需要新增銀行卡 -1 : 不需要新增, else : 以value作為string id 顯示彈窗提示
    val needToSendTwoFactor =
        withdrawRepository.showSecurityDialog //判斷是不是要進行手機驗證 true: 需要, false: 不需要

    val showBetUpperLimit = betInfoRepository.showBetUpperLimit

    private val _messageListResult = MutableLiveData<Event<MessageListResult?>>()
    val _curMatchType = MutableLiveData<MatchType?>()
    private val _curChildMatchType = MutableLiveData<MatchType?>()
    private val _sportMenuResult = MutableLiveData<SportMenuResult?>()
    private val _sportCouponMenuResult = MutableLiveData<Event<SportCouponMenuResult>>()
    private val _oddsListGameHallResult = MutableLiveData<Event<OddsListResult?>>()

    private val _oddsListGameHallIncrementResult =
        MutableLiveData<Event<OddsListIncrementResult?>>()
    private val _oddsListResult = MutableLiveData<Event<OddsListResult?>>()
    private val _oddsListIncrementResult = MutableLiveData<Event<OddsListIncrementResult?>>()
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
    private var _isNoEvents = MutableLiveData<Boolean>()
    private val _errorPromptMessage = MutableLiveData<Event<String>>()
    private val _specialEntrance = MutableLiveData<SpecialEntrance?>()
    private val _outrightCountryListSearchResult =
        MutableLiveData<List<org.cxct.sportlottery.network.outright.season.Row>>()
    private val _leagueSelectedList = MutableLiveData<List<League>>()
    private val _leagueSubmitList = MutableLiveData<Event<List<League>>>()
    private val _leagueFilterList = MutableLiveData<List<League>>()
    private val _playList = MutableLiveData<List<Play>>()
    private val _playCate = MutableLiveData<String?>()
    private val _searchResult = MutableLiveData<Event<List<SearchResult>?>>()
    private val _navDetail = MutableLiveData<Event<NavDirections>>()


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

    private val _betInfoResult = MutableLiveData<Event<BetInfoResult?>>()
    val betInfoResult: LiveData<Event<BetInfoResult?>>
        get() = _betInfoResult

    private val _oddsDetailResult = MutableLiveData<Event<OddsDetailResult?>?>()
    val oddsDetailResult: LiveData<Event<OddsDetailResult?>?>
        get() = _oddsDetailResult

    private val _oddsDetailList = MutableLiveData<Event<ArrayList<OddsDetailListData>>>()
    val oddsDetailList: LiveData<Event<ArrayList<OddsDetailListData>>>
        get() = _oddsDetailList

    private val _checkInListFromSocket = MutableLiveData<Boolean>()
    val checkInListFromSocket: LiveData<Boolean>
        get() = _checkInListFromSocket

    //賽事直播網址
    private val _matchLiveInfo = MutableLiveData<Event<LiveStreamInfo>?>()
    val matchLiveInfo: LiveData<Event<LiveStreamInfo>?>
        get() = _matchLiveInfo

    //賽事動畫網址
    private val _matchTrackerUrl = MutableLiveData<Event<MatchTrackerUrl>>()
    val matchTrackerUrl: LiveData<Event<MatchTrackerUrl>>
        get() = _matchTrackerUrl

    //Loading
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private var _isLoading = MutableLiveData<Boolean>()

    //ErrorDialog
    val showErrorDialogMsg: LiveData<String>
        get() = _showErrorDialogMsg
    private var _showErrorDialogMsg = MutableLiveData<String>()

    private val _sportMenuList = MutableLiveData<Event<List<SportMenu>>>()
    val sportMenuList: LiveData<Event<List<SportMenu>>>
        get() = _sportMenuList

    private val _sportSortList = MutableLiveData<Event<List<SportMenu>>>()
    val sportSortList: LiveData<Event<List<SportMenu>>>
        get() = _sportSortList

    private val _sportMenuFilterList = MutableLiveData<Event<MutableMap<String?, MutableMap<String?, SportMenuFilter>?>?>>()
    val sportMenuFilterList: LiveData<Event<MutableMap<String?, MutableMap<String?, SportMenuFilter>?>?>>
        get() = _sportMenuFilterList

    private val _publicityRecommend = MutableLiveData<Event<RecommendResult>>()
    val publicityRecommend: LiveData<Event<RecommendResult>>
        get() = _publicityRecommend

    //發送簡訊碼之後60s無法再發送
    val twoFactorResult: LiveData<BaseSecurityCodeResult?>
        get() = _twoFactorResult
    private val _twoFactorResult = MutableLiveData<BaseSecurityCodeResult?>()

    //錯誤提示
    val errorMessageDialog: LiveData<String?>
        get() = _errorMessageDialog
    private val _errorMessageDialog = MutableLiveData<String?>()

    //認證成功
    val twoFactorSuccess: LiveData<Boolean?>
        get() = _twoFactorSuccess
    private val _twoFactorSuccess = MutableLiveData<Boolean?>()

    //需要完善個人資訊(缺電話號碼) needPhoneNumber
    val showPhoneNumberMessageDialog = withdrawRepository.hasPhoneNumber

    var sportQueryData: SportQueryData? = null
    var specialMenuData: SportQueryData? = null
    var allSearchData: List<SearchResponse.Row>? = null


    private var lastSportTypeHashMap: HashMap<String, String?> = hashMapOf(
        MatchType.IN_PLAY.postValue to null,
        MatchType.AT_START.postValue to null,
        MatchType.TODAY.postValue to null,
        MatchType.EARLY.postValue to null,
        MatchType.OUTRIGHT.postValue to null,
        MatchType.PARLAY.postValue to null,
        MatchType.EPS.postValue to null
    )

    fun navDirectEntrance(matchType: MatchType, gameType: GameType?) {
        _specialEntrance.postValue(SpecialEntrance(matchType, gameType))
    }

    fun navSpecialEntrance(matchType: MatchType, gameType: GameType?) {
        _specialEntrance.postValue(getSpecEntranceFromHome(matchType, gameType))
        gameType?.let { recordSportType(matchType, it.key) }
    }

    fun navSpecialEntrance(
        matchType: MatchType,
        gameType: GameType?,
        couponCode: String,
        couponName: String
    ) {
        _specialEntrance.postValue(SpecialEntrance(matchType, gameType, couponCode, couponName))
        gameType?.let { recordSportType(matchType, it.key) }
    }

    fun navSpecialEntrance(
        entranceMatchType: MatchType,
        gameType: GameType?,
        matchId: String,
        gameMatchType: MatchType? = null
    ) {
        _specialEntrance.postValue(SpecialEntrance(entranceMatchType = entranceMatchType, gameType, matchID = matchId, gameMatchType = gameMatchType))
    }

    private fun getSpecEntranceFromHome(
        matchType: MatchType,
        gameType: GameType?
    ): SpecialEntrance = when {
        matchType == MatchType.OTHER -> {
            SpecialEntrance(matchType, gameType, "_sportCouponMenuResult.value?.couponCode")
        }
        else -> {
            SpecialEntrance(matchType, gameType)
        }
    }

    fun setSportClosePromptMessage(sport: String) {
        _errorPromptMessage.postValue(
            Event(
                String.format(
                    androidContext.getString(R.string.message_no_sport_game),
                    sport
                )
            )
        )
    }

    fun switchMainMatchType() {
        _curChildMatchType.value = null
        _oddsListGameHallResult.value = Event(null)
        //_quickOddsListGameHallResult.value = Event(null)
        _oddsListResult.value = Event(null)
        _curMatchType.value = MatchType.MAIN
        filterLeague(listOf())
    }

    fun switchMatchType(matchType: MatchType) {
        _curChildMatchType.value = null
        _oddsListGameHallResult.value = Event(null)
        //_quickOddsListGameHallResult.value = Event(null)
        _oddsListResult.value = Event(null)
        getSportMenu(matchType, onlyRefreshSportMenu = false)
        getAllPlayCategory(matchType)
        filterLeague(listOf())
    }

    fun switchChildMatchType(childMatchType: MatchType? = null) {
        _curChildMatchType.value = childMatchType
        _oddsListGameHallResult.value = Event(null)
        //_quickOddsListGameHallResult.value = Event(null)
        _oddsListResult.value = Event(null)
        if (childMatchType == MatchType.OTHER_OUTRIGHT) {
//            getLeagueList(
//                getSportSelectedCode(MatchType.OTHER_OUTRIGHT) ?: "",
//                currentSpecialCode,
//                null,
//                isIncrement = false
//            )
            //aaaaa
//            getOutrightSeasonList(
//                getSportSelectedCode(MatchType.OTHER_OUTRIGHT) ?: "",
//                true
//            )
            getOutrightOddsList(getSportSelectedCode(MatchType.OTHER_OUTRIGHT) ?: "")
        }
        if(childMatchType == MatchType.OUTRIGHT) {
//            getOutrightSeasonList(
//                getSportSelectedCode(MatchType.OUTRIGHT) ?: "",
//                false
//            )
            getOutrightOddsList(getSportSelectedCode(_curMatchType.value!!) ?: "")
        }
        else if (childMatchType == MatchType.OTHER) {
            getGameHallList(
                matchType = MatchType.OTHER,
                isReloadDate = true,
                isReloadPlayCate = true,
                isLastSportType = true
            )
        }
        else {
            curMatchType.value?.let {
                getGameHallList(matchType = it, isReloadDate = true, isReloadPlayCate = true)
            }
        }
    }

    //獲取系統公告
    fun getAnnouncement() {
//        if (isLogin.value == true) {
//
//        } else {
////            _messageListResult.value = Event(null)
//            viewModelScope.launch {
//                doNetwork(androidContext) {
//                    val typeList = arrayOf(2, 3)
//                    OneBoSportApi.messageService.getPromoteNotice(typeList)
//                }?.let { result -> _messageListResult.postValue(Event(result)) }
//            }
//        }

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
                        matchInfo.homeName.contains(key, true) ||
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
                        matchInfo.homeName.contains(key, true) ||
                                matchInfo.awayName.contains(key, true)
                    }
                }
            }
            leagueMatchSearchResult?.forEachIndexed { index, league ->
                var searchResultLeagueList: MutableList<SearchResult.SearchResultLeague> =
                    arrayListOf()
                league.forEach {leagueMatch ->
                    var searchResultLeague = SearchResult.SearchResultLeague(leagueMatch.leagueName)
                    searchResultLeagueList.add(searchResultLeague)
                }
                finalResult?.get(index).searchResultLeague = searchResultLeagueList
            }
            //3.篩選比賽
            var matchSearchResult = leagueMatchSearchResult?.map { row ->
                row.map { leagueMatch ->
                    leagueMatch.matchInfoList.filter { matchInfo ->
                        matchInfo.homeName.contains(key, true) ||
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
                    finalResult?.get(index0).searchResultLeague.get(index1).leagueMatchList = matchList
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

    fun getSportList() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.sportService.getSportList()
            }
            result?.let { sportList ->
                val sportCardList = sportList.rows.sortedBy { it.sortNum }
                    .mapNotNull { row ->
                        GameType.getGameType(row.code)
                            ?.let { gameType ->
                                SportMenu(
                                    gameType,
                                    row.name,
                                    getSpecificLanguageString(androidContext, gameType.key, LanguageManager.Language.EN.key),
                                    getGameTypeMenuIcon(gameType)
                                )
                            }
                    }
                _sportSortList.postValue(Event(sportCardList))
                getSportMenu()
            }
        }
    }

    //獲取體育篩選菜單
    fun getSportMenuFilter() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.sportService.getSportListFilter()
            }

            result?.let {
                PlayCateMenuFilterUtils.filterList = it.t?.sportMenuList
                _sportMenuFilterList.postValue(Event(it.t?.sportMenuList))
            }
        }
    }

    //獲取體育菜單
    fun getSportMenu() {
        getSportMenu(null)
    }

    fun getSportMenu(
        matchType: MatchType?,
        switchFirstTag: Boolean = false,
        onlyRefreshSportMenu: Boolean = true
    ) {
        if (!onlyRefreshSportMenu)
            _isLoading.postValue(true)

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
                    matchType,
                    lastSportTypeHashMap[matchType?.postValue]
                )
            }
            //單純更新gameTypeAdapter就不需要更新當前MatchType，不然畫面會一直閃 by Bill
            if (!onlyRefreshSportMenu)
                _curMatchType.value = matchType

            val couponResult = doNetwork(androidContext) {
                sportMenuRepository.getSportCouponMenu()
            }

            couponResult?.let {
                _sportCouponMenuResult.postValue(Event(it))
            }

            //Socket更新自動選取第一個有賽事的球種
            if (switchFirstTag) {
                matchType?.let { switchFirstSportType(it) }
            }
        }

        _isLoading.postValue(false) // TODO IllegalStateException: Cannot invoke setValue on a background thread
    }

    fun getAllPlayCategory(matchType: MatchType) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.sportService.getQuery(
                    SportQueryRequest(
                        TimeUtil.getNowTimeStamp().toString(),
                        TimeUtil.getTodayStartTimeStamp().toString(),
                        matchType.postValue
                    )
                )
            }.let { result ->
                if (result?.success == true) {
                    sportQueryData = result.sportQueryData
                    checkLastSportType(matchType, sportQueryData)
                    _isNoEvents.value = result.sportQueryData?.num == 0
                } else {
                    _showErrorDialogMsg.value = result?.msg
                }
            }
        }
    }

    var currentSpecialCode = ""

    fun resetOtherSeelectedGameType() {
        specialMenuData = null
    }

    fun getAllPlayCategoryBySpecialMatchType(code: String = _specialEntrance.value?.couponCode ?: currentSpecialCode, item: Item? = null, isReload: Boolean = false) {
        currentSpecialCode = code

        if (code.isEmpty()) return

        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.sportService.getQuery(
                    SportQueryRequest(
                        TimeUtil.getNowTimeStamp().toString(),
                        TimeUtil.getTodayStartTimeStamp().toString(),
                        code
                    )
                )
            }.let { result ->
                if (result?.success == true) {
                    var items = result.sportQueryData?.items
                    var gameCode = item?.code ?: getSportSelectedCode(MatchType.OTHER)
                    if (items?.filter { it.code == gameCode }.isNullOrEmpty()) {
                        gameCode = items?.getOrNull(0)?.code ?: GameType.FT.key
                    }
                    sportQueryData = result.sportQueryData
                    specialMenuData = result.sportQueryData
                    specialMenuData?.updateSportSelectState(gameCode)
                    _sportMenuResult.postValue(null)
                    getPlayCategory(MatchType.OTHER)

                    if (isReload && items?.isNotEmpty() == true && gameCode != null) {
                        val defaultItem = items?.firstOrNull { it.code == gameCode }
                        if (defaultItem?.play == null) {
                            getLeagueList(
                                gameCode,
                                code,
                                null,
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
                    }
                } else {
                    _showErrorDialogMsg.value = result?.msg
                }
            }
        }
    }

    private fun getLeaguePlayCategory(matchType: MatchType, leagueIdList: List<String>) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.sportService.getQuery(
                    SportQueryRequest(
                        TimeUtil.getNowTimeStamp().toString(),
                        TimeUtil.getTodayStartTimeStamp().toString(),
                        matchType.postValue,
                        leagueIdList = leagueIdList
                    )
                )
            }.let { result ->
                if (result?.success == true) {
                    sportQueryData = result.sportQueryData
                    getPlayCategory(matchType)
                } else {
                    _showErrorDialogMsg.value = result?.msg
                }
            }
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

        _sportSortList.value?.peekContent()?.let { list ->
            list.forEach { sportMenu ->
                sportMenu.apply {
                    gameCount =
                        getSportCount(MatchType.IN_PLAY, gameType, sportMenuResult) + getSportCount(
                            MatchType.TODAY,
                            gameType,
                            sportMenuResult
                        ) + getSportCount(MatchType.EARLY, gameType, sportMenuResult) +
                                getSportCount(
                                    MatchType.PARLAY,
                                    gameType,
                                    sportMenuResult
                                ) + getSportCount(
                            MatchType.OUTRIGHT,
                            gameType,
                            sportMenuResult
                        ) + getSportCount(MatchType.AT_START, gameType, sportMenuResult) +
                                getSportCount(MatchType.EPS, gameType, sportMenuResult)

                    entranceType = when {
                        getSportCount(MatchType.TODAY, gameType, sportMenuResult) != 0 -> {
                            MatchType.TODAY
                        }
                        getSportCount(MatchType.EARLY, gameType, sportMenuResult) != 0 -> {
                            MatchType.EARLY
                        }
                        getSportCount(MatchType.PARLAY, gameType, sportMenuResult) != 0 -> {
                            MatchType.PARLAY
                        }
                        getSportCount(MatchType.OUTRIGHT, gameType, sportMenuResult) != 0 -> {
                            MatchType.OUTRIGHT
                        }
                        else -> null
                    }
                }
            }
            _sportMenuList.postValue(Event(list))
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

    //遊戲大廳首頁: 滾球盤
    fun getMatchPreloadInPlay() {
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
                //Martin
                result.matchPreloadData?.datas?.forEach { data ->
                    data.matchOdds.forEach { matchOdd ->
                        matchOdd.sortOddsMap()
                        matchOdd.oddsMap?.forEach { map ->
                            map.value?.forEach { odd ->
                                odd?.isSelected =
                                    betInfoRepository.betInfoList.value?.peekContent()?.any {
                                        it.matchOdd.oddsId == odd?.id
                                    }
                            }
                        }
                        matchOdd.setupOddDiscount()
                        matchOdd.updateOddStatus()
                    }
                }

                _matchPreloadInPlay.postValue(Event(result))
            }
        }
    }

    //遊戲大廳首頁: 即將開賽盤
    fun getMatchPreloadAtStart() {
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
                        matchOdd.sortOddsMap()
                        //計算且賦值 即將開賽 的倒數時間
                        matchOdd.matchInfo?.apply {
                            remainTime = startTime?.let { TimeUtil.getRemainTime(it) }
                        }

                        //mapping 下注單裡面項目 & 賠率按鈕 選擇狀態
                        matchOdd.oddsMap?.forEach { map ->
                            map.value?.forEach { odd ->
                                odd?.isSelected =
                                    betInfoRepository.betInfoList.value?.peekContent()?.any {
                                        it.matchOdd.oddsId == odd?.id
                                    }
                            }
                        }
                        matchOdd.setupOddDiscount()
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
                result.t?.odds?.forEach { oddData ->
                    oddData.sortOddsMap()
                }
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
                        oddData.sortOddsMap() //按照188排序 不使用markSort by Bill
                        oddData.oddsMap?.forEach { map ->
                            map.value?.forEach { odd ->
                                odd?.isSelected =
                                    betInfoRepository.betInfoList.value?.peekContent()?.any {
                                        it.matchOdd.oddsId == odd?.id
                                    }
                            }
                        }
                        oddData.playCateMappingList = playCateMappingList
                        oddData.setupOddDiscount()
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
                    oddData.sortOddsMap()
                    oddData.oddsMap?.forEach { map ->
                        map.value?.forEach { odd ->
                            odd?.isSelected =
                                betInfoRepository.betInfoList.value?.peekContent()?.any {
                                    it.matchOdd.oddsId == odd?.id
                                }
                        }
                    }

                    oddData.setupOddDiscount()
                    oddData.updateOddStatus()
                }

                _highlightMatchResult.postValue(Event(result))

                notifyFavorite(FavoriteType.MATCH)
            }
        }
    }

    fun switchSportType(matchType: MatchType, item: Item) {
        if (matchType == MatchType.OTHER) {
            specialMenuData?.updateSportSelectState(item.code)
        } else {
            _sportMenuResult.value?.updateSportSelectState(matchType, item.code)
        }
        _curChildMatchType.value = null
        _oddsListGameHallResult.value = Event(null)
        //_quickOddsListGameHallResult.value = Event(null)
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
            //getGameHallList(matchType, true, isReloadPlayCate = true)
        } else {
            getGameHallList(matchType, true, isReloadPlayCate = true)
        }
        getMatchCategoryQuery(matchType)
        filterLeague(listOf())
    }

    //自動選取第一個有賽事的球種
    private fun switchFirstSportType(matchType: MatchType) {
        when (matchType) {
            MatchType.IN_PLAY -> {
                val gameTypeCode =
                    if (sportMenuResult.value?.sportMenuData?.menu?.inPlay?.items.isNullOrEmpty()) GameType.FT.key else sportMenuResult.value?.sportMenuData?.menu?.inPlay?.items?.first()?.code.toString()
                switchSportType(
                    matchType,
                    gameTypeCode
                )
            }
            MatchType.TODAY -> {
                val gameTypeCode =
                    if (sportMenuResult.value?.sportMenuData?.menu?.today?.items.isNullOrEmpty()) GameType.FT.key else sportMenuResult.value?.sportMenuData?.menu?.today?.items?.first()?.code.toString()
                switchSportType(
                    matchType,
                    gameTypeCode
                )
            }
            MatchType.EARLY -> {
                val gameTypeCode =
                    if (sportMenuResult.value?.sportMenuData?.menu?.early?.items.isNullOrEmpty()) GameType.FT.key else sportMenuResult.value?.sportMenuData?.menu?.early?.items?.first()?.code.toString()
                switchSportType(
                    matchType,
                    gameTypeCode
                )
            }
            MatchType.PARLAY -> {
                val gameTypeCode =
                    if (sportMenuResult.value?.sportMenuData?.menu?.parlay?.items.isNullOrEmpty()) GameType.FT.key else sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.first()?.code.toString()
                switchSportType(
                    matchType,
                    gameTypeCode
                )
            }
            MatchType.OUTRIGHT -> {
                val gameTypeCode =
                    if (sportMenuResult.value?.sportMenuData?.menu?.outright?.items.isNullOrEmpty()) GameType.FT.key else sportMenuResult.value?.sportMenuData?.menu?.outright?.items?.first()?.code.toString()
                switchSportType(
                    matchType,
                    gameTypeCode
                )
            }
            MatchType.AT_START -> {
                val gameTypeCode =
                    if (sportMenuResult.value?.sportMenuData?.atStart?.items.isNullOrEmpty()) GameType.FT.key else sportMenuResult.value?.sportMenuData?.atStart?.items?.first()?.code.toString()
                switchSportType(
                    matchType,
                    gameTypeCode
                )
            }
            MatchType.EPS -> {
                val gameTypeCode =
                    if (sportMenuResult.value?.sportMenuData?.menu?.eps?.items.isNullOrEmpty()) GameType.FT.key else sportMenuResult.value?.sportMenuData?.menu?.eps?.items?.first()?.code.toString()
                switchSportType(
                    matchType,
                    gameTypeCode
                )
            }
            else -> {

            }
        }
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
            //getGameHallList(matchType, true, isReloadPlayCate = true)

        } else {
            getGameHallList(matchType, true, isReloadPlayCate = true)
        }
        getMatchCategoryQuery(matchType)
        filterLeague(listOf())
    }

    private fun recordSportType(matchType: MatchType, sportType: String) {
        lastSportTypeHashMap[matchType.postValue] = sportType
    }

    fun switchPlay(matchType: MatchType, play: Play) {
        updatePlaySelectedState(play)
    }

    fun switchPlayCategory(matchType: MatchType, play: Play, playCateCode: String?) {
        _playList.value?.forEach {
            it.isSelected = (it == play)
        }
        _playCate.value = playCateCode
    }

    fun switchMatchDate(matchType: MatchType, date: Date) {
        _curDate.value?.updateDateSelectedState(date)

        getGameHallList(matchType, false, date.date)
    }

    fun checkGameInList(matchType: MatchType, leagueIdList: List<String>? = null) {
        val nowMatchType = curMatchType.value ?: matchType
        val nowChildMatchType = curChildMatchType.value ?: matchType
        val sportCode = getSportSelectedCode(nowMatchType)
        sportCode?.let { code ->
            when (nowChildMatchType) {
                MatchType.IN_PLAY -> {
                    checkOddsList(
                        code,
                        nowChildMatchType.postValue,
                        leagueIdList = leagueIdList,
                    )
                }
                MatchType.AT_START -> {
                    checkOddsList(
                        code,
                        nowChildMatchType.postValue,
                        leagueIdList = leagueIdList,
                    )
                }
                MatchType.OTHER -> {
                    checkOddsList(
                        code,
                        specialEntrance.value?.couponCode ?: "",
                        leagueIdList = leagueIdList,
                    )
                }
                else -> {
                }
            }
        }
    }

    private fun checkOddsList(gameType: String, matchType: String, leagueIdList: List<String>? = null) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.oddsService.getOddsList(
                    OddsListRequest(
                        gameType,
                        matchType,
                        leagueIdList = leagueIdList,
                        playCateMenuCode = getPlayCateSelected()?.code ?: "MAIN"
                    )
                )
            }?.let {
                if (!it.oddsListData?.leagueOdds.isNullOrEmpty()) {
                    _checkInListFromSocket.postValue(true)
                }
            }
        }
    }

    fun getGameHallList(
        matchType: MatchType,
        isReloadDate: Boolean,
        date: String? = null,
        leagueIdList: List<String>? = null,
        isReloadPlayCate: Boolean = false,
        isLastSportType: Boolean = false,
        isIncrement: Boolean = false
    ) {
        val nowMatchType = curMatchType.value ?: matchType
        val nowChildMatchType = curChildMatchType.value ?: matchType

        if (isReloadPlayCate) {
            getPlayCategory(nowChildMatchType)
        }

        if (isReloadDate) {
            getDateRow(nowChildMatchType)
        }

        if (isLastSportType)
            _sportMenuResult.value?.updateSportSelectState(
                matchType,
                lastSportTypeHashMap[matchType.postValue]
            )

        val sportCode = getSportSelectedCode(nowMatchType)

//        if(sportCode == null && nowChildMatchType == MatchType.OTHER) {
//            getOddsList(
//                code,
//                specialEntrance.value?.couponCode ?: "",
//                getCurrentTimeRangeParams(),
//                leagueIdList = leagueIdList,
//                isIncrement = isIncrement
//            )
//        }

        sportCode?.let { code ->
            when (nowChildMatchType) {
                MatchType.MAIN -> {
                    getOddsList(
                        code,
                        specialEntrance.value?.couponCode ?: "",
                        getCurrentTimeRangeParams(),
                        leagueIdList = leagueIdList,
                        isIncrement = isIncrement
                    )
                }
                MatchType.IN_PLAY -> {
                    getOddsList(
                        code,
                        nowChildMatchType.postValue,
                        leagueIdList = leagueIdList,
                        isIncrement = isIncrement
                    )
                }
                MatchType.TODAY -> {
                    getLeagueList(
                        gameType = code,
                        matchType = nowChildMatchType.postValue,
                        startTime = TimeUtil.getTodayStartTimeStamp().toString(),
                        endTime = TimeUtil.getTodayEndTimeStamp().toString(),
                    )
                }
                MatchType.EARLY -> {
                    val tr = TimeUtil.getEarlyAllTimeRangeParams()
                    getLeagueList(
                        gameType = code,
                        matchType = nowChildMatchType.postValue,
                        startTime = tr.startTime ?: "",
                        endTime = tr.endTime,
                        isIncrement = isIncrement
                    )
                }
                MatchType.PARLAY -> {
                    getLeagueList(
                        code,
                        nowChildMatchType.postValue,
                        getCurrentTimeRangeParams(),
                        date,
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
                        nowChildMatchType.postValue,
                        TimeUtil.getAtStartTimeRangeParams(),
                        leagueIdList = leagueIdList,
                        isIncrement = isIncrement
                    )
                }
                MatchType.EPS -> {
                    getEpsList(code, startTime = TimeUtil.getTodayStartTimeStamp())
                }
                MatchType.OTHER -> {
                    getOddsList(
                        code,
                        specialEntrance.value?.couponCode ?: "",
                        getCurrentTimeRangeParams(),
                        leagueIdList = leagueIdList,
                        isIncrement = isIncrement
                    )
                }
                MatchType.OTHER_OUTRIGHT -> {
                    //getOutrightSeasonList(code, true)
                    getOutrightOddsList(code)

                }
                MatchType.MY_EVENT -> {
                    getOddsList(
                        code,
                        specialEntrance.value?.couponCode ?: "",
                        getCurrentTimeRangeParams(),
                        leagueIdList = leagueIdList,
                        isIncrement = isIncrement
                    )
                }
                MatchType.OTHER_EPS -> {

                }
                else -> {
                }
            }
        }

        _isNoHistory.postValue(sportCode == null)
    }

    fun switchPlay(
        matchType: MatchType,
        leagueIdList: List<String>,
        matchIdList: List<String>,
        play: Play
    ) {
        updatePlaySelectedState(play)

//        getLeagueOddsList(matchType, leagueIdList, matchIdList)
    }

    fun switchPlayCategory(
        matchType: MatchType,
        leagueIdList: List<String>,
        matchIdList: List<String>,
        play: Play,
        playCateCode: String?
    ) {
        _playList.value?.forEach {
            it.isSelected = (it == play)
        }
        _playCate.value = playCateCode

//        getLeagueOddsList(matchType, leagueIdList, matchIdList)
    }

    /**
     * @update by Dean, 20210927, 配合socket event: league_change 新增參數 isIncrement, 作為識別是否增量更新
     */
    fun getLeagueOddsList(
        matchType: MatchType,
        leagueIdList: List<String>,
        matchIdList: List<String>,
        isReloadPlayCate: Boolean = false,
        isIncrement: Boolean = false
    ) {

        if (isReloadPlayCate && !isIncrement) {
            getLeaguePlayCategory(matchType, leagueIdList)
        }

        val nowMatchType = curChildMatchType.value ?: matchType

        getSportSelected(nowMatchType)?.let { item ->
            getOddsList(
                item.code,
                matchType.postValue,
                getCurrentTimeRangeParams(),
                leagueIdList,
                matchIdList,
                isIncrement
            )
        }
    }

    fun refreshGame(
        matchType: MatchType,
        leagueIdList: List<String>? = null,
        matchIdList: List<String>? = null
    ) {
        val nowMatchType = curChildMatchType.value ?: matchType
        val timeRangeParams =
            if (matchType == MatchType.IN_PLAY) null else getCurrentTimeRangeParams()
        getSportSelected(nowMatchType)?.let { item ->
            getOddsList(
                item.code,
                matchType.postValue,
                timeRangeParams,
                leagueIdList,
                matchIdList,
                false
            )
        }
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
    fun getOutrightOddsList(gameType: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.outrightService.getOutrightOddsList(
                    OutrightOddsListRequest(
                        gameType,
                        matchType = MatchType.OUTRIGHT.postValue
                    )
                )
            }

            result?.outrightOddsListData?.leagueOdds?.forEach { leagueOdd ->
                leagueOdd.matchOdds?.forEach { matchOdd ->
                    matchOdd?.oddsMap?.values?.forEach { oddList ->
                        oddList?.updateOddSelectState()
                    }

                    matchOdd?.setupOddDiscount()
                    matchOdd?.setupPlayCate()
                    matchOdd?.sortOdds()

                    matchOdd?.startDate = TimeUtil.timeFormat(matchOdd?.matchInfo?.endTime, DMY_FORMAT)
                    matchOdd?.startTime = TimeUtil.timeFormat(matchOdd?.matchInfo?.endTime, HM_FORMAT)
                }
            }

            val matchOdd =
                result?.outrightOddsListData?.leagueOdds?.firstOrNull()?.matchOdds?.firstOrNull()
            matchOdd?.let {
                matchOdd.playCateMappingList = playCateMappingList
                matchOdd.updateOddStatus()
            }

            _outrightOddsListResult.postValue(Event(result))
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

                    matchOdd?.setupOddDiscount()
                    matchOdd?.setupPlayCate()
                    matchOdd?.sortOdds()

                    matchOdd?.startDate = TimeUtil.timeFormat(matchOdd?.matchInfo?.endTime, DMY_FORMAT)
                    matchOdd?.startTime = TimeUtil.timeFormat(matchOdd?.matchInfo?.endTime, HM_FORMAT)
                }
            }

            val matchOdd =
                result?.outrightOddsListData?.leagueOdds?.firstOrNull()?.matchOdds?.firstOrNull()
            matchOdd?.let {
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
        isIncrement: Boolean = false
    ) {
        var currentTimeRangeParams: TimeRangeParams? = null
        when (matchType) {
            MatchType.IN_PLAY.postValue, MatchType.AT_START.postValue, MatchType.OTHER.postValue -> {
                _oddsListResult.postValue(Event(null))
                currentTimeRangeParams = timeRangeParams
            }
            MatchType.TODAY.postValue, MatchType.EARLY.postValue, MatchType.PARLAY.postValue -> {
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
        if(matchType != MatchType.OTHER.postValue) { // 特殊賽事則不帶時間 Ex: {gameType: "FT", matchType: "sc:QAtest", playCateMenuCode: "MAIN"}
            startTime = timeFilter(currentTimeRangeParams?.startTime) ?: ""
            endTime = timeFilter(currentTimeRangeParams?.endTime) ?: ""
        }

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
                        playCateMenuCode = getPlayCateSelected()?.code ?: "MAIN"
                    )
                )
            }?.updateMatchType()

            result?.oddsListData?.leagueOdds?.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.sortOddsMap()
                    matchOdd.matchInfo?.let { matchInfo ->
                        matchInfo.startDateDisplay =
                            TimeUtil.timeFormat(matchInfo.startTime, "dd/MM")

                        matchOdd.matchInfo.startTimeDisplay =
                            TimeUtil.timeFormat(matchInfo.startTime, "HH:mm")

                        matchInfo.remainTime = TimeUtil.getRemainTime(matchInfo.startTime)
                    }

                    matchOdd.playCateMappingList = playCateMappingList

                    matchOdd.oddsMap?.forEach { map ->
                        map.value?.updateOddSelectState()
                    }

//                    matchOdd.setupPlayCate()
//                    matchOdd.refactorPlayCode() //改成在OddButtonPagerAdapter處理
                    matchOdd.sortOdds()

                    if (!getPlayCateCodeList().isNullOrEmpty())
                        matchOdd.oddsMap?.entries?.retainAll { getPlayCateCodeList()?.contains(it.key) == true }

                    matchOdd.setupOddDiscount()
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
                        _oddsListGameHallResult.postValue(Event(result))
                        //_quickOddsListGameHallResult.postValue(Event(result))
                    }
                }

                MatchType.TODAY.postValue, MatchType.EARLY.postValue, MatchType.PARLAY.postValue, MatchType.OTHER.postValue -> {
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
                    //_quickOddsListGameHallResult.postValue(Event(result))
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

    fun getQuickList(matchId: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.oddsService.getQuickList(
                    QuickListRequest(matchId)
                )
            }

            result?.quickListData?.let {
                val discount = userInfo.value?.discount ?: 1.0F
                it.quickOdds?.forEach { (_, quickOddsValue) ->
                    quickOddsValue.forEach { (key, value) ->
                        value?.forEach { odd ->
                            odd?.odds = odd?.odds?.applyDiscount(discount)
                            odd?.hkOdds = odd?.hkOdds?.applyHKDiscount(discount)

                            if (key == QuickPlayCate.QUICK_EPS.value) {
                                odd?.extInfo =
                                    odd?.extInfo?.toDouble()?.applyDiscount(discount)?.toString()
                            }
                        }
                    }
                }

                _oddsListGameHallResult.postValue(
                    Event(
                        _oddsListGameHallResult.value?.peekContent()
                            ?.updateQuickPlayCate(matchId, it, it.playCateNameMap)
                    )
                )

                _oddsListResult.postValue(
                    Event(
                        _oddsListResult.value?.peekContent()
                            ?.updateQuickPlayCate(matchId, it, it.playCateNameMap)
                    )
                )
            }
        }
    }

//    fun getQuickList2(matchId: String) {
//        viewModelScope.launch {
//            val result = doNetwork(androidContext) {
//                OneBoSportApi.oddsService.getQuickList(
//                    QuickListRequest(matchId)
//                )
//            }
//
//            result?.quickListData?.let {
//                val discount = userInfo.value?.discount ?: 1.0F
//                it.quickOdds?.forEach { (_, quickOddsValue) ->
//                    quickOddsValue.forEach { (key, value) ->
//                        value?.forEach { odd ->
//                            odd?.odds = odd?.odds?.applyDiscount(discount)
//                            odd?.hkOdds = odd?.hkOdds?.applyHKDiscount(discount)
//
//                            if (key == QuickPlayCate.QUICK_EPS.value) {
//                                odd?.extInfo =
//                                    odd?.extInfo?.toDouble()?.applyDiscount(discount)?.toString()
//                            }
//                        }
//                    }
//                }
//
//                _quickOddsListGameHallResult.postValue(
//                    Event(
//                        _quickOddsListGameHallResult.value?.peekContent()
//                            ?.updateQuickPlayCate(matchId, it, it.playCateNameMap)
//                    )
//                )
//
//                _oddsListResult.postValue(
//                    Event(
//                        _oddsListResult.value?.peekContent()
//                            ?.updateQuickPlayCate(matchId, it, it.playCateNameMap)
//                    )
//                )
//            }
//        }
//    }

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
        date: String? = null,
        isIncrement: Boolean = false
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
        isIncrement: Boolean = false
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

    private fun getOutrightSeasonList(gameType: String, isSpecial: Boolean) {
        viewModelScope.launch {
            val outrightLeagueListRequest: OutrightLeagueListRequest
            if (isSpecial) {
                outrightLeagueListRequest =
                    OutrightLeagueListRequest(gameType, _specialEntrance.value?.couponCode)
            } else {
                outrightLeagueListRequest = OutrightLeagueListRequest(gameType)
            }

            val result = doNetwork(androidContext) {
                OneBoSportApi.outrightService.getOutrightSeasonList(
                    outrightLeagueListRequest
                )
            }

            _outrightLeagueListResult.postValue(Event(result))

            notifyFavorite(FavoriteType.LEAGUE)
        }
    }

    private fun getEpsList(
        gameType: String,
        matchType: String = MatchType.EPS.postValue,
        startTime: Long
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
                        matchOddsItem.setupOddDiscount()
                        matchOddsItem.updateOddStatus()
                    }
                }
            }

            _epsListResult.postValue(Event(result))
        }
    }

    private fun getPlayCategory(matchType: MatchType) {
        if (matchType == MatchType.OTHER) {
            sportQueryData?.let { sportQueryData ->
                sportQueryData.items?.find { item ->
                    item.code == getSportSelectedCode(matchType)
                }?.play?.filter { play ->
                    play.num != 0
                }?.let { playList ->
                    playList.forEach {
                        it.isSelected = (it == playList.firstOrNull())
                    }

                    _playList.postValue(playList)
                    _playCate.postValue(null)
                }
            }
        } else {
            sportQueryData?.let { sportQueryData ->
                sportQueryData.items?.find { item ->
                    item.code == getSportSelected(matchType)?.code
                }?.play?.filter { play ->
                    play.num != 0
                }?.let { playList ->
                    playList.forEach {
                        it.isSelected = (it == playList.firstOrNull())
                    }

                    _playList.postValue(playList)
                    _playCate.postValue(null)
                }
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
        val locale = when (LanguageManager.getSelectLanguage(androidContext)) {
            LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> {
                Locale.CHINA
            }
            LanguageManager.Language.VI -> {
                Locale("vi")
            }
            else -> Locale.getDefault()
        }
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
                LanguageManager.Language.VI -> {
                    Locale("vi")
                }
                else -> {
                    Locale.getDefault()
                }
            }
        ).map {
            Date(it, TimeUtil.getDayDateTimeRangeParams(it, locale), isDateFormat = true)
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
                        list.add(
                            OddsDetailListData(
                                key,
                                TextUtil.split(value.typeCodes),
                                value.name,
                                filteredOddList,
                                value.nameMap,
                                value.rowSort
                            )
                        )
                    }

                    result.oddsDetailData?.matchOdd?.odds?.sortPlayCate()
                    result.oddsDetailData?.matchOdd?.setupOddDiscount()
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

                    val animationTrackerId = result.oddsDetailData?.matchOdd?.matchInfo?.trackerId
                    Timber.e("Dean, animationTrackerId = $animationTrackerId")
                    if (!animationTrackerId.isNullOrEmpty()) {
                        doNetwork(androidContext) {
                            OneBoSportApi.matchService.getMatchTrackerUrl(animationTrackerId)
                        }?.let { result ->
                            if (result.success) {
                                _matchTrackerUrl.postValue(Event(result.matchTrackerUrl))
                                Timber.e("Dean, tracker url = ${result.matchTrackerUrl.h5Url}")
                            }
                        }
                    }

                    notifyFavorite(FavoriteType.PLAY_CATE)
                }
            }
        }
    }

    private fun getMatchCount(matchType: MatchType, sportMenuResult: SportMenuResult? = null): Int {
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
        MatchType.OTHER -> {
            null
        }
        else -> null
    }

    fun getSportSelectedCode(matchType: MatchType): String? = when (matchType) {
        MatchType.MAIN -> {
            specialMenuData?.items?.find { it.isSelected }?.code
        }
        MatchType.IN_PLAY -> {
            sportMenuResult.value?.sportMenuData?.menu?.inPlay?.items?.find { it.isSelected }?.code
        }
        MatchType.TODAY -> {
            sportMenuResult.value?.sportMenuData?.menu?.today?.items?.find { it.isSelected }?.code
        }
        MatchType.EARLY -> {
            sportMenuResult.value?.sportMenuData?.menu?.early?.items?.find { it.isSelected }?.code
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


    private fun getPlayCateSelected(): Play? = _playList.value?.find { it.isSelected }

    private fun getPlayCateCodeList(): List<String>? {
        _playCate.value?.let {
            return listOf(it)
        }
        return null
    }

    private fun SportQueryData.updateSportSelectState(
        gameTypeCode: String?
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

    private fun SportMenuData.updateSportSelectState(
        matchType: MatchType?,
        gameTypeCode: String?
    ): SportMenuData {
        this.menu.inPlay.items.map { sport ->
            sport.isSelected = when {
                ((matchType == MatchType.IN_PLAY) && gameTypeCode != null && sport.num > 0) -> {
                    sport.code == gameTypeCode
                }
                else -> {
                    this.menu.inPlay.items.indexOf(sport) == 0
                }
            }
        }
        this.menu.today.items.map { sport ->
            sport.isSelected = when {
                ((matchType == MatchType.TODAY) && gameTypeCode != null && sport.num > 0) -> {
                    sport.code == gameTypeCode
                }
                else -> {
                    this.menu.today.items.indexOf(sport) == 0
                }
            }
        }
        this.menu.early.items.map { sport ->
            sport.isSelected = when {
                ((matchType == MatchType.EARLY) && gameTypeCode != null && sport.num > 0) -> {
                    sport.code == gameTypeCode
                }
                else -> {
                    this.menu.early.items.indexOf(sport) == 0
                }
            }
        }
        this.menu.parlay.items.map { sport ->
            sport.isSelected = when {
                ((matchType == MatchType.PARLAY) && gameTypeCode != null && sport.num > 0) -> {
                    sport.code == gameTypeCode
                }
                else -> {
                    this.menu.parlay.items.indexOf(sport) == 0
                }
            }
        }
        this.menu.outright.items.map { sport ->
            sport.isSelected = when {
                ((matchType == MatchType.OUTRIGHT|| matchType == MatchType.PARLAY) && gameTypeCode != null && sport.num > 0) -> {
                    sport.code == gameTypeCode
                }
                else -> {
                    this.menu.outright.items.indexOf(sport) == 0
                }
            }
        }
        this.atStart.items.map { sport ->
            sport.isSelected = when {
                ((matchType == MatchType.AT_START) && gameTypeCode != null && sport.num > 0) -> {
                    sport.code == gameTypeCode
                }
                else -> {
                    this.atStart.items.indexOf(sport) == 0
                }
            }
        }
        this.menu.eps?.items?.map { sport ->
            sport.isSelected = when {
                ((matchType == MatchType.EPS) && gameTypeCode != null && sport.num > 0) -> {
                    sport.code == gameTypeCode
                }
                else -> {
                    this.menu?.eps?.items.indexOf(sport) == 0
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
     * 設置大廳所需顯示的玩法 (api未回傳的玩法需以“—”表示)
     */
    private fun MatchOdd.setupPlayCate() {
        val sortOrder = this.oddsSort?.split(",")
        this.oddsMap?.let { oddMap ->
            sortOrder?.forEach {
                if (!oddMap.keys.contains(it))
                    oddMap[it] = mutableListOf(null, null, null)
            }
        }
    }

    /**
     * 有些playCateCode後面會給： 要特別做處理
     * */
    private fun MatchOdd.refactorPlayCode() {
        try {
            val oddsMap: MutableMap<String, MutableList<Odd?>?>

            val rgzMap = this.oddsMap?.filter { (key, value) -> key.contains(":") }
            rgzMap?.let {
                if (rgzMap.isNotEmpty()) {
                    oddsMap = this.oddsMap?.filter { !it.key.contains(":") }?.toMutableMap() ?: mutableMapOf()
                    oddsMap[rgzMap.iterator().next().key] = rgzMap.iterator().next().value

                    this.oddsMap?.clear()
                    this.oddsMap?.putAll(oddsMap)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
        quickPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
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
                        quickOddsApi?.toMutableFormat() ?: mutableMapOf()
                    )
                }
                matchOdd.quickPlayCateNameMap = quickPlayCateNameMap
            }
        }
        return this
    }

    /**
     * 設置大廳所需顯示的快捷玩法 (api未回傳的玩法需以“—”表示)
     * 2021.10.25 發現可能會回傳但是是傳null, 故新增邏輯, 該玩法odd為null時也做處理
     */
    private fun MutableMap<String, List<Odd?>?>.setupQuickPlayCate(playCate: String) {
        val playCateSort = QuickPlayCate.values().find { it.value == playCate }?.rowSort?.split(",")

        playCateSort?.forEach {
            if (!this.keys.contains(it) || this[it] == null)
                this[it] = mutableListOf(null, null, null)
        }
    }

    /**
     * 根據QuickPlayCate的rowSort將盤口重新排序
     */
    private fun MutableMap<String, List<Odd?>?>.sortQuickPlayCate(playCate: String) {
        val playCateSort = QuickPlayCate.values().find { it.value == playCate }?.rowSort?.split(",")
        val sortedList = this.toSortedMap(compareBy<String> {
            val oddsIndex = playCateSort?.indexOf(it)
            oddsIndex
        }.thenBy { it })

        this.clear()
        this.putAll(sortedList)
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
        if (nowMatchLiveInfo?.matchId == matchId && nowMatchLiveInfo.isNewest && getNewest) return

        val tempLiveStreamUrl = gameLiveSharedPreferences.getString(matchId, null)
        // Todo 暫停使用，每次都請求最新的，確保沒問題
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

    private val p2StreamSourceList = mutableListOf("hlsmed", "hlslo", "iphonewabsec")

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
            VideoProvider.Own.code -> {
                // Todo: 需改成用 StreamURLs，格式依序採用 RTMP, FLV, M3U8，依次使用。
//                if (response.StreamURLs?.isNotEmpty() == true) {
//                    response.StreamURLs?.first { it.format == "rtmp" }.url ?: response.streamURL
//                } else {
                response.streamURL
//                }
            }
            VideoProvider.P2.code -> {
                val liveUrlResponse = OneBoSportApi.matchService.getLiveP2Url(
                    response.accessToken,
                    sConfigData?.referUrl,
                    response.streamURL
                )
                val streamSource = p2StreamSourceList.firstOrNull { p2Source ->
                    liveUrlResponse.body()?.launchInfo?.streamLauncher?.find { launcher ->
                        launcher.playerAlias == p2Source
                    } != null
                } ?: liveUrlResponse.body()?.launchInfo?.streamLauncher?.firstOrNull()?.playerAlias
                streamSource?.let {
                    return liveUrlResponse.body()?.launchInfo?.streamLauncher?.find { it.playerAlias == streamSource }?.launcherURL
                }
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

    fun clearLiveInfo() {
        _matchLiveInfo.postValue(null)
        _oddsDetailResult.postValue(null)
    }

    //提款功能是否啟用
    fun checkWithdrawSystem() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                withdrawRepository.checkWithdrawSystem()
            }
        }
    }

    //充值功能是否啟用
    fun checkRechargeSystem() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                withdrawRepository.checkRechargeSystem()
            }
        }
    }

    /**
     * 判斷個人資訊是否完整, 若不完整需要前往個人資訊頁面完善資料.
     * complete true: 個人資訊有缺漏, false: 個人資訊完整
     */
    fun checkProfileInfoComplete() {
        viewModelScope.launch {
            withdrawRepository.checkProfileInfoComplete()
        }
    }

    fun checkBankCardPermissions() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                withdrawRepository.checkBankCardPermissions()
            }
        }
    }

    fun setFastBetOpened(isOpen: Boolean) {
        betInfoRepository.setFastBetOpened(isOpen)
    }

    fun getIsFastBetOpened(): Boolean {
        return betInfoRepository.getIsFastBetOpened()
    }

    fun getLoginBoolean(): Boolean {
        return loginRepository.isLogin.value ?: false
    }

    fun resetErrorDialogMsg() {
        _showErrorDialogMsg.value = ""
    }

    //取得使用者是否需要手機驗證
    fun getTwoFactorValidateStatus() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.withdrawService.getTwoFactorStatus()
            }
            if (result?.success == false) { //代表需要驗證
                withdrawRepository.checkUserPhoneNumber()//檢查有沒有手機號碼
            }
        }
    }

    //發送簡訊驗證碼
    fun sendTwoFactor() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.withdrawService.sendTwoFactor()
            }
            _twoFactorResult.postValue(result)
        }
    }

    //双重验证校验
    fun validateTwoFactor(validateTwoFactorRequest: ValidateTwoFactorRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.withdrawService.validateTwoFactor(validateTwoFactorRequest)
            }?.let { result ->
                if (result.success) {
                    _twoFactorSuccess.value = true
                    withdrawRepository.sendTwoFactor()
                } else
                    _errorMessageDialog.value = result.msg
            }
        }
    }

    fun updateBetAmount(input: String) {
        betInfoRepository.updateBetAmount(input)
    }

    //region 宣傳頁用
    fun getRecommend() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                val currentTimeMillis = System.currentTimeMillis()
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = currentTimeMillis
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val startTimeStamp = timeFormat.parse("$year-$month-$day 00:00:00").time

                OneBoSportApi.sportService.getPublicityRecommend(
                    PublicityRecommendRequest(
                        currentTimeMillis.toString(),
                        startTimeStamp.toString()
                    )
                )
            }?.let { result ->
                if (result.success) {
                    result.result.recommendList.forEach { recommend ->
                        with(recommend) {
                            setupMatchType()
                            setupMatchTime()
                            setupPlayCateNum()
                            setupLeagueName()
                        }
                    }

                    _publicityRecommend.postValue(Event(result.result))
                }
            }
        }
    }

    //region 宣傳頁推薦賽事資料處理
    /**
     * 設置賽事類型參數(滾球、即將、今日、早盤)
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
        matchInfo?.startDateDisplay = TimeUtil.timeFormat(matchInfo?.startTime, "dd/MM")

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
    //endregion
    //endregion
}