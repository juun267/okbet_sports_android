package org.cxct.sportlottery.ui.game

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.SportMenuRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import org.cxct.sportlottery.util.TimeUtil

class GameViewModel(
    private val androidContext: Context,
    private val sportMenuRepository: SportMenuRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository
) : BaseOddButtonViewModel(loginRepository, betInfoRepository) {

    val isLogin: LiveData<Boolean> by lazy {
        loginRepository.isLogin.apply {
            if (this.value == false && !loginRepository.isCheckToken) {
                checkToken()
            }
        }
    }


    val messageListResult: LiveData<List<String>>
        get() = _messageListResult
    val sportMenuResult: LiveData<SportMenuResult>
        get() = _sportMenuResult

    val countInPlay: LiveData<Int>
        get() = _countInPlay
    val countToday: LiveData<Int>
        get() = _countToday
    val countEarly: LiveData<Int>
        get() = _countEarly
    val countParlay: LiveData<Int>
        get() = _countParlay
    val countOutright: LiveData<Int>
        get() = _countOutright
    val countAll: LiveData<Int>
        get() = _countAll

    val countAtStart: LiveData<Int>
        get() = _countAtStart
    val countParlayFootball: LiveData<Int>
        get() = _countParlayFootball
    val countParlayBasketball: LiveData<Int>
        get() = _countParlayBasketball
    val countParlayTennis: LiveData<Int>
        get() = _countParlayTennis
    val countParlayBadminton: LiveData<Int>
        get() = _countParlayBadminton
    val countParlayVolleyball: LiveData<Int>
        get() = _countParlayVolleyball


    private val _messageListResult = MutableLiveData<List<String>>()
    private val _sportMenuResult = MutableLiveData<SportMenuResult>()

    private val _countInPlay = MutableLiveData<Int>().apply { value = 0 }
    private val _countToday = MutableLiveData<Int>().apply { value = 0 }
    private val _countEarly = MutableLiveData<Int>().apply { value = 0 }
    private val _countParlay = MutableLiveData<Int>().apply { value = 0 }
    private val _countOutright = MutableLiveData<Int>().apply { value = 0 }
    private val _countAll = MutableLiveData<Int>().apply { value = 0 }

    private val _countAtStart = MutableLiveData<Int>().apply { value = 0 }
    private val _countParlayFootball = MutableLiveData<Int>().apply { value = 0 }
    private val _countParlayBasketball = MutableLiveData<Int>().apply { value = 0 }
    private val _countParlayTennis = MutableLiveData<Int>().apply { value = 0 }
    private val _countParlayBadminton = MutableLiveData<Int>().apply { value = 0 }
    private val _countParlayVolleyball = MutableLiveData<Int>().apply { value = 0 }


    private fun checkToken() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.checkToken()
            }
        }
    }

    fun getMessage() {
        val messageType = "1"

        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.messageService.getMessageList(messageType)
            }?.let { result ->
                val messageList = mutableListOf<String>()

                if (result.success) {
                    result.rows?.map {
                        it.title + " - " + it.message
                    }?.let {
                        messageList.addAll(it)
                    }
                }
                _messageListResult.postValue(messageList)

            }
        }
    }

    fun getSportMenu() {
        val now = TimeUtil.getNowTimeStamp()
        val todayStart = TimeUtil.getTodayStartTimeStamp()

        viewModelScope.launch {
            doNetwork(androidContext) {
                sportMenuRepository.getSportMenu(
                    now.toString(),
                    todayStart.toString()
                )
            }?.let { result ->
                if (result.success) {
                    updateMatchTypeCount(result)
                    updateParlayCount(result)
                    _sportMenuResult.postValue(result)
                }
            }
        }
    }

    private fun updateMatchTypeCount(sportMenuResult: SportMenuResult) {
        _countInPlay.value =
            sportMenuResult.sportMenuData?.menu?.inPlay?.items?.sumBy { it.num } ?: 0

        _countToday.value = sportMenuResult.sportMenuData?.menu?.today?.items?.sumBy { it.num } ?: 0

        _countEarly.value = sportMenuResult.sportMenuData?.menu?.early?.items?.sumBy { it.num } ?: 0

        _countParlay.value =
            sportMenuResult.sportMenuData?.menu?.parlay?.items?.sumBy { it.num } ?: 0

        _countOutright.value =
            sportMenuResult.sportMenuData?.menu?.outright?.items?.sumBy { it.num } ?: 0

        _countAll.value = (_countInPlay.value ?: 0) + (_countToday.value ?: 0) + (_countEarly.value
            ?: 0) + (_countParlay.value ?: 0) + (_countOutright.value ?: 0)

    }

    private fun updateParlayCount(sportMenuResult: SportMenuResult) {
        _countAtStart.value = sportMenuResult.sportMenuData?.atStart?.num ?: 0
        _countParlayFootball.value =
            sportMenuResult.sportMenuData?.menu?.parlay?.items?.find { it.code == SportType.FOOTBALL.code }?.num
                ?: 0
        _countParlayBasketball.value =
            sportMenuResult.sportMenuData?.menu?.parlay?.items?.find { it.code == SportType.BASKETBALL.code }?.num
                ?: 0
        _countParlayTennis.value =
            sportMenuResult.sportMenuData?.menu?.parlay?.items?.find { it.code == SportType.TENNIS.code }?.num
                ?: 0
        _countParlayBadminton.value =
            sportMenuResult.sportMenuData?.menu?.parlay?.items?.find { it.code == SportType.BADMINTON.code }?.num
                ?: 0
        _countParlayVolleyball.value =
            sportMenuResult.sportMenuData?.menu?.parlay?.items?.find { it.code == SportType.VOLLEYBALL.code }?.num
                ?: 0
    }

    fun selectHomeCard(matchType: MatchType, sportType: SportType?) {
        sportType?.let {
            _sportMenuResult.value?.sportMenuData?.menu?.parlay?.items?.map {
                it.isSelected = (it.code == sportType.code)
            }
        }

        selectMatchType(matchType)
    }

    fun selectMatchType(matchType: MatchType?) {
        val tempResult = _sportMenuResult.value

        tempResult?.sportMenuData?.menu?.inPlay?.isSelect =
            (matchType != null && matchType == MatchType.IN_PLAY)

        tempResult?.sportMenuData?.menu?.today?.isSelect =
            (matchType != null && matchType == MatchType.TODAY)

        tempResult?.sportMenuData?.menu?.early?.isSelect =
            (matchType != null && matchType == MatchType.EARLY)

        tempResult?.sportMenuData?.menu?.parlay?.isSelect =
            (matchType != null && matchType == MatchType.PARLAY)

        tempResult?.sportMenuData?.menu?.outright?.isSelect =
            (matchType != null && matchType == MatchType.OUTRIGHT)

        tempResult?.sportMenuData?.atStart?.isSelect =
            (matchType != null && matchType == MatchType.AT_START)

        tempResult?.let {
            _sportMenuResult.postValue(it)
        }
    }
}