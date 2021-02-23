package org.cxct.sportlottery.ui.profileCenter.changePassword

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.user.updateFundPwd.UpdateFundPwdRequest
import org.cxct.sportlottery.network.user.updateFundPwd.UpdateFundPwdResult
import org.cxct.sportlottery.network.user.updatePwd.UpdatePwdRequest
import org.cxct.sportlottery.network.user.updatePwd.UpdatePwdResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import org.cxct.sportlottery.util.VerifyConstUtil

class SettingPasswordViewModel(
    private val androidContext: Context,
    private val userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository
) : BaseOddButtonViewModel(loginRepository, betInfoRepository) {

    private val _passwordFormState = MutableLiveData<PasswordFormState>()
    private val _updatePwdResult = MutableLiveData<UpdatePwdResult?>()
    private val _updateFundPwdResult = MutableLiveData<UpdateFundPwdResult?>()

    val passwordFormState: LiveData<PasswordFormState>
        get() = _passwordFormState
    val updatePwdResult: LiveData<UpdatePwdResult?>
        get() = _updatePwdResult
    val updateFundPwdResult: LiveData<UpdateFundPwdResult?>
        get() = _updateFundPwdResult

    val userInfo: LiveData<UserInfo?> = userInfoRepository.userInfo.asLiveData()

    fun checkInputField(pwdPage: SettingPasswordActivity.PwdPage, context: Context, currentPwd: String, newPwd: String, confirmPwd: String): Boolean {
        val currentPwdError = checkCurrentPwd(context, currentPwd)
        val newPwdError = checkNewPwd(pwdPage, context, currentPwd, newPwd)
        val confirmPwdError = checkConfirmPwd(context, newPwd, confirmPwd)
        val isDataValid = currentPwdError == null && newPwdError == null && confirmPwdError == null
        _passwordFormState.value = PasswordFormState(
            currentPwdError = currentPwdError,
            newPwdError = newPwdError,
            confirmPwdError = confirmPwdError
        )
        return isDataValid
    }

    fun updatePwd(updatePwdRequest: UpdatePwdRequest) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.userService.updatePwd(updatePwdRequest)
            }

            _updatePwdResult.postValue(result)
        }
    }

    fun updateFundPwd(updateFundPwdRequest: UpdateFundPwdRequest) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.userService.updateFundPwd(updateFundPwdRequest)
            }

            if (result?.success == true)
                userInfoRepository.updatePayPwFlag(updateFundPwdRequest.userId)

            _updateFundPwdResult.postValue(result)
        }
    }

    private fun checkCurrentPwd(context: Context, currentPwd: String): String? {
        return when {
            currentPwd.isBlank() -> context.getString(R.string.error_input_empty)
            else -> null
        }
    }

    private fun checkNewPwd(pwdPage: SettingPasswordActivity.PwdPage, context: Context, currentPwd: String, newPwd: String): String? {
        return when {
            newPwd.isBlank() -> context.getString(R.string.error_input_empty)
            pwdPage == SettingPasswordActivity.PwdPage.LOGIN_PWD -> when {
                !VerifyConstUtil.verifyPwdFormat(newPwd) -> context.getString(R.string.error_password_format)
                newPwd.length !in 6..20 -> context.getString(R.string.error_register_password)
                !VerifyConstUtil.verifyPwd(newPwd) -> context.getString(R.string.error_incompatible_format)
                else -> null
            }
            pwdPage == SettingPasswordActivity.PwdPage.BANK_PWD && !VerifyConstUtil.verifyPayPwd(newPwd) -> context.getString(R.string.error_withdrawal_pwd)
            currentPwd == newPwd -> context.getString(R.string.error_password_cannot_be_same)
            else -> null
        }
    }

    private fun checkConfirmPwd(context: Context, newPwd: String, confirmPwd: String): String? {
        return when {
            newPwd != confirmPwd -> context.getString(R.string.error_confirm_password)
            else -> null
        }
    }

}