package org.cxct.sportlottery.ui.base

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.repository.LoginRepository

abstract class BaseSocketViewModel(val loginRepository: LoginRepository) : BaseViewModel() {

    init {
        if (loginRepository.isLogin.value == true && !loginRepository.isCheckToken) {
            viewModelScope.launch {
                loginRepository.checkToken()
            }
        }
    }
}