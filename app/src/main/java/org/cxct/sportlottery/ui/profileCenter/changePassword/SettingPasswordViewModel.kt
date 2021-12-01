package org.cxct.sportlottery.ui.profileCenter.changePassword

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.user.updateFundPwd.UpdateFundPwdRequest
import org.cxct.sportlottery.network.user.updateFundPwd.UpdateFundPwdResult
import org.cxct.sportlottery.network.user.updatePwd.UpdatePwdRequest
import org.cxct.sportlottery.network.user.updatePwd.UpdatePwdResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.MD5Util
import org.cxct.sportlottery.util.VerifyConstUtil

class SettingPasswordViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {

    private val _updatePwdResult = MutableLiveData<UpdatePwdResult?>()
    private val _updateFundPwdResult = MutableLiveData<UpdateFundPwdResult?>()
    private val _currentPwdError = MutableLiveData<String>()
    private val _newPwdError = MutableLiveData<String>()
    private val _confirmPwdError = MutableLiveData<String>()

    val updatePwdResult: LiveData<UpdatePwdResult?>
        get() = _updatePwdResult
    val updateFundPwdResult: LiveData<UpdateFundPwdResult?>
        get() = _updateFundPwdResult
    val currentPwdError: LiveData<String>
        get() = _currentPwdError
    val newPwdError: LiveData<String>
        get() = _newPwdError
    val confirmPwdError: LiveData<String>
        get() = _confirmPwdError


    fun checkInputField(pwdPage: SettingPasswordActivity.PwdPage, currentPwd: String, newPwd: String, confirmPwd: String) {
        checkCurrentPwd(currentPwd)
        checkNewPwd(pwdPage, currentPwd, newPwd)
        checkConfirmPwd(newPwd, confirmPwd)

        if (checkInputFieldVerify()) {
            when (pwdPage) {
                SettingPasswordActivity.PwdPage.LOGIN_PWD -> createUpdatePwdRequest(currentPwd, newPwd)?.let { request -> updatePwd(request) }
                SettingPasswordActivity.PwdPage.BANK_PWD -> createUpdateFunPwd(currentPwd, newPwd)?.let { updateFundPwd(it) }
            }
        }
    }

    private fun createUpdatePwdRequest(currentPwd: String, newPwd: String): UpdatePwdRequest? {
        return UpdatePwdRequest(
            userId = userInfo.value?.userId ?: return null,
            platformId = userInfo.value?.platformId ?: return null,
            oldPassword = MD5Util.MD5Encode(currentPwd),
            newPassword = MD5Util.MD5Encode(newPwd)
        )
    }

    private fun createUpdateFunPwd(currentPwd: String, newPwd: String): UpdateFundPwdRequest? {
        return UpdateFundPwdRequest(
            userId = userInfo.value?.userId ?: return null,
            platformId = userInfo.value?.platformId ?: return null,
            oldPassword = MD5Util.MD5Encode(currentPwd),
            newPassword = MD5Util.MD5Encode(newPwd)
        )
    }

    private fun updatePwd(updatePwdRequest: UpdatePwdRequest) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.userService.updatePwd(updatePwdRequest)
            }

            _updatePwdResult.postValue(result)
        }
    }

    private fun updateFundPwd(updateFundPwdRequest: UpdateFundPwdRequest) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.userService.updateFundPwd(updateFundPwdRequest)
            }

            if (result?.success == true)
                userInfoRepository.updatePayPwFlag(updateFundPwdRequest.userId)

            _updateFundPwdResult.postValue(result)
        }
    }

    private fun checkInputFieldVerify(): Boolean {
        if (currentPwdError.value != "")
            return false
        if (newPwdError.value != "")
            return false
        if (confirmPwdError.value != "")
            return false
        return true
    }

    fun checkCurrentPwd(currentPwd: String) {
        _currentPwdError.value = when {
            currentPwd.isBlank() -> androidContext.getString(R.string.error_input_empty)
            else -> ""
        }
    }

    fun checkNewPwd(pwdPage: SettingPasswordActivity.PwdPage, currentPwd: String, newPwd: String) {
        _newPwdError.value = when {
            newPwd.isBlank() -> androidContext.getString(R.string.error_input_empty)
            pwdPage == SettingPasswordActivity.PwdPage.LOGIN_PWD -> when {
                !VerifyConstUtil.verifyPwdFormat(newPwd) -> androidContext.getString(R.string.error_password_format)
                newPwd.length !in 6..20 -> androidContext.getString(R.string.error_register_password)
                !VerifyConstUtil.verifyPwd(newPwd) -> androidContext.getString(R.string.error_incompatible_format)
                else -> ""
            }
            pwdPage == SettingPasswordActivity.PwdPage.BANK_PWD && !VerifyConstUtil.verifyPayPwd(newPwd) -> androidContext.getString(R.string.error_withdrawal_pwd)
            currentPwd == newPwd -> androidContext.getString(R.string.error_password_cannot_be_same)
            else -> ""
        }
    }

    fun checkConfirmPwd(newPwd: String, confirmPwd: String) {
        _confirmPwdError.value = when {
            confirmPwd.isBlank() -> androidContext.getString(R.string.error_input_empty)
            newPwd != confirmPwd -> androidContext.getString(R.string.error_confirm_password)
            else -> ""
        }
    }

}