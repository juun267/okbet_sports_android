package org.cxct.sportlottery.ui.sport

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.OddSpreadForSCO
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.manager.RequestManager
import org.cxct.sportlottery.network.match.MatchRound
import org.cxct.sportlottery.network.match.MatchService
import org.cxct.sportlottery.network.matchLiveInfo.ChatLiveLoginData
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.detail.OddsDetailRequest
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.odds.list.*
import org.cxct.sportlottery.network.sport.*
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.network.sport.query.*
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.network.user.info.LiveSyncUserInfoVO
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.ui.odds.OddsDetailListData
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.px
import org.cxct.sportlottery.util.DisplayUtil.pxToDp
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

    val token = loginRepository.token


    val messageListResult: LiveData<Event<MessageListResult?>>
        get() = _messageListResult

    val sportMenuResult: LiveData<SportMenuResult?>
        get() = _sportMenuResult

    val playList: LiveData<Event<List<Play>>>
        get() = PlayRepository.playList

    val playCate: LiveData<Event<String?>>
        get() = _playCate

    val searchResult: LiveData<Event<List<SearchResult>?>>
        get() = _searchResult

    val showBetUpperLimit = betInfoRepository.showBetUpperLimit

    private val _messageListResult = MutableLiveData<Event<MessageListResult?>>()
    private val _sportMenuResult = MutableLiveData<SportMenuResult?>()

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

    //優惠活動文字跑馬燈
    private val _liveLoginInfo = MutableLiveData<Event<ChatLiveLoginData>>()
    val liveLoginInfo: LiveData<Event<ChatLiveLoginData>>
        get() = _liveLoginInfo

    var allSearchData: List<SearchResponse.Row>? = null

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

    fun loginLive() {
        if (sConfigData?.liveChatOpen == 0) return
        var spf = MultiLanguagesApplication.mInstance.getSharedPreferences(NAME_LOGIN,
            Context.MODE_PRIVATE)
        var spValue = spf.getString(KEY_LIVE_USER_INFO, "")
        if (spValue.isNullOrEmpty()) return
        var liveSyncUserInfoVO: LiveSyncUserInfoVO = JsonUtil.fromJson(spValue, LiveSyncUserInfoVO::class.java) ?: return
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