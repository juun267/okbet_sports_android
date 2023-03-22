package org.cxct.sportlottery.ui.profileCenter.modify

import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.extentions.callApi
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.user.UserRepository
import org.cxct.sportlottery.net.user.data.SendCodeRespnose
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseViewModel

class BindInfoViewModel(
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
): BaseViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    val sendCodeResult = MutableLiveData<ApiResult<SendCodeRespnose>>()                 // 发送验证码
    val verifyResult = MutableLiveData<ApiResult<SendCodeRespnose>>()                   // 验证验证码
    val resetResult = MutableLiveData<ApiResult<SendCodeRespnose>>()                    // 修改手机号或者邮箱后

    fun sendSMSOrEmailCode(phoneNumOrEmail: String, identity: String, validCode: String) {
        callApi({ UserRepository.sendEmailOrPhoneCode(phoneNumOrEmail, identity, validCode) }) {
            sendCodeResult.postValue(it)
        }
    }

    fun verifyEmailOrPhoneCode(phoneNumOrEmail: String, code: String) {
        callApi({ UserRepository.verifyEmailOrPhoneCode(phoneNumOrEmail, code) }) {
            verifyResult.postValue(it)
        }
    }

    fun resetEmailOrPhone(phoneNumOrEmail: String, code: String) {
        callApi({ UserRepository.resetEmailOrPhone(phoneNumOrEmail, code) }) {
            resetResult.postValue(it)
        }
    }

}