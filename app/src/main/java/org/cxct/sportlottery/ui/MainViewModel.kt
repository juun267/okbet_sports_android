package org.cxct.sportlottery.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.login.NAME_LOGIN
import org.cxct.sportlottery.util.LanguageManager

class MainViewModel(private val application: Application) : ViewModel() {
    val token: LiveData<String?> by lazy {
        loginRepository.token
    }

    private val loginRepository by lazy {
        LoginRepository(
            application.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
        )
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
            val xLang = LanguageManager.getSelectLanguage(application).key
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

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(application) as T
            }
            throw IllegalAccessException("Unable to construct view model")
        }
    }
}