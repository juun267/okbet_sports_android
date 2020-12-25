package org.cxct.sportlottery.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.match.MatchPreloadRequest
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.match.MatchType
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.SportMenuRepository
import org.cxct.sportlottery.ui.base.BaseViewModel


class MainViewModel(private val loginRepository: LoginRepository, private val sportMenuRepository: SportMenuRepository) : BaseViewModel() {
    val token: LiveData<String?> by lazy {
        loginRepository.token
    }

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


    private val _earlyGameResult = MutableLiveData<MatchPreloadResult>()
    val earlyGameResult: LiveData<MatchPreloadResult>
        get() = _earlyGameResult

    private val _inPlayGameResult = MutableLiveData<MatchPreloadResult>()
    val inPlayGameResult: LiveData<MatchPreloadResult>
        get() = _inPlayGameResult

    private val _todayGameResult = MutableLiveData<MatchPreloadResult>()
    val todayGameResult: LiveData<MatchPreloadResult>
        get() = _todayGameResult


    fun logout() {
        loginRepository.logout()
    }

    //獲取系統公告
    fun getAnnouncement(): LiveData<MessageListResult> {
        val messageType = "1"
        return doNetwork {
            OneBoSportApi.messageService.getMessageList(messageType)
        }
    }

    //獲取體育菜單
    fun getSportMenu(): LiveData<SportMenuResult> {
        val sportMenuResult = doNetwork {
            sportMenuRepository.getSportMenu()
        }
        val asStartCount = sportMenuResult.value?.sportMenuData?.atStart?.sumBy { it.num } ?: 0
        _asStartCount.postValue(asStartCount)
        _allFootballCount.postValue(getAllGameCount("FT", sportMenuResult.value))
        _allBasketballCount.postValue(getAllGameCount("BK", sportMenuResult.value))
        _allTennisCount.postValue(getAllGameCount("TN", sportMenuResult.value))
        _allBadmintonCount.postValue(
            getAllGameCount(
                "",
                sportMenuResult.value
            )
        ) //TODO simon test review 不知道羽毛球的 code 是什麼
        _allVolleyballCount.postValue(getAllGameCount("VB", sportMenuResult.value))
        return sportMenuResult
    }

    //按赛事类型预加载各体育赛事
    fun getMatchPreload(matchType: String): LiveData<MatchPreloadResult> {
        val request = MatchPreloadRequest(matchType)

        return doNetwork {
            OneBoSportApi.matchService.getMatchPreload(request)
        }
    }

    private fun getAllGameCount(goalCode: String, sportMenuResult: SportMenuResult?): Int {
        val inPlayCount = sportMenuResult?.sportMenuData?.inPlay?.find { it.code == goalCode }?.num?: 0
        val todayCount = sportMenuResult?.sportMenuData?.today?.find { it.code == goalCode }?.num?: 0
        val earlyCount = sportMenuResult?.sportMenuData?.early?.find { it.code == goalCode }?.num?: 0
        val parlayCount = sportMenuResult?.sportMenuData?.parlay?.find { it.code == goalCode }?.num?: 0
        val atStartCount = sportMenuResult?.sportMenuData?.atStart?.find { it.code == goalCode }?.num?: 0

        return inPlayCount + todayCount + earlyCount + parlayCount + atStartCount
    }
}