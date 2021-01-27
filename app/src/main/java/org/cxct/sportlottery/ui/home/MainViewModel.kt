package org.cxct.sportlottery.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.league.LeagueListRequest
import org.cxct.sportlottery.network.league.LeagueListResult
import org.cxct.sportlottery.network.match.MatchPreloadRequest
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.odds.detail.OddsDetailRequest
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.odds.list.OddsListRequest
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListRequest
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListResult
import org.cxct.sportlottery.network.outright.odds.Winner
import org.cxct.sportlottery.network.outright.season.OutrightSeasonListRequest
import org.cxct.sportlottery.network.outright.season.OutrightSeasonListResult
import org.cxct.sportlottery.network.playcate.PlayCateListResult
import org.cxct.sportlottery.network.service.global_stop.GlobalStopEvent
import org.cxct.sportlottery.network.service.match_clock.MatchClockEvent
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.network.service.notice.NoticeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.service.order_settlement.OrderSettlementEvent
import org.cxct.sportlottery.network.service.ping_pong.PingPongEvent
import org.cxct.sportlottery.network.service.producer_up.ProducerUpEvent
import org.cxct.sportlottery.network.service.user_notice.UserNoticeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.SportMenuRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.service.BackService
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.data.Date
import org.cxct.sportlottery.ui.home.broadcast.BroadcastRepository
import org.cxct.sportlottery.ui.home.gameDrawer.GameEntity
import org.cxct.sportlottery.ui.odds.OddsDetailListData
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel(
    private val androidContext: Context,
    private val userInfoRepository: UserInfoRepository,
    private val loginRepository: LoginRepository,
    private val sportMenuRepository: SportMenuRepository,
    private val betInfoRepository: BetInfoRepository
) : BaseViewModel() {

    val isLogin: LiveData<Boolean> by lazy {
        loginRepository.isLogin.apply {
            if (this.value == false && !loginRepository.isCheckToken) {
                checkToken()
            }
        }
    }

    val token = loginRepository.token
    val userId = loginRepository.userId

    val messageListResult: LiveData<MessageListResult>
        get() = _messageListResult

    val sportMenuResult: LiveData<SportMenuResult>
        get() = _sportMenuResult

    val matchPreloadInPlay: LiveData<MatchPreloadResult>
        get() = _matchPreloadInPlay

    val matchPreloadToday: LiveData<MatchPreloadResult>
        get() = _matchPreloadToday

    val oddsListGameHallResult: LiveData<OddsListResult?>
        get() = _oddsListGameHallResult

    val oddsListResult: LiveData<OddsListResult?>
        get() = _oddsListResult

    val leagueListResult: LiveData<LeagueListResult?>
        get() = _leagueListResult

    val outrightSeasonListResult: LiveData<OutrightSeasonListResult?>
        get() = _outrightSeasonListResult

    val outrightOddsListResult: LiveData<OutrightOddsListResult?>
        get() = _outrightOddsListResult

    val curPlayType: LiveData<PlayType>
        get() = _curPlayType

    val curDate: LiveData<List<Date>>
        get() = _curDate

    val curOddsDetailParams: LiveData<List<String?>>
        get() = _curOddsDetailParams

    val matchTypeCard: LiveData<MatchType>
        get() = _matchTypeCard

    val isOpenMatchOdds: LiveData<Boolean>
        get() = _isOpenMatchOdds

    val userInfo: LiveData<UserInfo?> = userInfoRepository.userInfo.asLiveData()

    private val _messageListResult = MutableLiveData<MessageListResult>()
    private val _sportMenuResult = MutableLiveData<SportMenuResult>()
    private val _matchPreloadInPlay = MutableLiveData<MatchPreloadResult>()
    private val _matchPreloadToday = MutableLiveData<MatchPreloadResult>()
    private val _oddsListGameHallResult = MutableLiveData<OddsListResult?>()
    private val _oddsListResult = MutableLiveData<OddsListResult?>()
    private val _leagueListResult = MutableLiveData<LeagueListResult?>()
    private val _outrightSeasonListResult = MutableLiveData<OutrightSeasonListResult?>()
    private val _outrightOddsListResult = MutableLiveData<OutrightOddsListResult?>()
    private val _curPlayType = MutableLiveData<PlayType>().apply {
        value = PlayType.OU_HDP
    }
    private val _curDate = MutableLiveData<List<Date>>()
    private val _curOddsDetailParams = MutableLiveData<List<String?>>()
    private val _asStartCount = MutableLiveData<Int>()
    private val _matchTypeCard = MutableLiveData<MatchType>()
    private val _isOpenMatchOdds = MutableLiveData<Boolean>()

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

    private val _betInfoResult = MutableLiveData<BetInfoResult>()
    val betInfoResult: LiveData<BetInfoResult>
        get() = _betInfoResult

    private val _betInfoList = MutableLiveData<MutableList<BetInfoListData>>()
    val betInfoList: LiveData<MutableList<BetInfoListData>>
        get() = _betInfoList

    private val _oddsDetailResult = MutableLiveData<OddsDetailResult?>()
    val oddsDetailResult: LiveData<OddsDetailResult?>
        get() = _oddsDetailResult

    private val _playCateListResult = MutableLiveData<PlayCateListResult?>()
    val playCateListResult: LiveData<PlayCateListResult?>
        get() = _playCateListResult

    private val _oddsDetailList = MutableLiveData<ArrayList<OddsDetailListData>>()
    val oddsDetailList: LiveData<ArrayList<OddsDetailListData>>
        get() = _oddsDetailList


    //BroadCastReceiver

    val globalStop: LiveData<GlobalStopEvent?>
        get() = BroadcastRepository().instance().globalStop

    val matchClock: LiveData<MatchClockEvent?>
        get() = BroadcastRepository().instance().matchClock

    val matchOddsChange: LiveData<MatchOddsChangeEvent?>
        get() = BroadcastRepository().instance().matchOddsChange

    val matchStatusChange: LiveData<MatchStatusChangeEvent?>
        get() = BroadcastRepository().instance().matchStatusChange

    val notice: LiveData<NoticeEvent?>
        get() = BroadcastRepository().instance().notice

    val oddsChange: LiveData<OddsChangeEvent?>
        get() = BroadcastRepository().instance().oddsChange

    val orderSettlement: LiveData<OrderSettlementEvent?>
        get() = BroadcastRepository().instance().orderSettlement

    val pingPong: LiveData<PingPongEvent?>
        get() = BroadcastRepository().instance().pingPong

    val producerUp: LiveData<ProducerUpEvent?>
        get() = BroadcastRepository().instance().producerUp

    private val _userMoney = BroadcastRepository().instance().userMoney
    val userMoney: LiveData<Double?> //使用者餘額
        get() = _userMoney

    val userNotice: LiveData<UserNoticeEvent?>
        get() = BroadcastRepository().instance().userNotice


    private val _isParlayPage = MutableLiveData<Boolean>()
    val isParlayPage: LiveData<Boolean>
        get() = _isParlayPage

    fun isParlayPage(boolean: Boolean) {
        _isParlayPage.postValue(boolean)
    }

    private fun checkToken() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.checkToken()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.logout()
            }.apply {
                loginRepository.clear()
                //TODO change timber to actual logout ui to da
                Timber.d("logout result is ${this?.success} ${this?.code} ${this?.msg}")
            }
        }
    }

    //獲取系統公告
    fun getAnnouncement() {
        val messageType = "1"
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.messageService.getMessageList(messageType)
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
                _matchPreloadInPlay.postValue(result)
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

    fun getGameHallList(matchType: MatchType, sportType: SportType?) {
        sportType?.let {
            _sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.map {
                it.isSelected = (it.code == sportType.code)
            }
        }

        _matchTypeCard.postValue(matchType)
    }

    fun getGameHallList(matchType: MatchType, item: Item) {
        updateSportSelectedState(matchType, item)
        getGameHallList(matchType, false)
    }

    fun getGameHallList(matchType: MatchType, date: Date) {
        updateDateSelectedState(date)
        getGameHallList(matchType, false)
    }

    fun getGameHallList(matchType: MatchType, isReloadDate: Boolean) {
        if (isReloadDate) {
            getDateRow(matchType)
        }

        when (matchType) {
            MatchType.IN_PLAY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.inPlay?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getOddsList(gameType, matchType.postValue)
                }
            }
            MatchType.TODAY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.today?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getLeagueList(gameType, matchType.postValue, getCurrentTimeRangeParams())
                }
            }
            MatchType.EARLY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.early?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getLeagueList(gameType, matchType.postValue, getCurrentTimeRangeParams())
                }
            }
            MatchType.PARLAY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getLeagueList(gameType, matchType.postValue, getCurrentTimeRangeParams())
                }
            }
            MatchType.OUTRIGHT -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.outright?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getOutrightSeasonList(it)
                }
            }
            MatchType.AT_START -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.atStart?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getOddsList(gameType, matchType.postValue, getCurrentTimeRangeParams())
                }
            }
        }
    }

    fun getNowUrlHall (cateMenuCode: String? = CateMenuCode.HDP_AND_OU.code, eventId: String?): String {
        return "${BackService.URL_HALL}$nowGameType/$cateMenuCode/$eventId"
    }

    fun getNowUrlEvent (eventId: String?): String {
        return "${BackService.URL_EVENT}${eventId}"
    }

    var nowGameType: String? = SportType.FOOTBALL.code

    fun getLeagueOddsList(matchType: MatchType, leagueId: String) {
        val leagueIdList by lazy {
            listOf(leagueId)
        }
        when (matchType) {
            MatchType.TODAY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.today?.items?.find {
                    it.isSelected
                }?.code
                nowGameType = gameType

                gameType?.let {
                    getOddsList(
                        gameType,
                        matchType.postValue,
                        getCurrentTimeRangeParams(),
                        leagueIdList
                    )
                }
            }

            MatchType.EARLY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.early?.items?.find {
                    it.isSelected
                }?.code
                nowGameType = gameType


                gameType?.let {
                    getOddsList(
                        gameType,
                        matchType.postValue,
                        getCurrentTimeRangeParams(),
                        leagueIdList
                    )
                }
            }

            MatchType.PARLAY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.find {
                    it.isSelected
                }?.code
                nowGameType = gameType


                gameType?.let {
                    getOddsList(
                        gameType,
                        matchType.postValue,
                        getCurrentTimeRangeParams(),
                        leagueIdList
                    )
                }
            }
            else -> {
            }
        }

        _isOpenMatchOdds.postValue(true)
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
                _outrightOddsListResult.postValue(result)
            }
        }

        _isOpenMatchOdds.postValue(true)
    }

    fun updateOutrightOddsSelectedState(winner: Winner) {
        val result = _outrightOddsListResult.value

        val winnerList =
            result?.outrightOddsListData?.leagueOdds?.get(0)?.matchOdds?.get(
                0
            )?.odds?.values?.first() ?: listOf()

        winnerList.map {
            it.isSelected = (it == winner)
        }

        _outrightOddsListResult.postValue(result)
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

            if (leagueIdList != null) {
                _oddsListResult.postValue(result)
            } else {
                _oddsListGameHallResult.postValue(result)
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
            _leagueListResult.postValue(result)
        }
    }

    private fun getOutrightSeasonList(gameType: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.outrightService.getOutrightSeasonList(
                    OutrightSeasonListRequest(gameType)
                )
            }

            _outrightSeasonListResult.postValue(result)
        }
    }

    private fun getDateRow(matchType: MatchType) {
        val dateRow = mutableListOf<Date>()

        when (matchType) {
            MatchType.TODAY -> {
                dateRow.add(Date("", TimeUtil.getTodayTimeRangeParams()))
            }
            MatchType.EARLY -> {
                TimeUtil.getOneWeekDate().forEach {
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
                TimeUtil.getOneWeekDate().forEach {
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

        _curDate.postValue(dateRow)
    }

    private fun getCurrentTimeRangeParams(): TimeRangeParams? {
        return _curDate.value?.find {
            it.isSelected
        }?.timeRangeParams
    }

    fun setPlayType(playType: PlayType) {
        _curPlayType.postValue(playType)
    }

    fun getOddsDetail(oddId: String) {
        val item = _sportMenuResult.value?.sportMenuData?.menu?.inPlay?.items?.find {
            it.isSelected
        }
        _curOddsDetailParams.postValue(listOf(item?.code, item?.name, oddId))
    }

    fun getOddsDetail(entity: GameEntity) {
        _curOddsDetailParams.postValue(listOf(entity.code, entity.name, entity.match?.id))
    }

    fun setOddsDetailMoreList(list: List<*>) {
        _oddsDetailMoreList.postValue(list)
    }

    fun getBetInfoList(oddsList: List<Odd>) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                betInfoRepository.getBetInfo(oddsList)
            }
            result?.success?.let {
                if (it) {
                    _betInfoList.postValue(betInfoRepository.betList)
                } else {
                    oddsDetailResult.value?.oddsDetailData?.matchOdd?.odds?.forEach { (_, value) ->
                        var odd: org.cxct.sportlottery.network.odds.detail.Odd?
                        betInfoList.value?.let { list ->
                            for (i in list.indices) {
                                betInfoList.value?.get(i)?.matchOdd?.oddsId?.let {
                                    odd = value.odds.find { v -> v.id == it }
                                    odd?.isSelect = false
                                }
                            }
                        }
                    }
                }
                _betInfoResult.postValue(result)
            }
        }
    }

    fun getBetInfoListForParlay() {
        val list: MutableList<Odd> = mutableListOf()
        betInfoRepository.betList.let {
            for (i in it.indices) {
                list.add(Odd(it[i].matchOdd.oddsId, it[i].matchOdd.odds))
            }
        }
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                betInfoRepository.getBetInfoList(list)
            }
            _betInfoResult.postValue(result)

        }
    }


    fun removeBetInfoItem(oddId: String) {
        betInfoRepository.removeItem(oddId)
        _betInfoList.postValue(betInfoRepository.betList)
    }

    fun removeBetInfoItemAndRefresh(oddId: String) {
        removeBetInfoItem(oddId)
        getBetInfoListForParlay()
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
                        betInfoList.value?.let { list ->
                            for (i in list.indices) {
                                odd = value.odds.find { v ->
                                    //server可能會回傳null
                                    v.id.let { id -> id == betInfoList.value?.get(i)?.matchOdd?.oddsId }
                                }
                                odd?.isSelect = true
                            }
                        }
                        list.add(
                            OddsDetailListData(
                                key,
                                TextUtil.split(value.typeCodes),
                                value.name,
                                value.odds,
                                false
                            )
                        )
                    }
                    _oddsDetailList.postValue(list)
                }
            }
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

    fun getUserInfo() {
        viewModelScope.launch {
            userInfoRepository.getUserInfo()
        }
    }

    fun sayHello(): String? {
        val hour = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
        return when {
            hour < 6 -> androidContext.getString(R.string.good_midnight) + ", "
            hour < 9 -> androidContext.getString(R.string.good_morning) + ", "
            hour < 12 -> androidContext.getString(R.string.good_beforenoon) + ", "
            hour < 14 -> androidContext.getString(R.string.good_noon) + ", "
            hour < 17 -> androidContext.getString(R.string.good_afternoon) + ", "
            hour < 19 -> androidContext.getString(R.string.good_evening) + ", "
            hour < 22 -> androidContext.getString(R.string.good_night) + ", "
            hour < 24 -> androidContext.getString(R.string.good_dreams) + ", "
            else -> null
        }
    }
}