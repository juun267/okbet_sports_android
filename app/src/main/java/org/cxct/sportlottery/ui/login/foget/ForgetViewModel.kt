package org.cxct.sportlottery.ui.login.foget

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.forgetPassword.*
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseViewModel


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

}