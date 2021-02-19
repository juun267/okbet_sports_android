package org.cxct.sportlottery.ui.game

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel

class GameViewModel(
    private val androidContext: Context,
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

    private val _messageListResult = MutableLiveData<List<String>>()

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
                        it.title + " - " + it.content
                    }?.let {
                        messageList.addAll(it)
                    }
                }
                _messageListResult.postValue(messageList)

            }
        }
    }
}