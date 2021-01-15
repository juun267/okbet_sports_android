package org.cxct.sportlottery.ui.profileCenter

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.user.nickname.NicknameRequest
import org.cxct.sportlottery.network.user.nickname.NicknameResult
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.VerifyConstUtil

class ProfileModel(
    private val androidContext: Context
) : BaseViewModel() {

    private val _nicknameFormState = MutableLiveData<NicknameFormState>()
    private val _nicknameResult = MutableLiveData<NicknameResult?>()

    val nicknameFormState: LiveData<NicknameFormState>
        get() = _nicknameFormState
    val nicknameResult: LiveData<NicknameResult?>
        get() = _nicknameResult

    fun nicknameDataChanged(context: Context, nickname: String) {
        val nicknameError = checkNickname(context, nickname)
        val isDataValid = nicknameError == null
        _nicknameFormState.value = NicknameFormState(nicknameError, isDataValid)
    }

    fun editNickName(nickname: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.userService.editNickname(NicknameRequest(nickname))
            }
            _nicknameResult.postValue(result)
        }
    }

    private fun checkNickname(context: Context, nickname: String): String? {
        return when {
            nickname.isBlank() -> context.getString(R.string.error_nickname_empty)
            !VerifyConstUtil.verifyNickname(nickname) -> context.getString(R.string.error_character_not_match)
            else -> null
        }
    }
}