package org.cxct.sportlottery.ui.profileCenter.changePassword

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.user.updateFundPwd.UpdateFundPwdRequest
import org.cxct.sportlottery.network.user.updatePwd.UpdatePwdRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.MD5Util
import org.cxct.sportlottery.util.VerifyConstUtil

class SettingPasswordViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {

    private val _updatePwdResult = MutableLiveData<NetResult?>()
    private val _updateFundPwdResult = MutableLiveData<NetResult?>()
    private val _currentPwdError = MutableLiveData<String>()
    private val _newPwdError = MutableLiveData<String>()
    private val _confirmPwdError = MutableLiveData<String>()

    val updatePwdResult: LiveData<NetResult?>
        get() = _updatePwdResult
    val updateFundPwdResult: LiveData<NetResult?>
        get() = _updateFundPwdResult
    val currentPwdError: LiveData<String>
        get() = _currentPwdError
    val newPwdError: LiveData<String>
        get() = _newPwdError
    val confirmPwdError: LiveData<String>
        get() = _confirmPwdError

    val submitEnable: LiveData<Boolean>
        get() = _submitEnable
    private val _submitEnable = MutableLiveData<Boolean>()

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
                UserInfoRepository.updatePayPwFlag(updateFundPwdRequest.userId)

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
        if (userInfo.value?.passwordSet == true) {
            _currentPwdError.value = ""
        } else {
            _currentPwdError.value = when {
                currentPwd.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
                else -> ""
            }
        }
        checkInputComplete()
    }

    fun checkNewPwd(pwdPage: SettingPasswordActivity.PwdPage, currentPwd: String, newPwd: String) {
        _newPwdError.value = when {
            newPwd.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            pwdPage == SettingPasswordActivity.PwdPage.LOGIN_PWD -> when {
                !VerifyConstUtil.verifyPwd(newPwd) ->
                    androidContext.getString(R.string.error_password_format)
                else -> ""
            }
            pwdPage == SettingPasswordActivity.PwdPage.BANK_PWD && !VerifyConstUtil.verifyPayPwd(newPwd) -> androidContext.getString(R.string.error_withdraw_password_for_new)
            currentPwd == newPwd -> androidContext.getString(R.string.error_password_cannot_be_same)
            else -> ""
        }
        checkInputComplete()
    }

    fun checkConfirmPwd(newPwd: String, confirmPwd: String) {
        _confirmPwdError.value = when {
            confirmPwd.isEmpty() -> androidContext.getString(R.string.error_input_empty)
            confirmPwd.isBlank() -> androidContext.getString(R.string.error_input_empty)
            newPwd != confirmPwd -> androidContext.getString(R.string.error_confirm_password)
            else -> ""
        }
        checkInputComplete()
    }

    private fun checkInputComplete() {
        _submitEnable.value =
            (userInfo.value?.passwordSet == true || _currentPwdError.value.isNullOrEmpty()) && _newPwdError.value?.isEmpty() == true && _confirmPwdError.value?.isEmpty() == true
    }
    fun getUserInfo() {
        viewModelScope.launch {
            runWithCatch { UserInfoRepository.getUserInfo() }
        }
    }

}