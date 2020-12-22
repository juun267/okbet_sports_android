package org.cxct.sportlottery.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.error.ErrorUtils
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseViewModel

class MainViewModel(private val loginRepository: LoginRepository) : BaseViewModel() {
    val token: LiveData<String?> by lazy {
        loginRepository.token
    }

    fun logout() {
        loginRepository.logout()
    }

    //獲取系統公告
    fun getAnnouncement() {
        viewModelScope.launch {
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
        }
    }

    //獲取體育菜單
    fun getSportMenu() {
        viewModelScope.launch {
            val sportMenuResponse = OneBoSportApi.sportService.getMenu()

            if (sportMenuResponse.isSuccessful) {
                mBaseResult.postValue(sportMenuResponse.body())
            } else {
                val result = ErrorUtils.parseError(sportMenuResponse)
                mBaseResult.postValue(result)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}