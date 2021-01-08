package org.cxct.sportlottery.ui.home


import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.league.LeagueListRequest
import org.cxct.sportlottery.network.league.LeagueListResult
import org.cxct.sportlottery.network.match.MatchPreloadRequest
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.odds.list.OddsListRequest
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.SportMenuRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
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

    val oddsListResult: LiveData<OddsListResult>
        get() = _oddsListResult

    val leagueListResult: LiveData<LeagueListResult>
        get() = _leagueListResult

    val curPlayType: LiveData<PlayType>
        get() = _curPlayType

    val curDateEarly: LiveData<List<Pair<String, Boolean>>>
        get() = _curDateEarly

    val curOddsDetailParams: LiveData<List<String?>>
        get() = _curOddsDetailParams

    private val _messageListResult = MutableLiveData<MessageListResult>()
    private val _sportMenuResult = MutableLiveData<SportMenuResult>()
    private val _matchPreloadInPlay = MutableLiveData<MatchPreloadResult>()
    private val _matchPreloadToday = MutableLiveData<MatchPreloadResult>()
    private val _oddsListResult = MutableLiveData<OddsListResult>()
    private val _leagueListResult = MutableLiveData<LeagueListResult>()
    private val _curPlayType = MutableLiveData<PlayType>().apply {
        value = PlayType.OU_HDP
    }
    private val _curDateEarly = MutableLiveData<List<Pair<String, Boolean>>>()
    private val _curOddsDetailParams = MutableLiveData<List<String?>>()

    private val _asStartCount = MutableLiveData<Int>()
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
            _allFootballCount.postValue(getAllGameCount("FT", result))
            _allBasketballCount.postValue(getAllGameCount("BK", result))
            _allTennisCount.postValue(getAllGameCount("TN", result))
            _allBadmintonCount.postValue(getAllGameCount("BM", result))
            _allVolleyballCount.postValue(getAllGameCount("VB", result))

            result?.let {
                if (it.sportMenuData != null)
                    initSportMenuSelectedState(it.sportMenuData)
                _sportMenuResult.postValue(it)
            }
        }
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
    }

    fun getInPlayMatchPreload() {
        viewModelScope.launch {
            doNetwork {
                OneBoSportApi.matchService.getMatchPreload(
                    MatchPreloadRequest("INPLAY")
                )
            }?.let { result ->
                _matchPreloadInPlay.postValue(result)
            }
        }
    }

    fun getLeagueList(matchType: MatchType, item: Item, timeRangeParams: TimeRangeParams) {
        updateSportSelectedState(matchType, item)
        getLeagueList(matchType, timeRangeParams)
    }

    fun getLeagueList(matchType: MatchType, timeRangeParams: TimeRangeParams) {
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
                    getLeagueList(gameType, matchType.postValue, timeRangeParams)
                }
            }
            MatchType.EARLY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.early?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getLeagueList(gameType, matchType.postValue, timeRangeParams)
                }
            }
            MatchType.PARLAY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getLeagueList(gameType, matchType.postValue, timeRangeParams)
                }
            }
            MatchType.OUTRIGHT -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.outright?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    //TODO outright odd list api
                    Timber.i("get outright list")
                }
            }
            else -> {
            }
        }
    }

    fun getLeagueOddsList(matchType: MatchType, leagueId: String, timeRangeParams: TimeRangeParams) {
//        val leagueIdList: List<Int>? = null
        val leagueIdList: MutableList<String> by lazy {
            mutableListOf(leagueId)
        }

        when (matchType) {
            MatchType.TODAY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.today?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getLeagueOddsList(gameType, matchType.postValue, leagueIdList, timeRangeParams)
                }
            }
            MatchType.EARLY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.early?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getLeagueOddsList(gameType, matchType.postValue, leagueIdList, timeRangeParams)
                }
            }
            MatchType.PARLAY -> {
                val gameType = _sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.find {
                    it.isSelected
                }?.code

                gameType?.let {
                    getLeagueOddsList(gameType, matchType.postValue, leagueIdList, timeRangeParams)
                }
            }
            else -> {
            }
        }
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
            else -> {
            }
        }

        _sportMenuResult.postValue(result)
    }

    fun updateDateSelectedState(string: String) {
        val dateEarly: MutableList<Pair<String, Boolean>> = mutableListOf()

        _curDateEarly.value?.forEach {
            dateEarly.add(Pair(it.first, (it.first == string)))
        }
        _curDateEarly.postValue(dateEarly)
    }

    private fun getOddsList(gameType: String, matchType: String) {
        viewModelScope.launch {
            val result = doNetwork {
                OneBoSportApi.oddsService.getOddsList(
                    OddsListRequest(
                        gameType,
                        matchType,
                    )
                )
            }
            _oddsListResult.postValue(result)
        }
    }

    private fun getLeagueOddsList(gameType: String, matchType: String, leagueIdList: List<String>, timeRangeParams: TimeRangeParams) {
        viewModelScope.launch {
            val result = doNetwork {
                OneBoSportApi.oddsService.getOddsList(
                    OddsListRequest(
                        gameType,
                        matchType,
                        leagueIdList = leagueIdList,
                        startTime = timeRangeParams.startTime,
                        endTime = timeRangeParams.endTime
                    )
                )
            }
            _oddsListResult.postValue(result)
        }
    }

    private fun getLeagueList(gameType: String, matchType: String, timeRangeParams: TimeRangeParams) {
        viewModelScope.launch {
            val result = doNetwork {
                OneBoSportApi.leagueService.getLeagueList(
                    LeagueListRequest(gameType, matchType, startTime = timeRangeParams.startTime, endTime = timeRangeParams.endTime)
                )
            }
            _leagueListResult.postValue(result)
        }
    }

    fun setPlayType(playType: PlayType) {
        _curPlayType.postValue(playType)
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

    fun getMoney() {
        viewModelScope.launch {
            val userMoneyResult = doNetwork {
                OneBoSportApi.userService.getMoney()
            }
            _userMoney.postValue(userMoneyResult?.money)
        }
    }

    fun getEarlyDateRow() {
        val oneWeekDate = TimeUtil.getOneWeekDate().toMutableList()
        oneWeekDate.add(androidContext.getString(R.string.date_row_other))

        val dateEarly = oneWeekDate.map {
            Pair(it, oneWeekDate.indexOf(it) == 0)
        }

        _curDateEarly.postValue(dateEarly)
    }

    fun getOddsDetail(oddId: String) {
        val item = _sportMenuResult.value?.sportMenuData?.menu?.inPlay?.items?.find {
            it.isSelected
        }
        _curOddsDetailParams.postValue(listOf(item?.code,item?.name,oddId))
    }
}