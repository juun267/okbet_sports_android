package org.cxct.sportlottery.ui.game

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.OddSpreadForSCO
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.common.GameType.Companion.getGameTypeMenuIcon
import org.cxct.sportlottery.network.common.GameType.Companion.getSpecificLanguageString
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.common.QuickPlayCate
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
import org.cxct.sportlottery.network.odds.list.*
import org.cxct.sportlottery.network.odds.quick.QuickListData
import org.cxct.sportlottery.network.odds.quick.QuickListRequest
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListRequest
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListResult
import org.cxct.sportlottery.network.outright.odds.OutrightShowMoreItem
import org.cxct.sportlottery.network.outright.odds.OutrightSubTitleItem
import org.cxct.sportlottery.network.outright.season.OutrightLeagueListRequest
import org.cxct.sportlottery.network.outright.season.OutrightLeagueListResult
import org.cxct.sportlottery.network.service.league_change.LeagueChangeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.*
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.network.sport.coupon.SportCouponMenuResult
import org.cxct.sportlottery.network.sport.publicityRecommend.PublicityRecommendRequest
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.network.sport.query.*
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.network.third_game.ThirdLoginResult
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.network.today.MatchCategoryQueryRequest
import org.cxct.sportlottery.network.today.MatchCategoryQueryResult
import org.cxct.sportlottery.network.user.odds.OddsChangeOptionRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.ui.game.data.Date
import org.cxct.sportlottery.ui.game.data.SpecialEntrance
import org.cxct.sportlottery.ui.game.publicity.PublicityMenuData
import org.cxct.sportlottery.ui.game.publicity.PublicityPromotionItemData
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.odds.OddsDetailListData
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.AppVersionState
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.px
import org.cxct.sportlottery.util.DisplayUtil.pxToDp
import org.cxct.sportlottery.util.MatchOddUtil.applyDiscount
import org.cxct.sportlottery.util.MatchOddUtil.applyHKDiscount
import org.cxct.sportlottery.util.MatchOddUtil.updateDiscount
import org.cxct.sportlottery.util.MatchOddUtil.updateOddsDiscount
import org.cxct.sportlottery.util.TimeUtil.DMY_FORMAT
import org.cxct.sportlottery.util.TimeUtil.HM_FORMAT
import org.cxct.sportlottery.util.TimeUtil.getTodayTimeRangeParams
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class GameViewModel(
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


    private val gameLiveSharedPreferences by lazy {
        androidContext.getSharedPreferences(
            GameLiveSP,
            Context.MODE_PRIVATE
        )
    }

    val parlayList: LiveData<MutableList<ParlayOdd>>
        get() = betInfoRepository.parlayList

    val gameCateDataList by lazy { ThirdGameRepository.gameCateDataList }

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

    val outrightMatchList: LiveData<Event<List<Any>>>
        get() = _outrightMatchList

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
    var tempDatePosition: Int = 0 //早盤的日期選擇切頁後要記憶的問題，切換球種要清除記憶

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
    private val _oddsListIncrementResult = MutableLiveData<Event<OddsListIncrementResult?>>()
    private val _leagueListResult = MutableLiveData<Event<LeagueListResult?>>()
    private val _outrightLeagueListResult = MutableLiveData<Event<OutrightLeagueListResult?>>()
    private val _outrightOddsListResult = MutableLiveData<Event<OutrightOddsListResult?>>()
    private val _outrightMatchList = MutableLiveData<Event<List<Any>>>()
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
    private val _playList = PlayRepository.mPlayList
    private val _playCate = MutableLiveData<Event<String?>>()
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

    private val _oddsDetailResult = MutableLiveData<Event<OddsDetailResult?>?>()
    val oddsDetailResult: LiveData<Event<OddsDetailResult?>?>
        get() = _oddsDetailResult

    private val _oddsDetailList = MutableLiveData<Event<ArrayList<OddsDetailListData>>>()
    val oddsDetailList: LiveData<Event<ArrayList<OddsDetailListData>>>
        get() = _oddsDetailList

    private val _checkInListFromSocket = MutableLiveData<LeagueChangeEvent?>()
    val checkInListFromSocket: LiveData<LeagueChangeEvent?>
        get() = _checkInListFromSocket

    //賽事直播網址
    private val _matchLiveInfo = MutableLiveData<Event<LiveStreamInfo>?>()
    val matchLiveInfo: LiveData<Event<LiveStreamInfo>?>
        get() = _matchLiveInfo

    //賽事動畫網址
    /*private val _matchTrackerUrl = MutableLiveData<Event<MatchTrackerUrl?>>()
    val matchTrackerUrl: LiveData<Event<MatchTrackerUrl?>>
        get() = _matchTrackerUrl*/

    private val _matchTrackerUrl = MutableLiveData<Event<String?>>()
    val matchTrackerUrl: LiveData<Event<String?>>
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

    private val _publicityRecommend = MutableLiveData<Event<List<Recommend>>>()
    val publicityRecommend: LiveData<Event<List<Recommend>>>
        get() = _publicityRecommend

    private val _enterThirdGameResult = MutableLiveData<EnterThirdGameResult>()
    val enterThirdGameResult: LiveData<EnterThirdGameResult>
        get() = _enterThirdGameResult

    //優惠活動文字跑馬燈
    private val _publicityPromotionAnnouncementList = MutableLiveData<List<String>>()
    val publicityPromotionAnnouncementList: LiveData<List<String>>
        get() = _publicityPromotionAnnouncementList

    //優惠活動圖文公告
    private val _publicityPromotionList = MutableLiveData<List<PublicityPromotionItemData>>()
    val publicityPromotionList: LiveData<List<PublicityPromotionItemData>>
        get() = _publicityPromotionList

    //新版宣傳頁菜單資料
    private val _publicityMenuData = MutableLiveData<PublicityMenuData>()
    val publicityMenuData: LiveData<PublicityMenuData>
        get() = _publicityMenuData

    var sportQueryData: SportQueryData? = null
    var specialMenuData: SportQueryData? = null
    var allSearchData: List<SearchResponse.Row>? = null

    private var sportMenuData: SportMenuData? = null //球種菜單資料

    private var outrightMatchDiscount = userInfo.value?.discount ?: 1.0F //當前冠軍頁面適配的折扣率


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
        _curChildMatchType.value = null
        _specialEntrance.postValue(SpecialEntrance(matchType, gameType, couponCode, couponName))
        gameType?.let { recordSportType(matchType, it.key) }
    }

    fun navSpecialEntrance(
        entranceMatchType: MatchType,
        gameType: GameType?,
        matchId: String,
        gameMatchType: MatchType? = null
    ) {
        _specialEntrance.postValue(
            SpecialEntrance(
                entranceMatchType = entranceMatchType,
                gameType,
                matchID = matchId,
                gameMatchType = gameMatchType
            )
        )
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

    fun switchChildMatchType(childMatchType: MatchType? = null) {
        /* 選取 tab 賽事、冠軍、特優 */
        _curChildMatchType.value = childMatchType
        _oddsListGameHallResult.value = Event(null)
        _oddsListResult.value = Event(null)
        if (childMatchType == MatchType.OTHER_OUTRIGHT) {
            getOutrightOddsList(getSportSelectedCode(MatchType.OTHER_OUTRIGHT) ?: "")
        }
        if (childMatchType == MatchType.OUTRIGHT) {
            getOutrightOddsList(getSportSelectedCode(_curMatchType.value!!) ?: "")
        } else if (childMatchType == MatchType.OTHER) {
            getGameHallList(
                matchType = MatchType.OTHER,
                isReloadDate = true,
                isReloadPlayCate = true,
                isLastSportType = true
            )
        } else {
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

    fun getSportListAtHomePage(matchType: MatchType?) {
//        viewModelScope.launch {
//            val result = doNetwork(androidContext) {
//                OneBoSportApi.sportService.getSportList()
//            }
//            result?.let { sportList ->
//                val sportCardList = sportList.rows.sortedBy { it.sortNum }
//                    .mapNotNull { row ->
//                        GameType.getGameType(row.code)
//                            ?.let { gameType ->
//                                SportMenu(
//                                    gameType,
//                                    row.name,
//                                    getSpecificLanguageString(
//                                        androidContext,
//                                        gameType.key,
//                                        LanguageManager.Language.EN.key
//                                    ),
//                                    getGameTypeMenuIcon(gameType)
//                                )
//                            }
//                    }
//                _sportSortList.postValue(Event(sportCardList))
//                if (_sportMenuResult.value == null) {
//                    switchMatchType(matchType)
//                }
//            }
//        }
    }

    fun firstSwitchMatch(matchType: MatchType?) {
        if (_sportMenuResult.value == null) {
            switchMatchType(matchType)
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
            }
        }
    }

    fun fetchDataFromDataSourceChange(matchType: MatchType) {
        switchMatchType(matchType)
    }

    var currentSpecialCode = ""

    fun resetOtherSeelectedGameType() {
        specialMenuData = null
    }

    fun getAllPlayCategoryBySpecialMatchType(
        code: String = _specialEntrance.value?.couponCode ?: currentSpecialCode,
        item: Item? = null,
        isReload: Boolean = false
    ) {
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

        sportMenuRepository.sportSortList.value?.let { list ->
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

                        getSportCount(MatchType.CS, gameType, sportMenuResult) != 0 -> {
                            MatchType.CS
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
                if (result.success) {
                    result.t?.odds?.forEach { oddData ->
                        oddData.sortOddsMap()
                    }
                    _highlightMenuResult.postValue(Event(result))
                }
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

    //自動選取第一個有賽事的球種
    fun switchFirstSportType(matchType: MatchType) {
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

            MatchType.CS -> {
                val gameTypeCode =
                    if (sportMenuResult.value?.sportMenuData?.menu?.cs?.items.isNullOrEmpty()) GameType.FT.key else sportMenuResult.value?.sportMenuData?.menu?.cs?.items?.first()?.code.toString()
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

    fun updateOddsChangeOption(option: Int) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.userService.oddsChangeOption(
                    OddsChangeOptionRequest(option)
                )
            }?.let { result ->
                userInfoRepository.updateOddsChangeOption(option)
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
        updatePlaySelectedState(matchType, play)
    }

    fun switchPlayCategory(
        play: Play,
        playCateCode: String?,
        hasItemSelect: Boolean,
        matchType: MatchType
    ) {
        _playList.value?.peekContent()?.forEach {
            it.isSelected = (it == play)
        }
        _playCate.value = Event(playCateCode)
        if (!hasItemSelect) {
            getGameHallList(
                matchType = matchType,
                isReloadDate = false,
                isReloadPlayCate = false,
                isLastSportType = true
            )
        }
    }

    fun switchMatchDate(matchType: MatchType, date: Date) {
        _curDate.value?.updateDateSelectedState(date)
        getGameHallList(matchType, false, date.date)
    }

    fun checkGameInList(matchType: MatchType, leagueChangeEvent: LeagueChangeEvent?) {
        val nowMatchType = curMatchType.value ?: matchType
        val nowChildMatchType = curChildMatchType.value ?: matchType
        val sportCode = getSportSelectedCode(nowMatchType)
        sportCode?.let { code ->
            when (nowChildMatchType) {
                MatchType.IN_PLAY -> {
                    checkOddsList(
                        code,
                        nowChildMatchType.postValue,
                        leagueChangeEvent = leagueChangeEvent,
                    )
                }

                MatchType.AT_START -> {
                    checkOddsList(
                        code,
                        nowChildMatchType.postValue,
                        leagueChangeEvent = leagueChangeEvent,
                    )
                }

                MatchType.CS -> {
                    checkOddsList(
                        code,
                        nowChildMatchType.postValue,
                        leagueChangeEvent = leagueChangeEvent,
                    )
                }

                MatchType.OTHER -> {
                    checkOddsList(
                        code,
                        specialEntrance.value?.couponCode ?: "",
                        leagueChangeEvent = leagueChangeEvent,
                    )
                }

                else -> {
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

        if (getMatchCount(matchType) < 1) {
            return
        }

        val nowMatchType = curMatchType.value ?: matchType
        val nowChildMatchType = curChildMatchType.value ?: matchType

        if (isReloadPlayCate) {
            getPlayCategory(nowChildMatchType)
        }

        var reloadedDateRow: List<Date>? = null

        if (isReloadDate) {
            reloadedDateRow = getDateRow(nowChildMatchType)
        }

        //20220422 若重新讀取日期列(isReloadDate == true)時，會因postValue 比getCurrentTimeRangeParams取當前日期慢導致取到錯誤的時間
        val reloadedTimeRange = reloadedDateRow?.find { it.isSelected }?.timeRangeParams

        val sportCode = getSportSelectedCode(nowMatchType)

        val mt = if (matchType == nowChildMatchType) matchType else nowChildMatchType

        sportCode?.let { code ->
            when (mt) {
                MatchType.MAIN -> {
                    getOddsList(
                        code,
                        specialEntrance.value?.couponCode ?: "",
                        reloadedTimeRange ?: getCurrentTimeRangeParams(),
                        leagueIdList = leagueIdList,
                        isIncrement = isIncrement
                    )
                }

                MatchType.IN_PLAY -> {
                    getOddsList(
                        code,
                        matchType.postValue,
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
                    getLeagueList(
                        gameType = code,
                        matchType = nowChildMatchType.postValue,
                        startTime = reloadedTimeRange?.startTime
                            ?: getCurrentTimeRangeParams()?.startTime ?: "",
                        endTime = reloadedTimeRange?.endTime
                            ?: getCurrentTimeRangeParams()?.endTime,
                        isIncrement = isIncrement
                    )
                }

                MatchType.CS -> {
                    getOddsList(
                        code,
                        matchType.postValue,
                        getCurrentTimeRangeParams(),
                    )
                }

                MatchType.PARLAY -> {
                    getLeagueList(
                        code,
                        nowChildMatchType.postValue,
                        reloadedTimeRange ?: getCurrentTimeRangeParams(),
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
                        matchType.postValue,
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
                        timeRangeParams = reloadedTimeRange ?: getCurrentTimeRangeParams(),
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
                        timeRangeParams = reloadedTimeRange ?: getCurrentTimeRangeParams(),
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

    //用於GameLeagueFragment
    fun switchPlay(
        matchType: MatchType,
        leagueIdList: List<String>,
        matchIdList: List<String>,
        play: Play
    ) {
        updatePlaySelectedState(matchType, play)
        getLeagueOddsList(matchType, leagueIdList, matchIdList)
    }

    //用於GameLeagueFragment
    fun switchPlayCategory(
        matchType: MatchType,
        leagueIdList: List<String>,
        matchIdList: List<String>,
        play: Play,
        playCateCode: String?,
        hasItemSelect: Boolean
    ) {
        _playList.value?.peekContent()?.forEach {
            it.isSelected = (it == play)
        }
        _playCate.value = Event(playCateCode)

        if (!hasItemSelect) getLeagueOddsList(matchType, leagueIdList, matchIdList)
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

        val nowMatchType = curChildMatchType.value ?: matchType

        if (isReloadPlayCate && !isIncrement) {
            getLeaguePlayCategory(matchType, leagueIdList)
        }

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

    fun getOutrightOddsList(gameType: String, outrightLeagueId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = doNetwork(androidContext) {
                OneBoSportApi.outrightService.getOutrightOddsList(
                    if (outrightLeagueId.isNullOrEmpty()) {
                        OutrightOddsListRequest(
                            gameType,
                            matchType = MatchType.OUTRIGHT.postValue
                        )
                    } else {
                        OutrightOddsListRequest(
                            gameType,
                            matchType = MatchType.OUTRIGHT.postValue,
                            leagueIdList = listOf(outrightLeagueId)
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
                    matchOdd.let { matchOddNotNull ->
                        //聯賽標題
                        oddsList.add(matchOddNotNull)

                        matchOddNotNull.oddsMap?.forEach { oddMap ->
                            val playCateExpand =
                                matchOddNotNull.oddsExpand?.get(oddMap.key) ?: false

                            //region 玩法標題
                            oddsList.add(
                                OutrightSubTitleItem(
                                    belongMatchOdd = matchOddNotNull,
                                    playCateCode = oddMap.key,
                                    subTitle = matchOddNotNull.dynamicMarkets[oddMap.key]?.let {
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
                                    } ?: "",
                                    leagueExpanded = matchOddNotNull.isExpanded))
                            //endregion

                            //region 玩法賠率項
                            oddsList.addAll(
                                oddMap.value?.filterNotNull()
                                    ?.mapIndexed { index, odd ->
                                        odd.outrightCateKey = oddMap.key
                                        odd.playCateExpand = playCateExpand
                                        odd.leagueExpanded = matchOddNotNull.isExpanded
                                        odd.belongMatchOdd = matchOddNotNull
                                        if (index < 5) odd.isExpand = true
                                        odd
                                    } ?: listOf()
                            )
                            //endregion

                            //region 顯示更多選項(大於五項才需要此功能)
                            if (oddMap.value?.filterNotNull()?.size ?: 0 > 5) {
                                //Triple(玩法key, MatchOdd, 該玩法是否需要展開)
                                oddsList.add(
                                    OutrightShowMoreItem(
                                        oddMap.key,
                                        matchOddNotNull,
                                        playCateExpand,
                                        isExpanded = false,
                                        leagueExpanded = matchOddNotNull.isExpanded
                                    )
                                )
                            }
                            //endregion
                        }
//                        matchOddNotNull.outrightOddsList = oddsList
                        outrightMatchList.add(matchOddNotNull)
                    }
                    //endregion
                }
            }

            withContext(Dispatchers.Main) {
//                _outrightMatchList.value = Event(outrightMatchList)
                _outrightMatchList.value = Event(oddsList)
            }
        }
    }

    fun updateOutrightDiscount(newDiscount: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            outrightMatchList.value?.peekContent()?.let { outrightList ->
                outrightList.filterIsInstance<Odd>().forEach { odd ->
                    odd.updateDiscount(outrightMatchDiscount, newDiscount)
                }

                withContext(Dispatchers.Main) {
                    _outrightMatchList.value = Event(outrightList)
                }

                outrightMatchDiscount = newDiscount
            }
        }
    }

    fun updateOutrightOddsChange(context: Context?, oddsChangeEvent: OddsChangeEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            outrightMatchList.value?.peekContent()?.let { outrightList ->
                outrightList.filterIsInstance<MatchOdd>().forEach { matchOdd ->
                    SocketUpdateUtil.updateMatchOdds(
                        context, matchOdd, oddsChangeEvent
                    )
                }

                withContext(Dispatchers.Main) {
                    _outrightMatchList.value = Event(outrightList)
                }
            }
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
        var playCateMenuCode = getPlayCateSelected()?.code ?: MenuCode.MAIN.code
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

                    if (!getPlayCateCodeList().isNullOrEmpty())
                        matchOdd.oddsMap?.entries?.retainAll { getPlayCateCodeList()?.contains(it.key) == true }

                    matchOdd.setupOddDiscount()
                    matchOdd.updateOddStatus()
                    matchOdd.filterQuickPlayCate(matchType)
                }
            }
            result?.oddsListData.getPlayCateNameMap(matchType)

            when (matchType) {
                MatchType.IN_PLAY.postValue, MatchType.AT_START.postValue -> {
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

                    _playList.postValue(Event(playList))
                    _playCate.postValue(Event(null))
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

                    _playList.postValue(Event(playList))
                    _playCate.postValue(Event(null))
                }
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
                            value.rowSort
                        )

                        //球員玩法邏輯
                        if (PlayCate.getPlayCate(key) == PlayCate.SCO) {
                            oddsDetail.setSCOTeamNameList(
                                filteredOddList,
                                result.oddsDetailData.matchOdd.matchInfo.homeName
                            )
                            oddsDetail.homeMap = setItemMap(
                                filteredOddList,
                                result.oddsDetailData.matchOdd.matchInfo.homeName
                            )
                            oddsDetail.awayMap = setItemMap(
                                filteredOddList,
                                result.oddsDetailData.matchOdd.matchInfo.awayName
                            )
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

                    //賽事動畫網址
                    val eventId = result.oddsDetailData?.matchOdd?.matchInfo?.trackerId
                    val screenWidth = MetricsUtil.getScreenWidth()
                    val animationHeight = (LiveUtil.getAnimationHeightFromWidth(screenWidth)).px
                    val languageParams =
                        LanguageManager.getLanguageString(MultiLanguagesApplication.appContext)

                    val trackerUrl =
                        "${sConfigData?.sportAnimation}/animation/?eventId=${eventId}&width=${screenWidth.pxToDp}&height=${animationHeight}&lang=${languageParams}&mode=widget"
                    //測試用eventId=4385309, 4477265
//                    val trackerUrl = "${Constants.getBaseUrl()}animation/?eventId=4477265&width=${screenWidth.px}&height=${animationHeight}&lang=${languageParams}&mode=widget"

                    _matchTrackerUrl.postValue(Event(trackerUrl))
                    notifyFavorite(FavoriteType.PLAY_CATE)
                }
            }
        }
    }

    private fun OddsDetailListData.setSCOTeamNameList(
        oddList: MutableList<Odd?>,
        homeName: String?
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
        teamName: String?
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

    fun getSportSelectedCode(matchType: MatchType? = curMatchType.value): String? =
        when (matchType) {
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


    private fun getPlayCateSelected(): Play? =
        _playList.value?.peekContent()?.find { it.isSelected }

    private fun getPlayCateCodeList(): List<String>? {
        _playCate.value?.peekContent()?.let {
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

    //20220507 原本除了matchType之外的玩法都會被調整至第一個球類, 調整為僅對matchType做選中球種更新
    private fun SportMenuData.updateSportSelectState(
        matchType: MatchType?,
        gameTypeCode: String?
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

            //早盤
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
        gameTypeCode: String?
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

    private fun updatePlaySelectedState(matchType: MatchType, play: Play) {
        val playList = _playList.value?.peekContent()

        playList?.forEach {
            it.isSelected = (it == play)
        }

        playList?.let {
            _playList.value = Event(it)
            _playCate.value = Event((
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
            )
        }

        getGameHallList(
            matchType = matchType,
            isReloadDate = false,
            isReloadPlayCate = false,
            isLastSportType = true
        )
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

    fun resetErrorDialogMsg() {
        _showErrorDialogMsg.value = ""
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
                            setupOddsSort()
                            setupMatchType()
                            setupMatchTime()
                            setupPlayCateNum()
                            setupLeagueName()
                            setupSocketMatchStatus()
                        }
                    }
                    _publicityRecommend.postValue(Event(result.result.recommendList))

                    notifyFavorite(FavoriteType.MATCH)
                }
            }
        }
    }

    fun publicityLeagueChange(leagueChangeEvent: LeagueChangeEvent) {
        var needUpdatePublicityRecommend = false
        publicityRecommend.value?.peekContent()?.forEach { recommend ->
            if (leagueChangeEvent.leagueIdList?.contains(recommend.leagueId) == true) {
                needUpdatePublicityRecommend = true
            }

            if (leagueChangeEvent.matchIdList?.contains(recommend.matchInfo?.id) == true) {
                needUpdatePublicityRecommend = true
            }
        }

        if (needUpdatePublicityRecommend) {
            getRecommend()
        }
    }

    /**
     * 更新宣傳頁賠率折扣
     */
    fun publicityUpdateDiscount(oldDiscount: Float, newDiscount: Float) {
        if (oldDiscount == newDiscount) return
        viewModelScope.launch(Dispatchers.IO) {
            publicityRecommend.value?.peekContent()?.let { recommendList ->
                val iterator = recommendList.iterator()
                while (iterator.hasNext()) {
                    val recommend = iterator.next()
                    recommend.oddsMap?.updateOddsDiscount(oldDiscount, newDiscount)
                }

                withContext(Dispatchers.Main) {
                    _publicityRecommend.value = Event(recommendList)
                }
            }
        }
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
        val playCateMenuCode = menuList?.firstOrNull()?.code
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
    //endregion

    //region 宣傳頁 優惠活動文字跑馬燈、圖片公告
    fun getPublicityPromotion() {

        sConfigData?.imageList?.filter { it.imageType == ImageType.PROMOTION.code }
            ?.let { promotionList ->
                //優惠活動文字跑馬燈
                promotionList.filter { it.viewType == 1 }.mapNotNull { it.imageText1 }.let {
                    _publicityPromotionAnnouncementList.postValue(it)
                }

                //優惠活動圖片公告清單
                _publicityPromotionList.postValue(promotionList.map {
                    PublicityPromotionItemData.createData(it)
                })
            }
    }
    //endregion

    //region 新版宣傳頁Menu

    fun getPublicitySportMenu() {
        viewModelScope.launch(Dispatchers.IO) {
            getSportListAtPublicityPage()
            postPublicitySportMenu()
        }
    }

    private suspend fun getSportListAtPublicityPage() {
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
                                    getSpecificLanguageString(
                                        androidContext,
                                        gameType.key,
                                        LanguageManager.Language.EN.key
                                    ),
                                    getGameTypeMenuIcon(gameType)
                                )
                            }
                    }
                sportMenuRepository.postSportSortList(sportCardList)
            }
        }
    }

    private suspend fun postPublicitySportMenu() {
        getSportMenuAll()?.let { sportMenuResult ->
            sportMenuRepository.sportSortList.value?.let { list ->
                val sportMenuDataList = mutableListOf<SportMenu>()
                list.forEach { sportMenu ->
                    sportMenu.apply {
                        gameCount =
                            getSportCount(
                                MatchType.IN_PLAY,
                                gameType,
                                sportMenuResult
                            ) + getSportCount(
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
                            getSportCount(MatchType.IN_PLAY, gameType, sportMenuResult) != 0 -> {
                                MatchType.IN_PLAY
                            }

                            getSportCount(MatchType.TODAY, gameType, sportMenuResult) != 0 -> {
                                MatchType.TODAY
                            }

                            getSportCount(MatchType.EARLY, gameType, sportMenuResult) != 0 -> {
                                MatchType.EARLY
                            }

                            getSportCount(MatchType.CS, gameType, sportMenuResult) != 0 -> {
                                MatchType.CS
                            }

                            getSportCount(MatchType.PARLAY, gameType, sportMenuResult) != 0 -> {
                                MatchType.PARLAY
                            }

                            getSportCount(MatchType.OUTRIGHT, gameType, sportMenuResult) != 0 -> {
                                MatchType.OUTRIGHT
                            }

                            else -> null
                        }

                        if (entranceType != null)
                            sportMenuDataList.add(sportMenu)
                    }
                }

                updatePublicityMenuLiveData(sportMenuDataList = sportMenuDataList)
            }
        }
    }

    fun getMenuThirdGame() {
        viewModelScope.launch(Dispatchers.IO) {
            getThirdGameList()?.let { gameCateDataList ->
                //棋牌
                val eGameList =
                    gameCateDataList.firstOrNull { it.categoryThird == ThirdGameCategory.QP }?.tabDataList?.firstOrNull()?.gameList?.firstOrNull()?.thirdGameData
                //真人
                val casinoList =
                    gameCateDataList.firstOrNull { it.categoryThird == ThirdGameCategory.LIVE }?.tabDataList?.firstOrNull()?.gameList?.firstOrNull()?.thirdGameData
                //TODO 鬥雞 當前還沒有接入這個分類
                val sabongList = null

                updatePublicityMenuLiveData(
                    sportMenuDataList = null,
                    eGameMenuDataList = eGameList,
                    casinoMenuDataList = casinoList,
                    sabongMenuDataList = sabongList
                )
            }
        }
    }

    private suspend fun getThirdGameList(): MutableList<GameCateData>? {
        doNetwork(androidContext) {
            ThirdGameRepository.getThirdGameResponse()
        }?.let { result ->
            return if (result.success) {
                ThirdGameRepository.createHomeGameList(result.t)
            } else {
                Timber.e("獲取第三方遊戲配置失敗")
                null
            }
        }
        return null
    }

    fun updateMenuVersionUpdatedStatus(appVersionState: AppVersionState) {
        //appVersionState.isNewVersion代表有無新版本
        updatePublicityMenuLiveData(isNewestVersion = !appVersionState.isNewVersion)
    }

    /**
     * 更新publicityMenuData
     */
    private fun updatePublicityMenuLiveData(
        sportMenuDataList: List<SportMenu>? = null,
        eGameMenuDataList: ThirdDictValues? = null,
        casinoMenuDataList: ThirdDictValues? = null,
        sabongMenuDataList: ThirdDictValues? = null,
        isNewestVersion: Boolean? = null
    ) {

        viewModelScope.launch(Dispatchers.Main) {
            if (publicityMenuData.value == null) {
                _publicityMenuData.value = PublicityMenuData(
                    sportMenuDataList = sportMenuDataList,
                    eGameMenuData = eGameMenuDataList,
                    casinoMenuData = casinoMenuDataList,
                    sabongMenuData = sabongMenuDataList,
                    isNewestVersion = isNewestVersion ?: true
                )
            } else {
                val menuData = publicityMenuData.value
                sportMenuDataList?.let {
                    menuData?.sportMenuDataList = it
                }
                eGameMenuDataList?.let {
                    menuData?.eGameMenuData = it
                }
                casinoMenuDataList?.let {
                    menuData?.casinoMenuData = it
                }
                sabongMenuDataList?.let {
                    menuData?.sabongMenuData = it
                }
                menuData?.isNewestVersion?.let {
                    menuData?.isNewestVersion = it
                }
                _publicityMenuData.value = publicityMenuData.value
            }
        }
    }

    fun getGoGamePageEntrance(): Pair<MatchType, String>? {
        return when {
            (sportMenuData?.menu?.inPlay?.num ?: 0) > 0 -> {
                sportMenuData?.menu?.inPlay?.items?.firstOrNull { it.num > 0 }?.code?.let {
                    Pair(MatchType.IN_PLAY, it)
                }
            }

            (sportMenuData?.menu?.today?.num ?: 0) > 0 -> {
                sportMenuData?.menu?.today?.items?.firstOrNull { it.num > 0 }?.code?.let {
                    Pair(MatchType.TODAY, it)
                }
            }

            (sportMenuData?.menu?.early?.num ?: 0) > 0 -> {
                sportMenuData?.menu?.early?.items?.firstOrNull { it.num > 0 }?.code?.let {
                    Pair(MatchType.EARLY, it)
                }
            }

            (sportMenuData?.menu?.cs?.num ?: 0) > 0 -> {
                sportMenuData?.menu?.early?.items?.firstOrNull { it.num > 0 }?.code?.let {
                    Pair(MatchType.CS, it)
                }
            }

            (sportMenuData?.menu?.parlay?.num ?: 0) > 0 -> {
                sportMenuData?.menu?.parlay?.items?.firstOrNull { it.num > 0 }?.code?.let {
                    Pair(MatchType.PARLAY, it)
                }
            }

            (sportMenuData?.menu?.outright?.num ?: 0) > 0 -> {
                sportMenuData?.menu?.outright?.items?.firstOrNull { it.num > 0 }?.code?.let {
                    Pair(MatchType.OUTRIGHT, it)
                }
            }

            else -> null
        }
    }

    //endregion

    //endregion

    //region 第三方遊戲
    fun getThirdGame() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                ThirdGameRepository.getThirdGame()
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

    fun requestEnterThirdGame(gameData: ThirdDictValues?) {
//        Timber.e("gameData: $gameData")
        when {
            gameData == null -> {
                _enterThirdGameResult.postValue(
                    EnterThirdGameResult(
                        resultType = EnterThirdGameResult.ResultType.FAIL,
                        url = null,
                        errorMsg = androidContext.getString(R.string.error_url_fail)
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

    private suspend fun getOddsList(
        gameType: String,
        matchType: String,
        leagueIdList: List<String>? = null
    ): OddsListResult? {
        return doNetwork(androidContext) {
            OneBoSportApi.oddsService.getOddsList(
                OddsListRequest(
                    gameType,
                    matchType,
                    leagueIdList = leagueIdList,
                    playCateMenuCode = getPlayCateSelected()?.code ?: MenuCode.MAIN.code
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
                            postHomeCardCount(it)              // 更新主頁、左邊選單
                        }
                    }
                }
            }

            /* 主頁 */
            MatchType.MAIN -> {
                _curMatchType.value = MatchType.MAIN
                viewModelScope.launch {
                    getSportCouponMenu()?.let {
                        _sportCouponMenuResult.postValue(Event(it))
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
                            postHomeCardCount(it)              // 更新主頁、左邊選單
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

        // 今日、串關頁面下賽事選擇 (今日、所有)
        if (matchType == MatchType.TODAY || matchType == MatchType.PARLAY) {
            getMatchCategory(matchType)
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
            getSportMenuAll()?.let {
                if (it.success) {
                    postHomeCardCount(it)              // 更新主頁、左邊選單
                } else {
                    return@launch
                }
            }

            //原有邏輯暫時不動
            if (matchType == MatchType.OTHER) {
                getAllPlayCategoryBySpecialMatchType(item = item)
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

            // 今日、串關頁面下賽事選擇 (今日、所有)
            if (matchType == MatchType.TODAY || matchType == MatchType.PARLAY) {
                getMatchCategory(matchType)
            }

            clearGameHallContentBySwitchGameType()

            recordSportType(matchType, item.code)

            getGameHallList(matchType, true, isReloadPlayCate = true)
        }
    }

    private var jobSwitchGameType: Job? = null

    private fun checkOddsList(
        gameType: String,
        matchTypeString: String,
        leagueChangeEvent: LeagueChangeEvent?
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

            // 更新主頁、左邊選單
            postHomeCardCount(sportMenuResult)

            // 今日、串關頁面下賽事選擇 (今日、所有)
            if (matchType == MatchType.TODAY || matchType == MatchType.PARLAY) {
                getMatchCategory(matchType)
            }

            // 如選擇球種的線程正在執行 則不需執行 league change 後的流程
            if (jobSwitchGameType?.isActive == true) return@launch

            /* 2. 確認 league change 聯賽列表有無 */
            val oddsListResult = getOddsList(
                gameType,
                matchTypeString,
                leagueIdList = leagueChangeEvent?.leagueIdList
            )?.apply {
                let {
                    if (!it.success) {
                        return@launch
                    }
                }
            } ?: return@launch

            if (oddsListResult.oddsListData?.leagueOdds.isNullOrEmpty()) return@launch

            _checkInListFromSocket.postValue(leagueChangeEvent)

            //後續尚可優化
//                    //收到的gameType与用户当前页面所选球种相同, 则需额外调用/match/odds/simple/list & /match/odds/eps/list
//                    val nowGameType =
//                        GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)?.key
//
//                    if(leagueAdapter.data.isNotEmpty()) {
//                        val hasLeagueIdList =
//                            leagueAdapter.data.any { leagueOdd -> leagueOdd.league.id == mLeagueChangeEvent?.leagueIdList?.firstOrNull() }
//
//                        if (nowGameType == mLeagueChangeEvent?.gameType) {
//                            when {
//                                !hasLeagueIdList || args.matchType == MatchType.AT_START -> {
//                                    //全刷
//                                    unSubscribeChannelHallAll()
//                                    withContext(Dispatchers.Main) {
//                                        if (args.matchType == MatchType.OTHER) {
//                                            viewModel.getAllPlayCategoryBySpecialMatchType(isReload = false)
//                                        } else {
//                                            viewModel.getGameHallList(args.matchType, false)
//                                        }
//                                    }
//                                }
//                                else -> {
//                                    unSubscribeChannelHall(nowGameType ?: GameType.FT.key, mLeagueChangeEvent?.matchIdList?.firstOrNull())
//                                    subscribeChannelHall(nowGameType ?: GameType.FT.key, mLeagueChangeEvent?.matchIdList?.firstOrNull())
//                                    if (args.matchType == MatchType.OTHER) {
//                                        viewModel.getAllPlayCategoryBySpecialMatchType(isReload = false)
//                                    }
//                                }
//                            }
//                        } else if (args.matchType == MatchType.OTHER) {
//                            viewModel.getAllPlayCategoryBySpecialMatchType(isReload = false)
//                        }
//                    }
//                    isUpdatingLeague = false
//                }
//            }
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

}