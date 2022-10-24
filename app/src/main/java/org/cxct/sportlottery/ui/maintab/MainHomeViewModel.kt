package org.cxct.sportlottery.ui.maintab

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.sport.SportMenu
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.network.sport.SportMenuFilter
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.network.sport.publicityRecommend.PublicityRecommendRequest
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.network.third_game.ThirdGameService
import org.cxct.sportlottery.network.third_game.ThirdLoginResult
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryConfigRequest
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.ui.game.publicity.PublicityMenuData
import org.cxct.sportlottery.ui.game.publicity.PublicityPromotionItemData
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.util.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class MainHomeViewModel(
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

    //新版宣傳頁菜單資料
    private val _publicityMenuData = MutableLiveData<PublicityMenuData>()
    val publicityMenuData: LiveData<PublicityMenuData>
        get() = _publicityMenuData
    private val _sportMenuFilterList =
        MutableLiveData<Event<MutableMap<String?, MutableMap<String?, SportMenuFilter>?>?>>()
    private val _sportMenuResult = MutableLiveData<SportMenuResult?>()
    private var sportMenuData: SportMenuData? = null //球種菜單資料
    private val _messageListResult = MutableLiveData<Event<MessageListResult?>>()
    val messageListResult: LiveData<Event<MessageListResult?>>
        get() = _messageListResult

    //優惠活動圖文公告
    private val _publicityPromotionList = MutableLiveData<List<PublicityPromotionItemData>>()
    val publicityPromotionList: LiveData<List<PublicityPromotionItemData>>
        get() = _publicityPromotionList
    private val _enterThirdGameResult = MutableLiveData<EnterThirdGameResult>()
    val enterThirdGameResult: LiveData<EnterThirdGameResult>
        get() = _enterThirdGameResult
    private val _errorPromptMessage = MutableLiveData<Event<String>>()
    val token = loginRepository.token

    val cardGameData: LiveData<List<ThirdDictValues?>>
        get() = _cardGameData
    private val _cardGameData = MutableLiveData<List<ThirdDictValues?>>()
    val homeGameData: LiveData<List<ThirdDictValues?>>
        get() = _homeGameData
    private val _homeGameData = MutableLiveData<List<ThirdDictValues?>>()

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
                    result.result.recommendList.filter {
                        !it.menuList.isNullOrEmpty()
                    }.forEach { recommend ->
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
                    setupDefaultHandicapType()
                    _gotConfig.postValue(Event(true))
                }
            }
        }
    }

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
                                    GameType.getSpecificLanguageString(androidContext,
                                        gameType.key,
                                        LanguageManager.Language.EN.key),
                                    GameType.getGameTypeMenuIcon(gameType)
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
                            getSportCount(MatchType.IN_PLAY,
                                gameType,
                                sportMenuResult) + getSportCount(
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
                var cardGameList = mutableListOf<ThirdDictValues?>(null, null, null)
                gameCateDataList.forEach { gameCateData ->
                    gameCateData.tabDataList.forEach { gameTabData ->
                        gameTabData.gameList.forEach { gameItemData ->
                            gameItemData.thirdGameData?.let {
                                //PM 指定捞起这几个游戏，没有捞到或者open=0，就显示敬请期待
                                when (it.firmCode) {
                                    "TPG" -> cardGameList[0] = it
                                    "FKG" -> cardGameList[1] = it
                                    "CGQP" -> cardGameList[2] = it
                                }
                            }
                        }
                    }
                }
                _cardGameData.postValue(cardGameList.toList())
            }
        }
    }

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

    private fun getSportCount(
        matchType: MatchType,
        gameType: GameType?,
        sportMenuResult: SportMenuResult? = null,
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

    /**
     * 更新publicityMenuData
     */
    private fun updatePublicityMenuLiveData(
        sportMenuDataList: List<SportMenu>? = null,
        eGameMenuDataList: ThirdDictValues? = null,
        casinoMenuDataList: ThirdDictValues? = null,
        sabongMenuDataList: ThirdDictValues? = null,
        isNewestVersion: Boolean? = null,
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

    //避免多次请求游戏
    var jumpingGame = false
    fun requestEnterThirdGame(gameData: ThirdDictValues?) {
//        Timber.e("gameData: $gameData")
        when {
            gameData == null -> {
                _enterThirdGameResult.postValue(
                    EnterThirdGameResult(
                        resultType = EnterThirdGameResult.ResultType.FAIL,
                        url = null,
                        errorMsg = androidContext.getString(R.string.hint_game_maintenance)
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
                if (jumpingGame) {
                    return
                }
                jumpingGame = true
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
                        jumpingGame = false
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
        sConfigData?.imageList?.filter { it.imageType == ImageType.PROMOTION.code }
            ?.let { promotionList ->
                promotionList.filter {
                    (it.viewType == 1) && TextUtils.equals(LanguageManager.getSelectLanguage(
                        androidContext).key, it.lang)
                }
                    .mapNotNull { it.imageText1 }
                    .let {
                        //優惠活動圖片公告清單
                        _publicityPromotionList.postValue(promotionList.map {
                            PublicityPromotionItemData.createData(it)
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
     */
    fun getGameEntryConfig(position: Int){
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
               OneBoSportApi.thirdGameService.queryGameEntryConfig(
                   QueryGameEntryConfigRequest(position,null)
               )
            }
            result?.let { result->
                LogUtil.toJson(result)
            }
        }
    }

    //endregion
}