package org.cxct.sportlottery.ui.profileCenter.nickname

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.user.nickname.NicknameRequest
import org.cxct.sportlottery.network.user.nickname.NicknameResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.FLAG_NICKNAME_IS_SET
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import org.cxct.sportlottery.util.VerifyConstUtil

class NicknameModel(
    private val androidContext: Context,
    private val userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository
) : BaseOddButtonViewModel(loginRepository, betInfoRepository) {

    private val _nicknameFormState = MutableLiveData<NicknameFormState>()
    private val _nicknameResult = MutableLiveData<NicknameResult?>()

    val nicknameFormState: LiveData<NicknameFormState>
        get() = _nicknameFormState
    val nicknameResult: LiveData<NicknameResult?>
        get() = _nicknameResult

    val userInfo: LiveData<UserInfo?> = userInfoRepository.userInfo.asLiveData()

    fun nicknameDataChanged(context: Context, nickname: String): Boolean {
        val nicknameError = checkNickname(context, nickname)
        _nicknameFormState.value = NicknameFormState(nicknameError)

        return nicknameError == null
    }

    fun editNickName(nickname: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.userService.editNickname(NicknameRequest(nickname))
            }

            if (result?.success == true) {
                val userId = userInfoRepository.userInfo.firstOrNull()?.userId?: -1
                userInfoRepository.updateNickname(userId, nickname)
                userInfoRepository.updateSetted(userId, FLAG_NICKNAME_IS_SET)
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