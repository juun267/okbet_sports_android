package org.cxct.sportlottery.ui.game

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
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

    private fun checkToken() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.checkToken()
            }
        }
    }
}