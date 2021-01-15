package org.cxct.sportlottery.ui.home


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.league.LeagueListRequest
import org.cxct.sportlottery.network.league.LeagueListResult
import org.cxct.sportlottery.network.match.MatchPreloadRequest
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.odds.list.OddsListRequest
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListRequest
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListResult
import org.cxct.sportlottery.network.outright.odds.Winner
import org.cxct.sportlottery.network.outright.season.OutrightSeasonListRequest
import org.cxct.sportlottery.network.outright.season.OutrightSeasonListResult
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.SportMenuRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.game.data.Date
import org.cxct.sportlottery.ui.home.gameDrawer.GameEntity
import org.cxct.sportlottery.util.TimeUtil
import timber.log.Timber


class MainViewModel(
    private val androidContext: Context,
    private val loginRepository: LoginRepository,
    private val sportMenuRepository: SportMenuRepository
) : BaseViewModel() {
    val token: LiveData<String?> by lazy {
        loginRepository.token
    }

    val messageListResult: LiveData<MessageListResult>
        get() = _messageListResult

    val sportMenuResult: LiveData<SportMenuResult>
        get() = _sportMenuResult

    val matchPreloadInPlay: LiveData<MatchPreloadResult>
        get() = _matchPreloadInPlay

    val matchPreloadToday: LiveData<MatchPreloadResult>
        get() = _matchPreloadToday

    val oddsListGameHallResult: LiveData<OddsListResult>
        get() = _oddsListGameHallResult

    val oddsListResult: LiveData<OddsListResult>
        get() = _oddsListResult

    val leagueListResult: LiveData<LeagueListResult>
        get() = _leagueListResult

    val outrightSeasonListResult: LiveData<OutrightSeasonListResult>
        get() = _outrightSeasonListResult

    val outrightOddsListResult: LiveData<OutrightOddsListResult>
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

    private val _messageListResult = MutableLiveData<MessageListResult>()
    private val _sportMenuResult = MutableLiveData<SportMenuResult>()
    private val _matchPreloadInPlay = MutableLiveData<MatchPreloadResult>()
    private val _matchPreloadToday = MutableLiveData<MatchPreloadResult>()
    private val _oddsListGameHallResult = MutableLiveData<OddsListResult>()
    private val _oddsListResult = MutableLiveData<OddsListResult>()
    private val _leagueListResult = MutableLiveData<LeagueListResult>()
    private val _outrightSeasonListResult = MutableLiveData<OutrightSeasonListResult>()
    private val _outrightOddsListResult = MutableLiveData<OutrightOddsListResult>()
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

    private val _userMoney = MutableLiveData<Double?>()
    val userMoney: LiveData<Double?> //使用者餘額
        get() = _userMoney

    private val _oddsDetailMoreList = MutableLiveData<List<*>>()
    val oddsDetailMoreList: LiveData<List<*>?>
        get() = _oddsDetailMoreList

    fun setOddsDetailMoreList(list: List<*>) {
        _oddsDetailMoreList.postValue(list)
    }

    fun logout() {
        viewModelScope.launch {
            val result = doNetwork {
                loginRepository.logout()
            }

            //TODO change timber to actual logout ui to da
            Timber.d("logout result is ${result?.success} ${result?.code} ${result?.msg}")
        }
    }

    //獲取系統公告
    fun getAnnouncement() {
        val messageType = "1"
        viewModelScope.launch {
            doNetwork {
                OneBoSportApi.messageService.getMessageList(messageType)
            }?.let { result -> _messageListResult.postValue(result) }
        }
    }

    //獲取體育菜單
    fun getSportMenu() {
        val now = TimeUtil.getNowTimeStamp()
        val todayStart = TimeUtil.getTodayStartTimeStamp()

        viewModelScope.launch {
            val result = doNetwork {
                sportMenuRepository.getSportMenu(
                    now.toString(),
                    todayStart.toString()
                )
            }

            val asStartCount = result?.sportMenuData?.atStart?.items?.sumBy { it.num } ?: 0
            _asStartCount.postValue(asStartCount)
            _allFootballCount.postValue(getAllGameCount(SportType.FOOTBALL.code, result))
            _allBasketballCount.postValue(getAllGameCount(SportType.BASKETBALL.code, result))
            _allTennisCount.postValue(getAllGameCount(SportType.TENNIS.code, result))
            _allBadmintonCount.postValue(getAllGameCount(SportType.BADMINTON.code, result))
            _allVolleyballCount.postValue(getAllGameCount(SportType.VOLLEYBALL.code, result))

            result?.let {
                if (it.sportMenuData != null)
                    initSportMenuSelectedState(it.sportMenuData)
                _sportMenuResult.postValue(it)
            }
        }
    }

    private fun getAllGameCount(goalCode: String, sportMenuResult: SportMenuResult?): Int {
        val inPlayCount =
            sportMenuResult?.sportMenuData?.menu?.inPlay?.items?.find { it.code == goalCode }?.num
                ?: 0
        val todayCount =
            sportMenuResult?.sportMenuData?.menu?.today?.items?.find { it.code == goalCode }?.num
                ?: 0
        val earlyCount =
            sportMenuResult?.sportMenuData?.menu?.early?.items?.find { it.code == goalCode }?.num
                ?: 0
        val parlayCount =
            sportMenuResult?.sportMenuData?.menu?.parlay?.items?.find { it.code == goalCode }?.num
                ?: 0
        val atStartCount =
            sportMenuResult?.sportMenuData?.atStart?.items?.find { it.code == goalCode }?.num ?: 0

        return inPlayCount + todayCount + earlyCount + parlayCount + atStartCount
    }

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
            doNetwork {
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
            val userMoneyResult = doNetwork {
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

    fun getLeagueOddsList(
        matchType: MatchType,
        leagueId: String,
    ) {
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
                val result = doNetwork {
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
            val result = doNetwork {
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
            val result = doNetwork {
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
            val result = doNetwork {
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

}