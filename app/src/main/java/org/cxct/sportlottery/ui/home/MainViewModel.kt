package org.cxct.sportlottery.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.error.ErrorUtils
import org.cxct.sportlottery.network.match.MatchPreloadRequest
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.SportMenuRepository
import org.cxct.sportlottery.ui.base.BaseViewModel


class MainViewModel(
    private val loginRepository: LoginRepository,
    private val sportMenuRepository: SportMenuRepository
) : BaseViewModel() {
    val token: LiveData<String?> by lazy {
        loginRepository.token
    }

    private val _asStartCount = MutableLiveData<Int>()
    val asStartCount: LiveData<Int> //即將開賽的數量
        get() = _asStartCount

    fun logout() {
        loginRepository.logout()
    }

    //獲取系統公告
    fun getAnnouncement() {
        viewModelScope.launch {
            try {
                val messageType = "1" //消息类型 1:系统公告 2:赛事公告
                val messageResponse = OneBoSportApi.messageService.getMessageList(
                    messageType
                )

                if (messageResponse.isSuccessful) {
                    mBaseResult.postValue(messageResponse.body())
                } else {
                    val result = ErrorUtils.parseError(messageResponse)
                    mBaseResult.postValue(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                //TODO simon test review API error handling
            }
        }
    }

    //獲取體育菜單
    fun getSportMenu() {
        viewModelScope.launch {
            try {
                val sportMenuResult = sportMenuRepository.getSportMenu()
                mBaseResult.postValue(sportMenuResult)

                val count = sportMenuResult?.sportMenuData?.atStart?.sumBy { it.num } ?: 0
                _asStartCount.postValue(count)
            } catch (e: Exception) {
                e.printStackTrace()
                //TODO simon test review API error handling
            }
        }
    }

    //按赛事类型预加载各体育赛事
    fun getMatchPreload() {
        viewModelScope.launch {
            try {
                val earlyRequest = MatchPreloadRequest("EARLY")
                val earlyResponse = OneBoSportApi.matchService.getMatchPreload(earlyRequest)
                if (earlyResponse.isSuccessful) {
                    mBaseResult.postValue(earlyResponse.body())
                } else {
                    val result = ErrorUtils.parseError(earlyResponse)
                    mBaseResult.postValue(result)
                }

                val inPlayRequest = MatchPreloadRequest("INPLAY")
                val inPlayResponse = OneBoSportApi.matchService.getMatchPreload(inPlayRequest)
                if (inPlayResponse.isSuccessful) {
                    mBaseResult.postValue(inPlayResponse.body())
                } else {
                    val result = ErrorUtils.parseError(inPlayResponse)
                    mBaseResult.postValue(result)
                }

                val todayRequest = MatchPreloadRequest("TODAY")
                val todayResponse = OneBoSportApi.matchService.getMatchPreload(todayRequest)
                if (todayResponse.isSuccessful) {
                    mBaseResult.postValue(todayResponse.body())
                } else {
                    val result = ErrorUtils.parseError(todayResponse)
                    mBaseResult.postValue(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                //TODO simon test review API error handling
            }
        }
    }
}