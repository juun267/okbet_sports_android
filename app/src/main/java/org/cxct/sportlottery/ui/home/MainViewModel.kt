package org.cxct.sportlottery.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.repository.LoginRepository

class MainViewModel(private val loginRepository: LoginRepository) : ViewModel() {
    val token: LiveData<String?> by lazy {
        loginRepository.token
    }

    val messageListResult: LiveData<MessageListResult?>
        get() = _messageListResult

    private val _messageListResult = MutableLiveData<MessageListResult?>()

    init {
    }

    fun logout() {
        loginRepository.logout()
    }

    //獲取系統公告
    fun getAnnouncement() {
        viewModelScope.launch {
            val token = loginRepository.token.value
            val messageType = "1" //消息类型 1:系统公告 2:赛事公告
            val messageResponse = OneBoSportApi.messageService.getMessageList(
                token, messageType
            )

            if (messageResponse.isSuccessful) {
                _messageListResult.postValue(messageResponse.body())
            } else {
                val errorBody = messageResponse.errorBody()
                val errorResult = MessageListResult(-1, errorBody.toString(), mutableListOf(), false, 0)
                _messageListResult.postValue(errorResult)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}