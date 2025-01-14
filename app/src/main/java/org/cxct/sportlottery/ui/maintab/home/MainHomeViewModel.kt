package org.cxct.sportlottery.ui.maintab.home

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.common.extentions.toast
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.PageData
import org.cxct.sportlottery.net.PageInfo
import org.cxct.sportlottery.net.bettingStation.BettingStationRepository
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.net.games.OKGamesRepository.recentGamesEvent
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.net.money.data.DailyConfig
import org.cxct.sportlottery.net.money.data.FirstDepositDetail
import org.cxct.sportlottery.net.news.NewsRepository
import org.cxct.sportlottery.net.news.data.NewsCategory
import org.cxct.sportlottery.net.news.data.NewsDetail
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.net.user.UserRepository
import org.cxct.sportlottery.net.user.data.ActivityCategory
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.net.user.data.RewardRecord
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.NewsType
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.news.News
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.network.sport.publicityRecommend.PublicityRecommendRequest
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.maintab.entity.EnterThirdGameResult
import org.cxct.sportlottery.util.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

open class MainHomeViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {

    companion object {

        val depositDetailEvent = SingleLiveEvent<FirstDepositDetail>()

    }

    private val _publicityRecommend = MutableLiveData<Event<List<Recommend>>>()
    val publicityRecommend: LiveData<Event<List<Recommend>>>
        get() = _publicityRecommend
    private val _hotESportMatch = SingleLiveEvent<Pair<String,List<Recommend>>>()
    val hotESportMatch: LiveData<Pair<String,List<Recommend>>>
        get() = _hotESportMatch
    val gotConfig: LiveData<Event<Boolean>>
        get() = _gotConfig
    private val _gotConfig = MutableLiveData<Event<Boolean>>()

    private val _messageListResult = MutableLiveData<Event<MessageListResult?>>()
    val messageListResult: LiveData<Event<MessageListResult?>>
        get() = _messageListResult

    val _enterThirdGameResult = SingleLiveEvent<Pair<String, EnterThirdGameResult>>()
    val enterThirdGameResult: LiveData<Pair<String, EnterThirdGameResult>>
        get() = _enterThirdGameResult

    //试玩线路
    private val _enterTrialPlayGameResult = SingleLiveEvent<Pair<String, EnterThirdGameResult>?>()
    val enterTrialPlayGameResult: LiveData<Pair<String, EnterThirdGameResult>?>
        get() = _enterTrialPlayGameResult

    //试玩线路
    private val _guestLoginGameResult = SingleLiveEvent<Pair<String, EnterThirdGameResult>?>()
    val guestLoginGameResult: LiveData<Pair<String, EnterThirdGameResult>?>
        get() = _guestLoginGameResult

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

    //最新投注
    val recordBetHttp: LiveData<List<RecordNewEvent>>
        get() = _recordBetHttp
    //最新大奖
    val recordWinHttp: LiveData<List<RecordNewEvent>>
        get() = _recordWinHttp


    //okgames游戏列表
    val homeOKGamesList: LiveData< List<OKGameBean>>
        get() = _homeOKGamesList
    private val _homeOKGamesList = MutableLiveData< List<OKGameBean>>()

    //oklive游戏列表
    val homeOKLiveList: LiveData< List<OKGameBean>>
        get() = _homeOKLiveList
    private val _homeOKLiveList = MutableLiveData< List<OKGameBean>>()

    private val _recordBetHttp = MutableLiveData<List<RecordNewEvent>>()
    private val _recordWinHttp = MutableLiveData<List<RecordNewEvent>>()

    val bettingStationList: LiveData<List<BettingStation>>
        get() = _bettingStationList
    private val _bettingStationList = MutableLiveData<List<BettingStation>>()

    val activityImageList: LiveData<List<ActivityImageList>>
        get() = _activityImageList
    private val _activityImageList = MutableLiveData<List<ActivityImageList>>()
    val activityCategroyList = SingleLiveEvent<Pair<String?, List<ActivityCategory>?>>()
    val activityList = SingleLiveEvent<Pair<String?, List<ActivityImageList>?>>()

    val activityDetail: LiveData<ActivityImageList?>
        get() = _activityDetail
    private val _activityDetail = MutableLiveData<ActivityImageList?>()

    val activityApply: LiveData<String?>
        get() = _activityApply
    private val _activityApply = MutableLiveData<String?>()

    val rewardRecord: LiveData<PageData<RewardRecord>>
        get() = _rewardRecord
    private val _rewardRecord = MutableLiveData<PageData<RewardRecord>>()

    val homeAllProvider: LiveData<List<OKGamesFirm>>
        get() = _homeAllProvider
    private val _homeAllProvider = MutableLiveData<List<OKGamesFirm>>()

    var dailyConfigEvent = SingleLiveEvent<List<DailyConfig>>()

    val systemNotice = SingleLiveEvent<ArrayList<News>>()

    val newsCategory: LiveData<List<NewsCategory>>
        get() = _newsCategory
    private val _newsCategory = MutableLiveData<List<NewsCategory>>()

    val showFirstDepositDetail = SingleLiveEvent<Boolean>()
    val firstDepositDetailEvent = SingleLiveEvent<FirstDepositDetail?>()

    val getFirstDepositAfterDay = SingleLiveEvent<ApiResult<Boolean>>()

    private var loadingGameTypes = mutableMapOf<GameType?, Long>()

    val taskRedDotEvent get() = TaskCenterRepository.taskRedDotEvent

    //region 宣傳頁用
    fun getRecommend(gameType: GameType? = null) = launch {
        val timeStep = loadingGameTypes[gameType] ?: 0
        if (System.currentTimeMillis() - timeStep < 15_000) { //拦截15秒内还未响应的相同请求
            return@launch
        }

        loadingGameTypes[gameType] = System.currentTimeMillis()
        launch {
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
                        startTimeStamp.toString(),
                        gameType=gameType?.key
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
                if (gameType==null){
                    _publicityRecommend.postValue(Event(resultRecommend.result.recommendList))
                }else{
                    _hotESportMatch.postValue(Pair(gameType.key,resultRecommend.result.recommendList))
                }
                notifyFavorite(FavoriteType.MATCH)
            }
        }

        loadingGameTypes.remove(gameType)
    }

    /**
     * 获取游戏厂商列表
     */
    fun getGameFirms() {
        callApi({ OKGamesRepository.getGameFirms() }) {
            _homeAllProvider.postValue(it.getData()?.filter { it.open==1 }?: listOf())
        }
    }

    val newGameList by lazy { MutableLiveData<List<OKGameBean>?>() }
    fun getNewGameList(pageIndex: Int = 1, pageSize: Int = 30)
    = callApi({ OKGamesRepository.getNewGameList(pageIndex, pageSize) }) {
        newGameList.value = it.getData()
    }

    fun getHomeOKGamesList(
    ) = callApi({
        OKGamesRepository.getHomeOKGamesList(
            GameEntryType.OKGAMES,1, 18
        )
    }) {
        if (it.getData() == null) {
            //hide loading
            _homeOKGamesList.value = arrayListOf()
        } else {
            _homeOKGamesList.value=it.getData()
        }
    }
    fun getOkLiveOKGamesList(
    ) = callApi({
        OKGamesRepository.getHomeOKGamesList(
            GameEntryType.OKLIVE,1, 18
        )
    }) {
        if (it.getData() == null) {
            //hide loading
            _homeOKLiveList.value = arrayListOf()
        } else {
            _homeOKLiveList.value=it.getData()
        }
    }


    val homeGamesList300: LiveData< List<OKGameBean>>
        get() = _homeGamesList300
    private val _homeGamesList300 = MutableLiveData< List<OKGameBean>>()
    fun getHomeOKGamesList300(
    ) = callApi({
        OKGamesRepository.getHomeOKGamesList(
            GameEntryType.OKGAMES,
             1,  300
        )
    }) {
        if (it.getData() == null) {
            //hide loading
            _homeGamesList300.value = arrayListOf()
        } else {
            _homeGamesList300.value=it.getData()
        }
    }

    val hotGameList = MutableLiveData<List<OKGameBean>?>()
    fun getHotGameList() = callApi({ OKGamesRepository.getHotGameList(1, 60) }) {
        hotGameList.value = it.getData()
    }


    //region 宣傳頁推薦賽事資料處理
    /**
     * 設置賽事類型參數(滾球、即將、今日、早盤,波胆)
     */
    private fun Recommend.setupMatchType() {
        matchType = when (status) {
            1 -> {
                MatchType.IN_PLAY
            }
            else -> {
                when {
                    TimeUtil.isTimeAtStart(startTime) -> {
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


    //獲取系統公告
    fun getAnnouncement() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                val typeList = arrayOf(1, 2, 3)
                OneBoSportApi.messageService.getPromoteNotice(typeList)
            }?.let { result -> _messageListResult.postValue(Event(result)) }
        }
    }
    /**
     * 未登录试玩
     */
    fun requestEnterThirdGameNoLogin(okGameBean: OKGameBean){
        if(okGameBean.firmType==null){
            //不支持试玩
            _enterTrialPlayGameResult.postValue(null)
            return
        }

        viewModelScope.launch {
            //请求试玩线路
            val result= doNetwork(androidContext) {
               OneBoSportApi.thirdGameService.thirdNoLogin(okGameBean.firmType, okGameBean.gameCode)
            }
            if (result !=null && result.success && result.msg.isNotEmpty()){
                //获得了试玩路径
                val thirdGameResult = EnterThirdGameResult(EnterThirdGameResult.ResultType.SUCCESS, result.msg, null, okGameBean)
                _enterTrialPlayGameResult.postValue(Pair(okGameBean.firmType, thirdGameResult))
            } else {
                requestGuestEnterGame(okGameBean)
            }
        }
    }

    /**
     * 访客登录三方游戏
     */
    private fun requestGuestEnterGame(okGameBean: OKGameBean){
        if(okGameBean.firmType==null){
            _guestLoginGameResult.postValue(null)
            return
        }
        if (OKGamesRepository.isGuestOpen(okGameBean.firmType)){
            callApi({ OKGamesRepository.guestLogin(okGameBean.firmType!!,okGameBean.gameCode!!)}) {
                if (it.succeeded()){
                    val thirdGameResult = EnterThirdGameResult(EnterThirdGameResult.ResultType.SUCCESS, it.msg, null, okGameBean, guestLogin = true)
                    _guestLoginGameResult.postValue(Pair(okGameBean.firmType, thirdGameResult))
                }else{
                    _guestLoginGameResult.postValue(null)
                }
            }
        }else{
            _guestLoginGameResult.postValue(null)
        }
    }

    //避免多次请求游戏
    var jumpingGame = false
    fun requestEnterThirdGame(
        okGameBean: OKGameBean,
        baseActivity: BaseActivity<*,*>,
    ) {
        val firmType = okGameBean.firmType ?: return
//        Timber.e("gameData: $gameData")
        if (LoginRepository.isLogin.value != true) {
            _enterThirdGameResult.postValue(Pair(firmType,
                EnterThirdGameResult(EnterThirdGameResult.ResultType.NEED_REGISTER, null, "", okGameBean)))
            return
        }

        if (jumpingGame) {
            return
        }
        jumpingGame = true
        baseActivity.loading()
        viewModelScope.launch {
            //FKG厂商的游戏，先转金额到游戏，然后在登录
            if (okGameBean.firmCode=="FKG"){
                if (LoginRepository.isLogined()&&!OKGamesRepository.isSingleWalletType(firmType) && isThirdTransferOpen()) {
                    async {
                        autoTransfer(firmType)
                    }.await()
                }
            }
            val thirdLoginResult = thirdGameLogin(firmType!!, okGameBean.gameCode!!)
            jumpingGame = false
            //20210526 result == null，代表 webAPI 處理跑出 exception，exception 處理統一在 BaseActivity 實作，這邊 result = null 直接略過
            if (thirdLoginResult == null) {
                baseActivity.hideLoading()
                return@launch
            }

            //先调用三方游戏的登入接口, 确认返回成功200之后再接著调用自动转换额度的接口, 如果没有登入成功, 后面就不做额度自动转换的调用了
            if (!thirdLoginResult.success) {
                _enterThirdGameResult.postValue(Pair(firmType, EnterThirdGameResult(EnterThirdGameResult.ResultType.FAIL,  null, thirdLoginResult?.msg, okGameBean)))
                baseActivity.hideLoading()
                return@launch
            }

            val thirdGameResult = EnterThirdGameResult(EnterThirdGameResult.ResultType.SUCCESS, url = thirdLoginResult.msg,errorMsg = null, okGameBean)
            if (OKGamesRepository.isSingleWalletType(firmType)){
                _enterThirdGameResult.postValue(Pair(firmType, thirdGameResult))
                baseActivity.hideLoading()
            }else{
                if (isThirdTransferOpen()){
                    _enterThirdGameResult.postValue(Pair(firmType, thirdGameResult))
                    baseActivity.hideLoading()
                }else{
                    getGameBalance(firmType,thirdGameResult,baseActivity)
                }
            }
        }
    }
    fun transfer(firmType: String){
        viewModelScope.launch {
            autoTransfer(firmType)
        }
    }

    //20200302 記錄問題：新增一個 NONE type，來清除狀態，避免 fragment 畫面重啟馬上就會觸發 observe，重複開啟第三方遊戲
    fun clearThirdGame() {
        _enterThirdGameResult.postValue(Pair("", EnterThirdGameResult(
            resultType = EnterThirdGameResult.ResultType.NONE,
            url = null,
            errorMsg = null,
            okGameBean = null
        )))
    }

    private suspend fun thirdGameLogin(firmType: String, gameCode: String): NetResult? {
        return doNetwork(androidContext) {
            OneBoSportApi.thirdGameService.thirdLogin(firmType, gameCode)
        }
    }


    private suspend fun autoTransfer(firmType: String) {
        //若自動轉換功能開啟，要先把錢都轉過去再進入遊戲
        val result = doNetwork(androidContext) {
            OneBoSportApi.thirdGameService.autoTransfer(firmType)
        }
        if (result?.success == true) getMoneyAndTransferOut(false) //金額有變動，通知刷新
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
        matchInfo?.categoryCode = league.categoryCode
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


    private fun getGameBalance(
        firmType: String,
        thirdGameResult: EnterThirdGameResult,
        baseActivity: BaseActivity<*,*>
    ) {
        doRequest({ OneBoSportApi.thirdGameService.getAllBalance() }) { result ->
            baseActivity.hideLoading()
            var balance: Double = result?.resultMap?.get(firmType)?.money ?: (0).toDouble()
            _gameBalanceResult.postValue(Event(Triple(firmType, thirdGameResult, balance)))
        }
    }

    /**
     * 获取新闻资讯列表
     */
    fun getHomeNews(pageNum: Int, pageSize: Int, categoryIds: List<Int>) {
        viewModelScope.launch {
            callApi({ NewsRepository.getHomeNews(pageNum, pageSize, NewsRepository.SORT_CREATE_TIME,categoryIds) }) {
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
            callApi({ NewsRepository.getPageNews(pageNum, pageSize, NewsRepository.SORT_CREATE_TIME,categoryId) }) {
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
    fun getNewsDetail(id: Int) = callApi({ NewsRepository.getNewsDetail(id,NewsRepository.SORT_CREATE_TIME) }) {
        if (it.succeeded()) {
            _newsDetail.postValue(Pair(id, it.getData()))
        } else {
            _newsDetail.postValue(Pair(id, null))
            toast(it.msg)
        }
    }

    //首页最新投注
    fun getBetRecord() = callApi({ OKGamesRepository.getRecordNew() }) {
        if (it.succeeded()) {
            _recordBetHttp.postValue(it.getData())
        } else {
            toast(it.msg)
        }
    }
    //首页最新大奖
    fun getWinRecord() = callApi({ OKGamesRepository.getRecordResult() }) {
        if (it.succeeded()) {
            _recordWinHttp.postValue(it.getData())
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

    fun getActivityCategoryList() = callApi({ UserRepository.activityCategoryList() }) {
        activityCategroyList.value = Pair(it.msg, if (it.succeeded()) it.getData() else null)
    }

    fun getActivityList(activityCategoryId: Int? = null) = callApi({UserRepository.activityImageListH5(activityCategoryId)}) {
        activityList.value = Pair(it.msg, if(it.succeeded()) it.getData()?.sortedBy { -it.imageSort } else null)
    }

    fun getActivityImageListH5() = callApi({UserRepository.activityImageListH5(null)}){
        if (it.succeeded()){
            _activityImageList.postValue(it.getData()?.sortedBy { -it.imageSort })
        }else{
            toast(it.msg)
        }
    }

    fun activityDetailH5(activityId:String) = callApi({UserRepository.activityDetailH5(activityId)}){
        if (it.succeeded()){
            _activityDetail.postValue(it.getData())
        }else{
            toast(it.msg)
        }
    }

    fun activityApply(activityId: String) = callApi({UserRepository.activityApply(activityId)}){
        if (it.succeeded()){
            _activityApply.postValue(it.getData())
        }else{
            toast(it.msg)
        }
    }

    fun activityRecord(activityId: String, page: Int, pageSize: Int=20) = callApi({UserRepository.activityRecord(activityId,page,pageSize)}){
        if (it.succeeded()){
            _rewardRecord.postValue(it.getData())
        }else{
            toast(it.msg)
        }
    }

    fun getDailyConfig(){
        callApi({org.cxct.sportlottery.net.money.MoneyRepository.rechDailyConfig()}){
            if (it.succeeded()){
                it.getData()?.let {
                    dailyConfigEvent.postValue(it)
                }
            }
        }
    }

    fun getSystemNotice() {
        callApi({ NewsRepository.getMessageList(1, 20, NewsType.PLAT) }) {
            it.getData()?.let { systemNotice.postValue(it) }
        }
    }

    fun getNewsCategory(){
        callApi({ NewsRepository.getRecommendNews() }) {
            it.getData()?.let { _newsCategory.postValue(it) }
        }
    }
    fun getHallOkSport() {
        if (sConfigData?.sbSportSwitch==1) {
            callApi({ OKGamesRepository.getHallOKSport() }) {}
        }
    }
    fun getFirstDepositDetail() {
        if (LoginRepository.isLogined()){
            callApi({ org.cxct.sportlottery.net.money.MoneyRepository.firstDepositDetail() }) {
                val userStatusChanged = firstDepositDetailEvent.value?.userStatus != it.getData()?.userStatus
                firstDepositDetailEvent.postValue(it.getData())
                showFirstDepositDetail.postValue(userStatusChanged)
            }
        }else{
            firstDepositDetailEvent.postValue(null)
            showFirstDepositDetail.postValue(false)
        }
    }
    fun getFirstDepositAfterDay() {
        callApi({ org.cxct.sportlottery.net.money.MoneyRepository.getFirstDepositAfterDay() }) {
            getFirstDepositAfterDay.postValue(it)
        }
    }
    /**
     * 获取最近游戏
     */
    fun getHomeRecentPlay() {
        if (LoginRepository.isLogined()) {  // 没登录不显示最近玩的游戏
            callApi({OKGamesRepository.getRecentGames()}){}
        }else{
            recentGamesEvent.postValue(listOf())
        }
    }
}