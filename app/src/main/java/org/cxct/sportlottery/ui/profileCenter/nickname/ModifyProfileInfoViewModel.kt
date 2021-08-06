package org.cxct.sportlottery.ui.profileCenter.nickname

import android.app.Application
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
import org.cxct.sportlottery.network.user.setWithdrawInfo.WithdrawInfoRequest
import org.cxct.sportlottery.network.user.setWithdrawInfo.WithdrawInfoResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import org.cxct.sportlottery.util.VerifyConstUtil

class ModifyProfileInfoViewModel(
    androidContext: Application,
    private val userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseOddButtonViewModel(
    androidContext,
    loginRepository,
    betInfoRepository,
    infoCenterRepository
) {

    private val _loading = MutableLiveData<Boolean>()
    private val _nicknameResult = MutableLiveData<NicknameResult?>()
    private val _withdrawInfoResult = MutableLiveData<WithdrawInfoResult?>()
    private val _fullNameErrorMsg = MutableLiveData<String>()
    private val _qqErrorMsg = MutableLiveData<String>()
    private val _eMailErrorMsg = MutableLiveData<String>()
    private val _phoneErrorMsg = MutableLiveData<String>()
    private val _weChatErrorMsg = MutableLiveData<String>()
    private val _nickNameErrorMsg = MutableLiveData<String>()

    val loading: LiveData<Boolean>
        get() = _loading

    //錯誤訊息
    val fullNameErrorMsg: LiveData<String>
        get() = _fullNameErrorMsg
    val qqErrorMsg: LiveData<String>
        get() = _qqErrorMsg
    val eMailErrorMsg: LiveData<String>
        get() = _eMailErrorMsg
    val phoneErrorMsg: LiveData<String>
        get() = _phoneErrorMsg
    val weChatErrorMsg: LiveData<String>
        get() = _weChatErrorMsg
    val nickNameErrorMsg: LiveData<String>
        get() = _nickNameErrorMsg
    val nicknameResult: LiveData<NicknameResult?>
        get() = _nicknameResult
    val withdrawInfoResult: LiveData<WithdrawInfoResult?>
        get() = _withdrawInfoResult


    val userInfo: LiveData<UserInfo?> = userInfoRepository.userInfo.asLiveData()

    fun confirmProfileInfo(modifyType: ModifyType, inputContent: String) {
        if (checkInput(modifyType, inputContent)) {
            //暱稱設定是獨立一隻api
            if (modifyType == ModifyType.NickName) {
                editNickName(inputContent)
            } else {
                setWithdrawInfo(modifyType, inputContent)
            }
        }
    }

    fun checkInput(modifyType: ModifyType, inputContent: String): Boolean {
        return when (modifyType) {
            ModifyType.RealName -> {
                checkFullName(androidContext, inputContent)
                fullNameErrorMsg.value == ""
            }
            ModifyType.QQNumber -> {
                checkQQ(androidContext, inputContent)
                qqErrorMsg.value == ""
            }
            ModifyType.Email -> {
                checkEmail(androidContext, inputContent)
                eMailErrorMsg.value == ""
            }
            ModifyType.WeChat -> {
                checkWeChat(androidContext, inputContent)
                weChatErrorMsg.value == ""
            }
            ModifyType.PhoneNumber -> {
                checkPhone(androidContext, inputContent)
                phoneErrorMsg.value == ""
            }
            ModifyType.NickName -> {
                checkNickname(androidContext, inputContent)
                nickNameErrorMsg.value == ""
            }
        }

    }

    private fun setWithdrawInfo(modifyType: ModifyType, inputContent: String) {
        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.userService.setWithdrawUserInfo(createWithdrawInfoRequest(modifyType, inputContent))
            }?.let { result ->
                if (result.success) {
                    updateUserInfoDao(modifyType, inputContent)
                    _withdrawInfoResult.value = result
                }
            }
        }
    }

    private fun createWithdrawInfoRequest(modifyType: ModifyType, inputContent: String): WithdrawInfoRequest {
        val userId = loginRepository.userId
        return when (modifyType) {
            ModifyType.RealName -> {
                WithdrawInfoRequest(userId = userId, fullName = inputContent)
            }
            ModifyType.QQNumber -> WithdrawInfoRequest(userId = userId, qq = inputContent)
            ModifyType.Email -> WithdrawInfoRequest(userId = userId, email = inputContent)
            ModifyType.WeChat -> WithdrawInfoRequest(userId = userId, wechat = inputContent)
            ModifyType.PhoneNumber -> WithdrawInfoRequest(userId = userId, phone = inputContent)
            else -> WithdrawInfoRequest(userId = userId)
        }
    }

    private suspend fun updateUserInfoDao(modifyType: ModifyType, inputContent: String) {
        userInfoRepository.apply {
            val userId = userInfo.firstOrNull()?.userId ?: -1
            when (modifyType) {
                ModifyType.RealName -> updateFullName(userId, inputContent)
                ModifyType.QQNumber -> updateQQ(userId, inputContent)
                ModifyType.Email -> updateEmail(userId, inputContent)
                ModifyType.WeChat -> updateWeChat(userId, inputContent)
                ModifyType.PhoneNumber -> updatePhone(userId, inputContent)
                ModifyType.NickName -> {
                }
            }
        }
    }

/*    fun nicknameDataChanged(context: Context, nickname: String): Boolean {
        val nicknameError = checkNickname(context, nickname)
        _nicknameFormState.value = NicknameFormState(nicknameError)

        return nicknameError == null
    }*/

    private fun editNickName(nickname: String) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.userService.editNickname(NicknameRequest(nickname))
            }

            if (result?.success == true) {
                val userId = userInfoRepository.userInfo.firstOrNull()?.userId ?: -1
                userInfoRepository.updateNickname(userId, nickname)
                userInfoRepository.updateSetted(userId, FLAG_NICKNAME_IS_SET)
            }

            _nicknameResult.postValue(result)
        }
    }

    private fun checkNickname(context: Context, nickname: String?) {
        _nickNameErrorMsg.value = when {
            nickname.isNullOrBlank() -> context.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyNickname(nickname) -> context.getString(R.string.error_nickname)
            else -> ""
        }
    }

    private fun checkFullName(context: Context, fullName: String?) {
        _fullNameErrorMsg.value = when {
            fullName.isNullOrBlank() -> context.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyFullName(fullName) -> context.getString(R.string.error_create_name)
            else -> ""
        }
    }

    private fun checkQQ(context: Context, qq: String?) {
        _qqErrorMsg.value = when {
            qq.isNullOrBlank() -> context.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyQQ(qq) -> context.getString(R.string.error_qq_number)
            else -> ""
        }
    }

    private fun checkEmail(context: Context, email: String?) {
        _eMailErrorMsg.value = when {
            email.isNullOrBlank() -> context.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyMail(email) -> context.getString(R.string.error_e_mail)
            else -> ""
        }
    }

    private fun checkWeChat(context: Context, wechat: String?) {
        _weChatErrorMsg.value = when {
            wechat.isNullOrBlank() -> context.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyWeChat(wechat) -> context.getString(R.string.error_we_chat_number)
            else -> ""
        }
    }

    private fun checkPhone(context: Context, phone: String?) {
        _phoneErrorMsg.value = when {
            phone.isNullOrBlank() -> context.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPhone(phone) -> context.getString(R.string.error_phone_number)
            else -> ""
        }
    }

    private fun loading() {
        _loading.postValue(true)
    }

    private fun hideLoading() {
        _loading.postValue(false)
    }
}