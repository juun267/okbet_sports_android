package org.cxct.sportlottery.ui

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.util.LanguageManager

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
    fun getAnnouncement(xLang: String) {
        viewModelScope.launch {
            val token = loginRepository.token.value
            val messageType = "1" //消息类型 1:系统公告 2:赛事公告
            val messageResponse = OneBoSportApi.messageService.getMessageList(
                token, xLang, messageType
            )

            if (messageResponse.isSuccessful) {
                Log.e("simon test", messageResponse.message())
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