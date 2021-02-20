package org.cxct.sportlottery.ui.game

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
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


    private val _messageListResult = MutableLiveData<List<String>>()

    private val _countInPlay = MutableLiveData<Int>().apply { value = 0 }
    private val _countToday = MutableLiveData<Int>().apply { value = 0 }
    private val _countEarly = MutableLiveData<Int>().apply { value = 0 }
    private val _countParlay = MutableLiveData<Int>().apply { value = 0 }
    private val _countOutright = MutableLiveData<Int>().apply { value = 0 }
    private val _countAll = MutableLiveData<Int>().apply { value = 0 }


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

                _countInPlay.value = (result.sportMenuData?.menu?.inPlay?.items?.sumBy { it.num }
                    ?: 0)
                _countToday.value = (result.sportMenuData?.menu?.today?.items?.sumBy { it.num }
                    ?: 0)
                _countEarly.value = (result.sportMenuData?.menu?.early?.items?.sumBy { it.num }
                    ?: 0)
                _countParlay.value = (result.sportMenuData?.menu?.parlay?.items?.sumBy { it.num }
                    ?: 0)
                _countOutright.value =
                    (result.sportMenuData?.menu?.outright?.items?.sumBy { it.num }
                        ?: 0)
                _countAll.value = (
                        (_countInPlay.value ?: 0) + (_countToday.value ?: 0) + (_countEarly.value
                            ?: 0) + (_countParlay.value ?: 0) + (_countOutright.value ?: 0)
                        )
            }
        }
    }
}