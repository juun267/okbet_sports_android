package org.cxct.sportlottery.ui.profileCenter.nickname

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.net.user.UserRepository
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.user.nickname.NicknameRequest
import org.cxct.sportlottery.network.user.setWithdrawInfo.WithdrawInfoRequest
import org.cxct.sportlottery.network.user.setWithdrawInfo.WithdrawInfoResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.Uide
import org.cxct.sportlottery.util.SingleLiveEvent
import org.cxct.sportlottery.util.VerifyConstUtil

class ModifyProfileInfoViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {

    private val _loading = MutableLiveData<Boolean>()
    private val _nicknameResult = MutableLiveData<NetResult?>()
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
    val nicknameResult: LiveData<NetResult?>
        get() = _nicknameResult
    val withdrawInfoResult: LiveData<WithdrawInfoResult?>
        get() = _withdrawInfoResult
    val userNameChangeResult = SingleLiveEvent<Pair<Boolean, String>>()

    fun confirmProfileInfo(@ModifyType modifyType: Int, inputContent: String) {
        if (checkInput(modifyType, inputContent)) {
            //暱稱設定是獨立一隻api
            when (modifyType) {
                ModifyType.NickName -> {
                    editNickName(inputContent)
                }

                ModifyType.PlaceOfBirth -> {
                    userCompleteUserDetails(
                        modifyType,
                        Uide(
                            placeOfBirth = inputContent
                        )
                    )
                }

                ModifyType.Address -> {
                    userCompleteUserDetails(
                        modifyType,
                        Uide(
                            address = inputContent
                        )
                    )
                }

                ModifyType.AddressP -> {
                    userCompleteUserDetails(
                        modifyType,
                        Uide(
                            permanentAddress = inputContent
                        )
                    )
                }

                ModifyType.ZipCode -> {
                    userCompleteUserDetails(
                        modifyType,
                        Uide(
                            zipCode = inputContent
                        )
                    )
                }

                ModifyType.ZipCodeP -> {
                    userCompleteUserDetails(
                        modifyType,
                        Uide(
                            permanentZipCode = inputContent
                        )
                    )
                }

                else -> {
                    setWithdrawInfo(modifyType, inputContent)
                }
            }
        }
    }

    /**
     * 完善用户信息
     */
    private fun userCompleteUserDetails(@ModifyType modifyType: Int, uide: Uide) {
        launch {
            doNetwork(androidContext) {
                OneBoSportApi.userService.userCompleteUserDetails(uide)
            }?.let {
                it.let {
                    _nicknameResult.postValue(it)
                    when (modifyType) {
                        ModifyType.PlaceOfBirth -> {
                            uide.placeOfBirth?.let { it1 ->
                                UserInfoRepository.updatePlaceOfBirth(
                                    it1
                                )
                            }
                        }

                        ModifyType.Address -> {
                            uide.address?.let { it1 ->
                                UserInfoRepository.updateaddress(
                                    it1
                                )
                            }
                        }

                        ModifyType.AddressP -> {
                            uide.permanentAddress?.let { it1 ->
                                UserInfoRepository.updatePermanentAddress(
                                    it1
                                )
                            }
                        }

                        ModifyType.ZipCode -> {
                            uide.zipCode?.let { it1 ->
                                UserInfoRepository.updateZipCode(
                                    it1
                                )
                            }
                        }

                        ModifyType.ZipCodeP -> {
                            uide.permanentZipCode?.let { it1 ->
                                UserInfoRepository.updatepermanentZipCode(
                                    it1
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun checkInput(@ModifyType modifyType: Int, inputContent: String): Boolean {
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

            ModifyType.PlaceOfBirth -> {
                true
            }

            ModifyType.Address -> {
                true
            }

            ModifyType.AddressP -> {
                true
            }

            ModifyType.ZipCode -> {
                true
            }

            ModifyType.ZipCodeP -> {
                true
            }

            else -> {
                false
            }
        }

    }

    private fun setWithdrawInfo(@ModifyType modifyType: Int, inputContent: String) {
        loading()
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.userService.setWithdrawUserInfo(
                    createWithdrawInfoRequest(
                        modifyType,
                        inputContent
                    )
                )
            }?.let { result ->
                hideLoading()
                _withdrawInfoResult.value = result
                if (result.success) {
                    updateUserInfoDao(modifyType, inputContent)
                }
            }
        }
    }

    private fun createWithdrawInfoRequest(
        @ModifyType modifyType: Int,
        inputContent: String
    ): WithdrawInfoRequest {
        val userId = LoginRepository.userId
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

    private suspend fun updateUserInfoDao(@ModifyType modifyType: Int, inputContent: String) {
        UserInfoRepository.apply {
            val userId = userInfo?.value?.userId ?: -1
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
                val userId = UserInfoRepository.userInfo?.value?.userId ?: -1
                UserInfoRepository.updateNickname(userId, nickname)
                UserInfoRepository.updateSetted(userId, FLAG_NICKNAME_IS_SET)
            }

            _nicknameResult.postValue(result)
        }
    }

    private fun checkNickname(context: Context, nickname: String?) {
        _nickNameErrorMsg.value = when {
            nickname.isNullOrBlank() -> context.getString(R.string.error_input_empty)
            else -> ""
        }
    }

    fun checkFullName(context: Context, fullName: String?) {
        _fullNameErrorMsg.value = when {
            !VerifyConstUtil.verifyFullName(fullName) -> context.getString(R.string.error_input_has_blank)
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
            else -> ""
        }
    }

    private fun checkPhone(context: Context, phone: String?) {
        _phoneErrorMsg.value = when {
            phone.isNullOrBlank() -> context.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPhone(phone) -> context.getString(R.string.hint_phone_format)
            else -> ""
        }
    }

    private fun loading() {
        _loading.postValue(true)
    }

    private fun hideLoading() {
        _loading.postValue(false)
    }

    fun editUserName(firstName: String, middelName: String, lastName: String) {
        loading()
        callApi({ UserRepository.changeUserName(firstName, middelName, lastName) }) {
            hideLoading()
            if (it.succeeded()) {
                val fullName = "$firstName${if (middelName == null) "" else " $middelName"} $lastName"
                UserInfoRepository.updateFullName(LoginRepository.userId, fullName)
            }
            userNameChangeResult.value = Pair(it.succeeded(), it.msg)
        }
    }
}