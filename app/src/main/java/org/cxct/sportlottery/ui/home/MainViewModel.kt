package org.cxct.sportlottery.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.match.MatchPreloadRequest
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.SportMenuRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import timber.log.Timber


class MainViewModel(private val loginRepository: LoginRepository, private val sportMenuRepository: SportMenuRepository) : BaseViewModel() {
    val token: LiveData<String?> by lazy {
        loginRepository.token
    }

    val messageListResult: LiveData<MessageListResult>
        get() = _messageListResult

    val sportMenuResult: LiveData<SportMenuResult>
        get() = _sportMenuResult

    val matchPreloadEarly: LiveData<MatchPreloadResult>
        get() = _matchPreloadEarly

    val matchPreloadInPlay: LiveData<MatchPreloadResult>
        get() = _matchPreloadInPlay

    val matchPreloadToday: LiveData<MatchPreloadResult>
        get() = _matchPreloadToday

    private val _messageListResult = MutableLiveData<MessageListResult>()
    private val _sportMenuResult = MutableLiveData<SportMenuResult>()
    private val _matchPreloadEarly = MutableLiveData<MatchPreloadResult>()
    private val _matchPreloadInPlay = MutableLiveData<MatchPreloadResult>()
    private val _matchPreloadToday = MutableLiveData<MatchPreloadResult>()

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


    fun logout() {
        viewModelScope.launch {
            val result = doNetwork {
                loginRepository.logout()
            }

            //TODO change timber to actual logout ui to da
            Timber.d("logout result is ${result.success} ${result.code} ${result.msg}")
        }
    }

    //獲取系統公告
    fun getAnnouncement() {
        val messageType = "1"
        viewModelScope.launch {
            val result = doNetwork {
                OneBoSportApi.messageService.getMessageList(messageType)
            }
            _messageListResult.postValue(result)
        }
    }

    //獲取體育菜單
    fun getSportMenu() {
        viewModelScope.launch {
            val result = doNetwork {
                sportMenuRepository.getSportMenu()
            }

            val asStartCount = result?.sportMenuData?.atStart?.sumBy { it.num } ?: 0
            _asStartCount.postValue(asStartCount)
            _allFootballCount.postValue(getAllGameCount("FT", result))
            _allBasketballCount.postValue(getAllGameCount("BK", result))
            _allTennisCount.postValue(getAllGameCount("TN", result))
            _allBadmintonCount.postValue(getAllGameCount("BM", result))
            _allVolleyballCount.postValue(getAllGameCount("VB", result))

            result.sportMenuData?.let {
                initSportMenuSelectedState(it)
            }

            _sportMenuResult.postValue(result)
        }
    }

    private fun initSportMenuSelectedState(sportMenuData: SportMenuData) {
        sportMenuData.inPlay.map { sport ->
            sport.isSelected = (sportMenuData.inPlay.indexOf(sport) == 0)
        }
        sportMenuData.today.map { sport ->
            sport.isSelected = (sportMenuData.today.indexOf(sport) == 0)
        }
        sportMenuData.early.map { sport ->
            sport.isSelected = (sportMenuData.early.indexOf(sport) == 0)
        }
        sportMenuData.parlay.map { sport ->
            sport.isSelected = (sportMenuData.parlay.indexOf(sport) == 0)
        }
    }

    //按赛事类型预加载各体育赛事
    fun getMatchPreload() {
        viewModelScope.launch {
            val resultEarly = doNetwork {
                OneBoSportApi.matchService.getMatchPreload(
                    MatchPreloadRequest("EARLY")
                )
            }

            val resultInPlay = doNetwork {
                OneBoSportApi.matchService.getMatchPreload(
                    MatchPreloadRequest("INPLAY")
                )
            }

            val resultToday = doNetwork {
                OneBoSportApi.matchService.getMatchPreload(
                    MatchPreloadRequest("TODAY")
                )
            }

            _matchPreloadEarly.postValue(resultEarly)
            _matchPreloadInPlay.postValue(resultInPlay)
            _matchPreloadToday.postValue(resultToday)
        }
    }

    fun getLeagueList(matchType: MatchType, sport: Sport) {
        updateSportSelectedState(matchType, sport)
        getLeagueList(matchType)
    }

    fun getLeagueList(matchType: MatchType) {
        var gameType: String? = null

        when (matchType) {
            MatchType.IN_PLAY -> {
                gameType = _sportMenuResult.value?.sportMenuData?.inPlay?.find {
                    it.isSelected
                }?.code
            }
            MatchType.TODAY -> {
                gameType = _sportMenuResult.value?.sportMenuData?.today?.find {
                    it.isSelected
                }?.code
            }
            MatchType.EARLY -> {
                gameType = _sportMenuResult.value?.sportMenuData?.early?.find {
                    it.isSelected
                }?.code
            }
            MatchType.PARLAY -> {
                gameType = _sportMenuResult.value?.sportMenuData?.parlay?.find {
                    it.isSelected
                }?.code
            }
            else -> {
            }
        }


        Timber.d("post $gameType ${matchType.postValue}")
    }

    private fun updateSportSelectedState(matchType: MatchType, sport: Sport) {
        val result = _sportMenuResult.value

        when (matchType) {
            MatchType.IN_PLAY -> {
                result?.sportMenuData?.inPlay?.map {
                    it.isSelected = (it == sport)
                }
            }
            MatchType.TODAY -> {
                result?.sportMenuData?.today?.map {
                    it.isSelected = (it == sport)
                }
            }
            MatchType.EARLY -> {
                result?.sportMenuData?.early?.map {
                    it.isSelected = (it == sport)
                }
            }
            MatchType.PARLAY -> {
                result?.sportMenuData?.parlay?.map {
                    it.isSelected = (it == sport)
                }
            }
            else -> {
            }
        }

        _sportMenuResult.postValue(result)
    }

    private fun getAllGameCount(goalCode: String, sportMenuResult: SportMenuResult?): Int {
        val inPlayCount = sportMenuResult?.sportMenuData?.inPlay?.find { it.code == goalCode }?.num ?: 0
        val todayCount = sportMenuResult?.sportMenuData?.today?.find { it.code == goalCode }?.num ?: 0
        val earlyCount = sportMenuResult?.sportMenuData?.early?.find { it.code == goalCode }?.num ?: 0
        val parlayCount = sportMenuResult?.sportMenuData?.parlay?.find { it.code == goalCode }?.num ?: 0
        val atStartCount = sportMenuResult?.sportMenuData?.atStart?.find { it.code == goalCode }?.num ?: 0

        return inPlayCount + todayCount + earlyCount + parlayCount + atStartCount
    }
}