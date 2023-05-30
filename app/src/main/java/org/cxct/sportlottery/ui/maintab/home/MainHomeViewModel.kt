package org.cxct.sportlottery.ui.maintab.home

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.toast
import org.cxct.sportlottery.net.PageInfo
import org.cxct.sportlottery.net.bettingStation.BettingStationRepository
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.news.NewsRepository
import org.cxct.sportlottery.net.news.data.NewsDetail
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.match.MatchRound
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.odds.list.MatchLiveData
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.network.sport.SportMenuFilter
import org.cxct.sportlottery.network.sport.publicityRecommend.PublicityRecommendRequest
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryConfigRequest
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData
import org.cxct.sportlottery.network.third_game.third_games.TotalRewardAmountData
import org.cxct.sportlottery.network.third_game.third_games.hot.HandicapData
import org.cxct.sportlottery.network.third_game.third_games.hot.HotMatchLiveData
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.maintab.entity.GameCateData
import org.cxct.sportlottery.ui.maintab.publicity.PromotionItemData
import org.cxct.sportlottery.util.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

open class MainHomeViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
    private val sportMenuRepository: SportMenuRepository,
) : BaseBottomNavViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository,
) {
    private val _publicityRecommend = MutableLiveData<Event<List<Recommend>>>()
    val publicityRecommend: LiveData<Event<List<Recommend>>>
        get() = _publicityRecommend
    val gotConfig: LiveData<Event<Boolean>>
        get() = _gotConfig
    private val _gotConfig = MutableLiveData<Event<Boolean>>()

    private val _sportMenuFilterList =
        MutableLiveData<Event<MutableMap<String?, MutableMap<String?, SportMenuFilter>?>?>>()
    private val _messageListResult = MutableLiveData<Event<MessageListResult?>>()
    val messageListResult: LiveData<Event<MessageListResult?>>
        get() = _messageListResult

    //優惠活動圖文公告
    private val _publicityPromotionList = MutableLiveData<List<PromotionItemData>>()
    val publicityPromotionList: LiveData<List<PromotionItemData>>
        get() = _publicityPromotionList
    val _enterThirdGameResult = MutableLiveData<Pair<String, EnterThirdGameResult>>()
    val enterThirdGameResult: LiveData<Pair<String, EnterThirdGameResult>>
        get() = _enterThirdGameResult
    private val _errorPromptMessage = MutableLiveData<Event<String>>()
    val token = loginRepository.token

    val homeGameData: LiveData<List<QueryGameEntryData>?>
        get() = _homeGameData
    private val _homeGameData = MutableLiveData<List<QueryGameEntryData>?>()
    val elecGameData: LiveData<List<QueryGameEntryData>?>
        get() = _elecGameData
    private val _elecGameData = MutableLiveData<List<QueryGameEntryData>?>()
    val slotGameData: LiveData<List<QueryGameEntryData>?>
        get() = _slotGameData
    private val _slotGameData = MutableLiveData<List<QueryGameEntryData>?>()

    private val _totalRewardAmount = MutableLiveData<List<TotalRewardAmountData>>()
    val totalRewardAmount: LiveData<List<TotalRewardAmountData>>
        get() = _totalRewardAmount

    private val _liveRoundHall = MutableLiveData<List<MatchLiveData>>()
    val liveRoundHall: LiveData<List<MatchLiveData>>
        get() = _liveRoundHall
    val hotLiveData: LiveData<List<HotMatchLiveData>?>
        get() = _hotLiveData
    private val _hotLiveData = MutableLiveData<List<HotMatchLiveData>?>()

    val hotHandicap: LiveData<Event<List<HandicapData>?>>
        get() = _hotHandicap
    private val _hotHandicap = MutableLiveData<Event<List<HandicapData>?>>()

    //賽事列表直播網址
    private val _matchLiveInfo = MutableLiveData<Event<MatchRound>?>()
    val matchLiveInfo: LiveData<Event<MatchRound>?>
        get() = _matchLiveInfo

    //首页的直播地址获取
    private val _homeMatchLiveInfo = MutableLiveData<Event<MatchRound>?>()
    val homeMatchLiveInfo: LiveData<Event<MatchRound>?>
        get() = _homeMatchLiveInfo

    private val _liveRoundCount = MutableLiveData<String>()
    val liveRoundCount: LiveData<String>
        get() = _liveRoundCount
    val gameBalanceResult: LiveData<Event<Triple<String, EnterThirdGameResult, Double>>>
        get() = _gameBalanceResult
    private var _gameBalanceResult =
        MutableLiveData<Event<Triple<String, EnterThirdGameResult, Double>>>()

    val homeNewsList: LiveData<List<NewsItem>>
        get() = _homeNewsList
    private val _homeNewsList = MutableLiveData<List<NewsItem>>()

    val pageNewsList: LiveData<PageInfo<NewsItem>>
        get() = _pageNewsList
    private val _pageNewsList = MutableLiveData<PageInfo<NewsItem>>()

    val newsDetail: LiveData<Pair<Int, NewsDetail?>>
        get() = _newsDetail
    private val _newsDetail = MutableLiveData<Pair<Int, NewsDetail?>>()

    val recordBetNewHttp: LiveData<List<RecordNewEvent>>
        get() = _recordBetNewHttp
    val recordWinsResultHttp: LiveData<List<RecordNewEvent>>
        get() = _recordWinsResultHttp


    //okgames游戏列表
    val homeGamesList: LiveData<List<OKGameBean>>
        get() = _homeGamesList
    private val _homeGamesList = MutableLiveData<List<OKGameBean>>()


    private val _recordBetNewHttp = MutableLiveData<List<RecordNewEvent>>()
    private val _recordWinsResultHttp = MutableLiveData<List<RecordNewEvent>>()

    val bettingStationList: LiveData<List<BettingStation>>
        get() = _bettingStationList
    private val _bettingStationList = MutableLiveData<List<BettingStation>>()

    //region 宣傳頁用
    fun getRecommend() {
        viewModelScope.launch {
            val resultRecommend = doNetwork(androidContext) {
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
            } ?: return@launch

            if (!resultRecommend.success && resultRecommend.result == null) {
                return@launch
            }

            launch(Dispatchers.IO) {
                resultRecommend.result!!.recommendList.filter {
                    !it.menuList.isNullOrEmpty()
                }.forEach { recommend ->
                    recommend.oddsMap = recommend.odds

                    // 过滤掉赔率为空掉对象
                    recommend.oddsMap?.let { oddsMap ->
                        oddsMap.forEach {
                            oddsMap[it.key] =
                                it.value?.filter { null != it }?.toMutableList() ?: mutableListOf()
                        }
                    }

                    with(recommend) {
//                            setupOddsSort()
                        sortOddsByMenu()
                        setupMatchType()
                        setupMatchTime()
                        setupPlayCateNum()
                        setupLeagueName()
                        setupSocketMatchStatus()
                    }
                }
                _publicityRecommend.postValue(Event(resultRecommend.result.recommendList))

                notifyFavorite(FavoriteType.MATCH)
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


    /**
     * 获取首页okgames列表
     */
    var pageIndex = 1
    val pageSize = 6
    var totalCount = 0
    var totalPage = 0
    fun getHomeOKGamesList(
    ) = callApi({ OKGamesRepository.getHomeOKGamesList(pageIndex, pageSize) }) {
        if (it.getData() == null) {
            //hide loading
            _homeGamesList.value = arrayListOf()
        } else {
            totalCount = it.total
            if (totalPage == 0) {
                totalPage = totalCount / pageSize
                if (totalCount % pageSize != 0) {
                    totalPage++
                }
            }
            _homeGamesList.value = it.getData()
        }
    }


    /**
     * 进入OKgame游戏
     */
    fun homeOkGamesEnterThirdGame(gameData: OKGameBean?, baseFragment: BaseFragment<*>) {
        if (gameData == null) {
            _enterThirdGameResult.postValue(
                Pair(
                    "${gameData?.firmCode}", EnterThirdGameResult(
                        resultType = EnterThirdGameResult.ResultType.FAIL,
                        url = null,
                        errorMsg = androidContext.getString(R.string.hint_game_maintenance)
                    )
                )
            )
            return
        }
        requestEnterThirdGame(
            "${gameData.firmType}",
            "${gameData.gameCode}",
            "${gameData.gameCode}",
            baseFragment
        )
    }

    /**
     * 记录最近游戏
     */
    fun homeOkGameAddRecentPlay(okGameBean: OKGameBean) {
        LoginRepository.addRecentPlayGame(okGameBean.id.toString())
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
                    ConfigRepository.config.postValue(configResult)
                    setupDefaultHandicapType()
                    _gotConfig.postValue(Event(true))
                }
            }
        }
    }

    fun getThirdGame() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                ThirdGameRepository.getThirdGame()
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

    //獲取系統公告
    fun getAnnouncement() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                val typeList = arrayOf(1, 2, 3)
                OneBoSportApi.messageService.getPromoteNotice(typeList)
            }?.let { result -> _messageListResult.postValue(Event(result)) }
        }
    }

    fun requestEnterThirdGame(gameData: QueryGameEntryData, baseFragment: BaseFragment<*>) {
        if (gameData == null) {
            _enterThirdGameResult.postValue(
                Pair(
                    "${gameData.firmCode}", EnterThirdGameResult(
                        resultType = EnterThirdGameResult.ResultType.FAIL,
                        url = null,
                        errorMsg = androidContext.getString(R.string.hint_game_maintenance)
                    )
                )
            )
            return
        }

        requestEnterThirdGame(
            "${gameData.firmType}",
            "${gameData.gameCode}",
            "${gameData.gameCategory}",
            baseFragment
        )
    }

    //避免多次请求游戏
    var jumpingGame = false
    fun requestEnterThirdGame(
        firmType: String,
        gameCode: String,
        gameCategory: String,
        baseFragment: BaseFragment<*>,
    ) {
//        Timber.e("gameData: $gameData")
        if (loginRepository.isLogin.value != true) {
            _enterThirdGameResult.postValue(
                Pair(
                    firmType,
                    EnterThirdGameResult(EnterThirdGameResult.ResultType.NEED_REGISTER, null)
                )
            )
            return
        }

        if (jumpingGame) {
            return
        }

        jumpingGame = true
        baseFragment.loading()
        viewModelScope.launch {
            val thirdLoginResult = thirdGameLogin(firmType!!, gameCode!!)
            jumpingGame = false
            //20210526 result == null，代表 webAPI 處理跑出 exception，exception 處理統一在 BaseActivity 實作，這邊 result = null 直接略過
            if (thirdLoginResult == null) {
                baseFragment.hideLoading()
                return@launch
            }

            //先调用三方游戏的登入接口, 确认返回成功200之后再接著调用自动转换额度的接口, 如果没有登入成功, 后面就不做额度自动转换的调用了
            if (!thirdLoginResult.success) {
                _enterThirdGameResult.postValue(
                    Pair(
                        firmType,
                        EnterThirdGameResult(
                            EnterThirdGameResult.ResultType.FAIL,
                            null,
                            thirdLoginResult?.msg
                        )
                    )
                )
                baseFragment.hideLoading()
                return@launch
            }

            val thirdGameResult = EnterThirdGameResult(
                EnterThirdGameResult.ResultType.SUCCESS,
                thirdLoginResult.msg,
                gameCategory
            )
            if (autoTransfer(firmType)) { //第三方自動轉換
                _enterThirdGameResult.postValue(Pair(firmType, thirdGameResult))
                baseFragment.hideLoading()
                return@launch
            }

            getGameBalance(firmType, thirdGameResult, baseFragment)
        }
    }

    //20200302 記錄問題：新增一個 NONE type，來清除狀態，避免 fragment 畫面重啟馬上就會觸發 observe，重複開啟第三方遊戲
    fun clearThirdGame() {
        _enterThirdGameResult.postValue(
            Pair(
                "", EnterThirdGameResult(
                    resultType = EnterThirdGameResult.ResultType.NONE,
                    url = null,
                    errorMsg = null
                )
            )
        )
    }

    private suspend fun thirdGameLogin(firmType: String, gameCode: String): NetResult? {
        return doNetwork(androidContext) {
            OneBoSportApi.thirdGameService.thirdLogin(firmType, gameCode)
        }
    }

    private suspend fun autoTransfer(firmType: String): Boolean {
        if (isThirdTransferOpen()) {
            //若自動轉換功能開啟，要先把錢都轉過去再進入遊戲
            val result = doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.autoTransfer(firmType)
            }
            if (result?.success == true) getMoneyAndTransferOut(false) //金額有變動，通知刷新
            return true
        }

        return false
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

    //region 宣傳頁 優惠活動文字跑馬燈、圖片公告
    fun getPublicityPromotion() {
        sConfigData?.imageList?.filter { it.imageType == ImageType.PROMOTION.code && !(isGooglePlayVersion() && it.isHidden) }
            ?.let { promotionList ->
                promotionList.filter {
                    (it.viewType == 1) && TextUtils.equals(
                        LanguageManager.getSelectLanguage(
                            androidContext
                        ).key, it.lang
                    )
                }
                    .mapNotNull { it.imageText1 }
                    .let {
                        //優惠活動圖片公告清單
                        _publicityPromotionList.postValue(promotionList.map {
                            PromotionItemData.createData(it)
                        })
                    }
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

    private fun Recommend.sortOddsByMenu() {
        val sortOrder = this.menuList?.firstOrNull()?.playCateList?.map { it.code }

        oddsMap?.let { map ->
            val filterPlayCateMap = map.filter { sortOrder?.contains(it.key) == true }
            val sortedMap = filterPlayCateMap.toSortedMap(compareBy<String> {
                sortOrder?.indexOf(it)
            }.thenBy { it })

            map.clear()
            map.putAll(sortedMap)
        }
        oddsSort = oddsMap?.keys?.first()
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
    //二次改版region
    /**
     * 电子和棋牌
     * @position position 1: 首页； 2: 主页
     * @gameType gameType 1: 棋牌； 2: 电子
     */
    fun getGameEntryConfig(position: Int, gameType: Int?) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.queryGameEntryConfig(
                    QueryGameEntryConfigRequest(position, gameType)
                )
            }
            result?.rows.let {
                if (position == 2) {
                    if (gameType == 1) {
                        _slotGameData.postValue(it)
                    } else {
                        _elecGameData.postValue(it)
                    }
                } else {
                    _homeGameData.postValue(it)
                }
            }
        }
    }

    /**
     * 电子金额排名
     */
    fun getTotalRewardAmount() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.queryTotalRewardAmount()
            }
            result?.rows?.let {
                _totalRewardAmount.postValue(it)
            }
        }
    }

    /**
     * 热门盘口
     * @handicapType 盘口类型, 1:独赢 2：让球 3:大小
     */

    fun getHandicapConfig(handicapType: Int) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.getHotHandicapList(handicapType)
            }
            result?.rows.let { handicapList ->
                handicapList?.forEach { hotHandicap ->
                    hotHandicap.matchInfos.forEach {
                        it.leagueName =
                            if (TextUtils.isEmpty(it.leagueName)) hotHandicap.league.name else it.leagueName
                    }
                }
                _hotHandicap.postValue(Event(handicapList))
            }
        }
    }

    /**
     * 热门直播
     */

    fun getHotLiveList() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.thirdGameService.getLiveList()
            }?.let { result ->
                _hotLiveData.postValue(result.MatchLiveList)
            }
        }
    }

    fun getLiveRoundHall() {
        viewModelScope.launch {
            var result = doNetwork(androidContext) {
                OneBoSportApi.matchService.getLiveRoundHall()
            }?.let {
                if (it.success) {
                    _liveRoundHall.postValue(it.MatchLiveList)
                }
            }
        }
    }

    fun getLiveInfo(roundNo: String, position: Int) {
        viewModelScope.launch {
            var result = doNetwork(androidContext) {
                OneBoSportApi.matchService.getMatchLiveRound(roundNo)
            }
            result?.matchRound?.let {
                it.roundNo = roundNo
                if (position == 0) {
                    _homeMatchLiveInfo.postValue(Event(it))
                } else {
                    _matchLiveInfo.postValue(Event(it))
                }

            }
        }
    }

    //直播数量
    fun getLiveRoundCount() {
        viewModelScope.launch {
            var result = doNetwork(androidContext) {
                OneBoSportApi.matchService.getLiveRoundCount()
            }?.let {
                if (it.success) {
                    _liveRoundCount.postValue(it.t.toString())
                }
            }
        }
    }

    private fun getGameBalance(
        firmType: String,
        thirdGameResult: EnterThirdGameResult,
        baseFragment: BaseFragment<*>,
    ) {
        doRequest(androidContext, { OneBoSportApi.thirdGameService.getAllBalance() }) { result ->
            baseFragment.hideLoading()
            var balance: Double = result?.resultMap?.get(firmType)?.money ?: (0).toDouble()
            _gameBalanceResult.postValue(Event(Triple(firmType, thirdGameResult, balance)))
        }
    }

    /**
     * 获取新闻资讯列表
     */
    fun getHomeNews(pageNum: Int, pageSize: Int, categoryIds: List<Int>) {
        viewModelScope.launch {
            callApi({
                NewsRepository.getHomeNews(
                    pageNum,
                    pageSize,
                    NewsRepository.SORT_CREATE_TIME,
                    categoryIds
                )
            }) {
                if (it.succeeded()) {
                    _homeNewsList.postValue(it.getData()?.firstOrNull()?.detailList ?: listOf())
                } else {
                    toast(it.msg)
                }
            }
        }
    }

    /**
     * 获取新闻分页列表
     */
    fun getPageNews(pageNum: Int, pageSize: Int, categoryId: Int) {
        viewModelScope.launch {
            callApi({
                NewsRepository.getPageNews(
                    pageNum,
                    pageSize,
                    NewsRepository.SORT_CREATE_TIME,
                    categoryId
                )
            }) {
                if (it.succeeded()) {
                    it.getData()?.let {
                        _pageNewsList.postValue(it)
                    }
                } else {
                    toast(it.msg)
                }
            }
        }
    }


    /**
     * 获取新闻资讯列表
     */
    fun getNewsDetail(id: Int) =
        callApi({ NewsRepository.getNewsDetail(id, NewsRepository.SORT_CREATE_TIME) }) {
            if (it.succeeded()) {
                _newsDetail.postValue(Pair(id, it.getData()))
            } else {
                _newsDetail.postValue(Pair(id, null))
                toast(it.msg)
            }
        }


    fun getRecordNew() = callApi({ OKGamesRepository.getRecordNew() }) {
        if (it.succeeded()) {
            _recordBetNewHttp.postValue(it.getData())
        } else {
            toast(it.msg)
        }
    }

    fun getRecordResult() = callApi({ OKGamesRepository.getRecordResult() }) {
        if (it.succeeded()) {
            _recordWinsResultHttp.postValue(it.getData())
        } else {
            toast(it.msg)
        }
    }

    fun getBettingStationList() = callApi({ BettingStationRepository.getBettingStationList() }) {
        if (it.succeeded()) {
            _bettingStationList.postValue(it.getData())
        } else {
            toast(it.msg)
        }
    }
}