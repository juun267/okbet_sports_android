package org.cxct.sportlottery.ui.login.foget

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.user.UserRepository
import org.cxct.sportlottery.net.user.data.CheckSafeQuestionResp
import org.cxct.sportlottery.net.user.data.ForgetPasswordResp
import org.cxct.sportlottery.net.user.data.SafeQuestion
import org.cxct.sportlottery.common.appevent.SensorsEventUtil
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.CaptchaRequest
import org.cxct.sportlottery.network.index.forgetPassword.*
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.SingleLiveEvent
import org.cxct.sportlottery.util.VerifyConstUtil


class ForgetViewModel(
    androidContext: Application
) : BaseViewModel(androidContext) {

    val smsResult: LiveData<SendSmsResult?>
        get() = _smsResult
    private val _smsResult = MutableLiveData<SendSmsResult?>()
    //短信验证码返回值
    private val smsCodeResult: LiveData<NetResult?>
        get() = _smsCodeResult
    private val _smsCodeResult = MutableLiveData<NetResult?>()
    //重设密码数据
    val resetPasswordResult: LiveData<ResetPasswordResult?>
        get() = _resetPasswordResult
    private val _resetPasswordResult = MutableLiveData<ResetPasswordResult?>()

    val userNameMsg: LiveData<Pair<String?, Boolean>>
        get() = _userNameMsg
    private val _userNameMsg = MutableLiveData<Pair<String?, Boolean>>()

    val userQuestionEvent = SingleLiveEvent<ApiResult<CheckSafeQuestionResp>>()
    val checkSafeQuestionEvent = SingleLiveEvent<ApiResult<CheckSafeQuestionResp>>()
    val updatePasswordResultEvent = SingleLiveEvent<ApiResult<ForgetPasswordResp>>()

    fun sendEmail(email: String,  identity: String, validCode: String) {
        val params = mutableMapOf("email" to email).apply {
            if (sConfigData?.captchaType==1){
                put("ticket",identity)
                put("randstr",validCode)
            }else{
                put("validCodeIdentity",identity)
                put("validCode",validCode)
            }
        }
        doRequest({ OneBoSportApi.indexService.sendEmailForget(params) }) {
            val status = it?.success == true
            SensorsEventUtil.getCodeEvent(status, true, if (status) null else it?.msg)
            _smsResult.value = it
        }
    }

    fun checkEmailCode(email: String, emailCode: String) {
        val params = mapOf("email" to email, "emailCode" to emailCode)
        doRequest({ OneBoSportApi.indexService.validateEmailCode(params) }) {
            _smsCodeResult.value = it
        }
    }

    fun resetPassWorkByEmail(userName: String, newPassword: String) {
        val request = ResetPasswordRequest(userName, newPassword, newPassword)
        doRequest({ OneBoSportApi.indexService.resetPassWordByEmail(request) }) {
            _resetPasswordResult.value = it
        }
    }

    /**
     * @phoneNum 手机号码
     *  获取短信你验证码
     */
    fun getSendSms(phone: String, identity: String, validCode: String) {
        //先检测手机号 暂时做假数据处理
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.indexService.sendSmsForget(
                    SendSmsRequest(phone).apply { buildParams(identity, validCode) }
                )
            }
            val status = result?.success == true
            SensorsEventUtil.getCodeEvent(status, errorMsg = if (status) null else result?.msg)
            _smsResult.postValue(result)
        }
    }

    //提交手机验证码
     fun getCheckPhone(phone: String,validCode: String) {

        viewModelScope.launch {
             doNetwork(androidContext) {
                OneBoSportApi.indexService.forgetPasswordSMS(
                    ForgetPasswordSmsRequest(phone,validCode)
                )
            }?.let {result->
                 _smsCodeResult.postValue(result)
             }

        }

    }
    //提交密码
    fun resetPassword(userName: String,
                      confirmPassword :String,
                      newPassword: String,
                      phoneNumber: String?,
                      code: String?){
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.indexService.resetPassWord(
                    ResetPasswordRequest(userName,confirmPassword,newPassword, phoneNumber, code)
                )
            }

            _resetPasswordResult.postValue(result)
        }
    }

    /**
     * 获取用户的密保问题
     */
    fun getUserQuestion(userName: String,identity: String, validCode: String){
        callApi({ UserRepository.getUserSafeQuestion(userName, identity, validCode) }){
            userQuestionEvent.postValue(it)
        }
    }
    /**
     * 验证密保问题和答案
     */
    fun checkSafeQuest(userName: String, answer: String, identity: String, validCode: String){
        callApi({ UserRepository.checkSafeQuest(userName, answer, identity, validCode) }){
            checkSafeQuestionEvent.postValue(it)
        }
    }

    /**
     * 使用密保问题修改密码
     */
    fun updatePasswordBySafeQuestion(userName: String, securityCode: String, newPassword: String){
        callApi({ UserRepository.updatePasswordBySafeQuestion(userName, securityCode, newPassword) }){
            updatePasswordResultEvent.postValue(it)
        }
    }


}