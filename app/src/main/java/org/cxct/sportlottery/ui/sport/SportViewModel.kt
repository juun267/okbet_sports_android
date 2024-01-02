package org.cxct.sportlottery.ui.sport

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddSpreadForSCO
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.common.extentions.toStringS
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.match.MatchRound
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.detail.OddsDetailRequest
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.odds.list.*
import org.cxct.sportlottery.network.sport.*
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.network.sport.query.*
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.ui.sport.detail.OddsDetailListData
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.px
import org.cxct.sportlottery.util.DisplayUtil.pxToDp
import timber.log.Timber
import java.util.*

class SportViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    myFavoriteRepository: MyFavoriteRepository,
) : BaseBottomNavViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    myFavoriteRepository,
) {

    val token = loginRepository.token

    val playCate: LiveData<Event<String?>>
        get() = _playCate

    val searchResult: LiveData<Event<Pair<String, List<SearchResult>?>>>
        get() = _searchResult

    val showBetUpperLimit = betInfoRepository.showBetUpperLimit
    val showBetBasketballUpperLimit = betInfoRepository.showBetBasketballUpperLimit

    private val _playCate = MutableLiveData<Event<String?>>()
    private val _searchResult = MutableLiveData<Event<Pair<String, List<SearchResult>?>>>()

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

    var allSearchData: List<SearchResponse.Row>? = null

    private final val defaultVale = "0"
    private var home1st = defaultVale
    private var away1st = defaultVale
    private var home2nd = defaultVale
    private var away2nd = defaultVale
    private var home3rd = defaultVale
    private var away3rd = defaultVale
    private var home4th = defaultVale
    private var away4th = defaultVale
    private var home5th = defaultVale
    private var away5th = defaultVale
    private var home6th = defaultVale
    private var away6th = defaultVale
    private var home7th = defaultVale
    private var away7th = defaultVale
    private var home8th = defaultVale
    private var away8th = defaultVale

    //用于网球
    private var home9th = defaultVale
    private var away9th = defaultVale

    val chartViewList = SingleLiveEvent<MutableList<String>>()

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
            } ?: return@launch

            launch(Dispatchers.IO) {
                result.rows.updateMatchType()
                allSearchData = result.rows
            }
        }
    }

    fun getSportSearch(key: String,gameType: String?) = viewModelScope.launch(Dispatchers.IO) {
        //[Martin] 小弟愚鈍 搜尋無法一次Filter所有資料(待強人捕)
        // 所以下面的做法總共分三次去Filter資料 然後再合併
        // 1.篩選球種 2.篩選聯賽 3.篩選比賽
        var finalResult: MutableList<SearchResult> = arrayListOf()
        //1.篩選球種
        var searchResult = allSearchData?.filter { row ->
            ( gameType.isNullOrBlank()||gameType==row.gameType)&&row.leagueMatchList.any { leagueMatch ->
                leagueMatch.matchInfoList.any { matchInfo ->
                    leagueMatch.leagueName.contains(key, true) || matchInfo.homeName.contains(
                        key, true
                    ) || matchInfo.awayName.contains(key, true)
                }
            }
        }

        searchResult?.forEach {
            var searchResult = SearchResult(it.gameName)
            searchResult.sportTitle = it.gameName
            searchResult.gameType = it.gameType
            finalResult?.add(searchResult)
        }

        //2.篩選聯賽
        var leagueMatchSearchResult = searchResult?.map { row ->
            row.leagueMatchList.filter { leagueMatch ->
                leagueMatch.matchInfoList.any { matchInfo ->
                    leagueMatch.leagueName.contains(key, true) || matchInfo.homeName.contains(
                        key, true
                    ) || matchInfo.awayName.contains(key, true)
                }
            }
        }

        leagueMatchSearchResult?.forEachIndexed { index, league ->
            var searchResultLeagueList: MutableList<SearchResult.SearchResultLeague> = arrayListOf()
            league.forEach { leagueMatch ->
                var searchResultLeague =
                    SearchResult.SearchResultLeague(leagueMatch.leagueName, leagueMatch.icon)
                searchResultLeagueList.add(searchResultLeague)
            }
            finalResult?.get(index).searchResultLeague = searchResultLeagueList
        }

        //3.篩選比賽
        var matchSearchResult = leagueMatchSearchResult?.map { row ->
            row.map { leagueMatch ->
                leagueMatch.matchInfoList.filter { matchInfo ->
                    leagueMatch.leagueName.contains(key, true) || matchInfo.homeName.contains(
                        key, true
                    ) || matchInfo.awayName.contains(key, true)
                }
            }
        }

        matchSearchResult?.forEachIndexed { index0, row ->
            row.forEachIndexed { index1, league ->

                val searchResult = finalResult.getOrNull(index0)
                if (searchResult != null) {
                    val matchList: MutableList<SearchResponse.Row.LeagueMatch.MatchInfo> =
                        arrayListOf()
                    league.forEachIndexed { _, matchInfo ->
                        matchInfo.gameType = searchResult.gameType
                        matchList.add(matchInfo)
                    }
                    searchResult.searchResultLeague.get(index1).leagueMatchList = matchList
                }
            }
        }

        _searchResult.postValue(Event(Pair(key, finalResult)))
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
            result?.oddsDetailData?.matchOdd?.matchInfo?.apply {
                leagueName = result?.oddsDetailData?.league?.name
                categoryCode = result.oddsDetailData.league.categoryCode
                isFavorite = favorMatchList.value?.contains(matchId)==true
            }
            _oddsDetailResult.postValue(Event(result))
            result?.success?.let { success ->
                val list: ArrayList<OddsDetailListData> = ArrayList()
                if (success) {
                    result.oddsDetailData?.matchOdd?.sortOddsMap()
                    result.oddsDetailData?.matchOdd?.setupIsOnlyEUType()
                    result.oddsDetailData?.matchOdd?.odds?.forEach { (key, value) ->
                        betInfoRepository.betInfoList.value?.peekContent()?.let { list ->
                            value.odds.forEach { odd ->
                                odd?.isSelected = list.any {
                                    it.matchOdd.oddsId == odd?.id
                                }
                            }
                        }
                        val filteredOddList = mutableListOf<Odd?>()
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
                        if (key == PlayCate.SCO.value) {
                            oddsDetail.setSCOTeamNameList(
                                filteredOddList, result.oddsDetailData.matchOdd.matchInfo.homeName
                            )
                            oddsDetail.homeMap = setItemMap(
                                filteredOddList, result.oddsDetailData.matchOdd.matchInfo.homeName
                            )
                            oddsDetail.awayMap = setItemMap(
                                filteredOddList, result.oddsDetailData.matchOdd.matchInfo.awayName
                            )
                            oddsDetail.scoItem = oddsDetail.homeMap // default
                        }

                        list.add(oddsDetail)
                    }

                    result.oddsDetailData?.matchOdd?.odds?.sortPlayCate()
                    result.oddsDetailData?.matchOdd?.setupOddDiscount()
                    result.oddsDetailData?.matchOdd?.updateOddStatus()

                    //因UI需求 特優賠率移到第一項 需求先隱藏特優賠率
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
                        val languageParams = LanguageManager.getLanguageString()

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
            odd?.playCode == OddSpreadForSCO.SCORE_1ST_O || odd?.playCode == OddSpreadForSCO.SCORE_ANT_O || odd?.playCode == OddSpreadForSCO.SCORE_LAST_O || odd?.playCode == OddSpreadForSCO.SCORE_N
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
            odd?.playCode == OddSpreadForSCO.SCORE_1ST_O || odd?.playCode == OddSpreadForSCO.SCORE_ANT_O || odd?.playCode == OddSpreadForSCO.SCORE_LAST_O || odd?.playCode == OddSpreadForSCO.SCORE_N
        }.groupBy {
            it?.name
        }.entries.sortedByDescending {
            it.value.size
        }.associateBy({ it.key }, { it.value }).forEach {
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


    /**
     * 排球
     * 羽毛球
     * 乒乓球
     * 冰球
     * 台球
     */
    fun assembleData3(matchInfo: MatchInfo) {
        Timber.d("assembleData3 matchStatusList.isEmpty:${matchInfo.matchStatusList?.isEmpty()}")
        matchInfo.matchStatusList?.let {
            if (it.isEmpty()) return@let
            home1st = it[0].homeScore.toString()
            away1st = it[0].awayScore.toString()
            if (it.size > 1) {
                home2nd = it[1].homeScore.toString()
                away2nd = it[1].awayScore.toString()
            }
            if (it.size > 2) {
                home3rd = it[2].homeScore.toString()
                away3rd = it[2].awayScore.toString()
            }
            if (it.size > 3) {
                home4th = it[3].homeScore.toString()
                away4th = it[3].awayScore.toString()
            }
            if (it.size > 4) {
                home5th = it[4].homeScore.toString()
                away5th = it[4].awayScore.toString()
            }
            if (it.size > 5) {
                home7th = it[5].homeScore.toString()
                away7th = it[5].awayScore.toString()
            }
            if (it.size > 6) {
                home8th = it[6].homeScore.toString()
                away8th = it[6].awayScore.toString()
            }
            home6th = matchInfo.homeTotalScore.toString()
            away6th = matchInfo.awayTotalScore.toString()
        }
//        }
        extracted3(matchInfo)

    }


    /**
     *篮球
     *美式足球
     *冰球
     */
    fun assembleData2(matchInfo: MatchInfo) {
        matchInfo.matchStatusList?.let {
            if (it.isEmpty()) return@let
            home1st = it[0].homeScore.toString()
            away1st = it[0].awayScore.toString()
            if (it.size > 1) {
                home2nd = it[1].homeScore.toString()
                away2nd = it[1].awayScore.toString()
            }
            home3rd = (home1st.toIntS() + home2nd.toIntS()).toString()
            away3rd = (away1st.toIntS() + away2nd.toIntS()).toString()

            if (it.size > 2) {
                home4th = it[2].homeScore.toString()
                away4th = it[2].awayScore.toString()
            }
            if (it.size > 3) {
                home5th = it[3].homeScore.toString()
                away5th = it[3].awayScore.toString()
            }
        }
        //home6th = (home3rd.toIntS() + home4th.toIntS() + home5th.toIntS()).toString()
        //away6th = (away3rd.toIntS() + away4th.toIntS() + away5th.toIntS()).toString()
        home6th = matchInfo.homeScore ?: "0"
        away6th = matchInfo.awayScore ?: "0"
        extracted1()
    }

    private fun getLocalString(res: Int): String {
        return androidContext.getString(res)
    }


    /**
     * 足球
     * 网球数据
     */
    fun assembleData1(gameType: String?, matchInfo: MatchInfo) {
        //足球
        if (gameType == GameType.FT.key) {
            matchInfo.apply {
                home1st = homeCornerKicks.toString()
                away1st = awayCornerKicks.toString()
                home2nd = homeCards.toString()
                away2nd = awayCards.toString()

                home3rd = homeYellowCards.toString()
                away3rd = awayYellowCards.toString()

                home4th = homeScore.toStringS("0")
                away4th = awayScore.toStringS("0")

//                home5th = (homeScore ?: "0").toString()
//                away5th = (homeScore ?: "0").toString()
                Timber.d("home5th:$home5th away5th:$away5th")
            }
        } else {
            matchInfo.apply {
                Timber.d("assembleData1 matchStatusList.isEmpty:${matchInfo.matchStatusList?.isEmpty()}")
                matchStatusList?.let {
                    if (it.isEmpty()) return@let
                    home1st = it[0].homeScore.toString()
                    away1st = it[0].awayScore.toString()
                    if (it.size > 1) {
                        home2nd = it[1].homeScore.toString()
                        away2nd = it[1].awayScore.toString()
                    }
                    if (it.size > 2) {
                        home3rd = it[2].homeScore.toString()
                        away3rd = it[2].awayScore.toString()
                    }
                    if (it.size > 3) {
                        home4th = it[3].homeScore.toString()
                        away4th = it[3].awayScore.toString()
                    }
                    if (it.size > 4) {
                        home5th = it[4].homeScore.toString()
                        away5th = it[4].awayScore.toString()
                    }
                    if (it.size > 5) {
                        home6th = it[5].homeScore.toString()
                        away6th = it[5].awayScore.toString()
                    }
                    if (it.size > 6) {
                        home7th = it[6].homeScore.toString()
                        away7th = it[6].awayScore.toString()
                    }
                }
                home8th = homeTotalScore.toStringS("0")
                away8th = awayTotalScore.toStringS("0")
                home9th = homePoints.toStringS("0")
                away9th = awayPoints.toStringS("0")
            }
        }
        extracted2(matchInfo, gameType)
    }


    private fun extracted3(matchInfo: MatchInfo) {
        val list: MutableList<String> = mutableListOf()
        list.apply {
            add("1")
            add(home1st)
            add(away1st)
            add("2")
            add(home2nd)
            add(away2nd)
            add("3")
            add(home3rd)
            add(away3rd)
            val totalScore = getLocalString(R.string.J254)
            matchInfo.let {
                when (it.spt) {
                    3 -> {
                        add(totalScore)
                        add(home6th)
                        add(away6th)
                    }

                    7 -> {
                        add("4")
                        add(home4th)
                        add(away4th)
                        add("5")
                        add(home5th)
                        add(away5th)
                        add("6")
                        add(home7th)
                        add(away7th)
                        add("7")
                        add(home8th)
                        add(away8th)
                        add(totalScore)
                        add(home6th)
                        add(away6th)
                    }

                    else -> {
                        add("4")
                        add(home4th)
                        add(away4th)
                        add("5")
                        add(home5th)
                        add(away5th)
                        add(totalScore)
                        add(home6th)
                        add(away6th)
                    }
                }
            }
        }
        chartViewList.postValue(list)
    }

    private fun extracted2(matchInfo: MatchInfo, gameType: String?) {
        val list: MutableList<String> = mutableListOf()
        list.apply {
            if (gameType == GameType.FT.key) {
                add(getLocalString(R.string.J244))
                add(home1st)
                add(away1st)
                add(getLocalString(R.string.P140))
                add(home2nd)
                add(away2nd)
                add(getLocalString(R.string.P141))
                add(home3rd)
                add(away3rd)
                add(getLocalString(R.string.J254))
                add(home4th)
                add(away4th)
//                add(getLocalString(R.string.J254))
//                add(home5th)
//                add(away5th)
            } else {
                add("1")
                add(home1st)
                add(away1st)
                add("2")
                add(home2nd)
                add(away2nd)
                add("3")
                add(home3rd)
                add(away3rd)
                val commonAdd = {
                    add(getLocalString(R.string.J254))
                    add(home8th)
                    add(away8th)
                    add(getLocalString(R.string.N237))
                    add(home9th)
                    add(away9th)
                }
                matchInfo.let {
                    when (it.spt) {
                        3 -> {
                        }

                        7 -> {
                            add("4")
                            add(home4th)
                            add(away4th)
                            add("5")
                            add(home5th)
                            add(away5th)
                            add("6")
                            add(home6th)
                            add(away6th)
                            add("7")
                            add(home7th)
                            add(away7th)
                        }

                        else -> {
                            add("4")
                            add(home4th)
                            add(away4th)
                            add("5")
                            add(home5th)
                            add(away5th)
                        }
                    }
                    commonAdd()
                }
            }
        }
//        list.forEach {
//            Timber.d("it:$it")
//        }
        chartViewList.postValue(list)
    }

    private fun extracted1() {
        val list: MutableList<String> = mutableListOf()
        list.apply {
            clear()
            add(getLocalString(R.string.J245))
            add(home1st)
            add(away1st)
            add(getLocalString(R.string.J246))
            add(home2nd)
            add(away2nd)
            add(getLocalString(R.string.N937))
            add(home3rd)
            add(away3rd)
            add(getLocalString(R.string.J247))
            add(home4th)
            add(away4th)
            add(getLocalString(R.string.J248))
            add(home5th)
            add(away5th)
            add(getLocalString(R.string.J254))
            add(home6th)
            add(away6th)
        }
        chartViewList.postValue(list)
    }


}