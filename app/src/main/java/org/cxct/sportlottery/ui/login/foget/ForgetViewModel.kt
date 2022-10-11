package org.cxct.sportlottery.ui.login.foget

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.forgetPassword.ForgetPasswordSmsRequest
import org.cxct.sportlottery.network.index.forgetPassword.ForgetSmsResult
import org.cxct.sportlottery.network.index.forgetPassword.ResetPasswordRequest
import org.cxct.sportlottery.network.index.forgetPassword.ResetPasswordResult
import org.cxct.sportlottery.network.index.sendSms.SmsRequest
import org.cxct.sportlottery.network.index.sendSms.SmsResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.VerifyConstUtil


class ForgetViewModel(
    private val androidContext: Context,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
) : BaseViewModel(loginRepository, betInfoRepository, infoCenterRepository) {
    //账户异常提示
    val phoneMsg: LiveData<Pair<String?, Boolean>>
        get() = _phoneMsg
    private val _phoneMsg = MutableLiveData<Pair<String?, Boolean>>()

    //密码异常提示
    val passwordMsg: LiveData<Pair<String?, Boolean>>
        get() = _passwordMsg
    private val _passwordMsg = MutableLiveData<Pair<String?, Boolean>>()

    //确认密码异常提示
    val confirmPasswordMsg: LiveData<Pair<String?, Boolean>>
        get() = _confirmPasswordMsg
    private val _confirmPasswordMsg = MutableLiveData<Pair<String?, Boolean>>()

    //验证码异常提示
    val validateCodeMsg: LiveData<Pair<String?, Boolean>>
        get() = _validateCodeMsg
    private val _validateCodeMsg = MutableLiveData<Pair<String?, Boolean>>()

    //提交按钮状态
    val putEnable: LiveData<Boolean>
        get() = _putEnable
    private val _putEnable = MutableLiveData<Boolean>()

    //提交按钮状态
    val smsEnable: LiveData<Boolean>
        get() = _smsEnable
    private val _smsEnable = MutableLiveData<Boolean>()
    val smsResult: LiveData<SmsResult?>
        get() = _smsResult
    private val _smsResult = MutableLiveData<SmsResult?>()
    //短信验证码返回值
    val smsCodeResult: LiveData<ForgetSmsResult?>
        get() = _smsCodeResult
    private val _smsCodeResult = MutableLiveData<ForgetSmsResult?>()
    //重设密码数据
    val resetPasswordResult: LiveData<ResetPasswordResult?>
        get() = _resetPasswordResult
    private val _resetPasswordResult = MutableLiveData<ResetPasswordResult?>()
    //手机号码输入验证
    fun checkPhone(phoneNum: String): String? {
        val msg = when {
            phoneNum.isBlank() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPhone(phoneNum) -> {
                LocalUtils.getString(R.string.error_phone_number)
            }
            else -> null
        }
        _phoneMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(1)
        smsCheckComplete()
        return msg
    }

    fun checkSecurityCode(securityCode: String?) {
        val msg = when {
            securityCode.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifySecurityCode(securityCode) -> LocalUtils.getString(R.string.error_verification_code_by_sms)
            else -> null
        }
        _validateCodeMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(1)
    }

    fun checkPassword(password: String, confirmPassword: String? = null): String? {
        val msg = when {
            password.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPwdFormat(password) -> LocalUtils.getString(R.string.error_register_password)
            password.length !in 6..20 -> LocalUtils.getString(R.string.error_register_password)
            !VerifyConstUtil.verifyPwd(password) -> LocalUtils.getString(R.string.error_input_empty)
            else -> null
        }
        if (confirmPassword?.isNotEmpty() == true)
            checkConfirmPassword(password, confirmPassword)
        _passwordMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(2)
        return msg
    }

    //确认密码
    fun checkConfirmPassword(password: String?, confirmPassword: String?) {
        val msg = when {
            password.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            password != confirmPassword -> LocalUtils.getString(R.string.error_confirm_password_forget)
            else -> null
        }
        _confirmPasswordMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(2)
    }

    fun smsCheckComplete() {
        _smsEnable.value = !checkInputPair(phoneMsg)
    }

    fun focusChangeCheckAllInputComplete(page: Int) {
        _putEnable.value = checkAllInputComplete(page)
    }

    //手机验证码页面检测
    private fun checkAllInputComplete(page: Int): Boolean {
        when (page) {
            1 -> {
                if (checkInputPair(phoneMsg)) {
                    return false
                }
                if (checkInputPair(validateCodeMsg)) {
                    return false
                }
            }
            2 -> {
                if (checkInputPair(passwordMsg)) {
                    return false
                }
                if (checkInputPair(_confirmPasswordMsg)) {
                    return false
                }
            }
        }
        return true
    }

    //输入框验证-按钮是否可点击
    private fun checkInputPair(data: LiveData<Pair<String?, Boolean>>): Boolean {
        return data.value?.first != null || data.value?.second != true
    }

    /**
     * @phoneNum 手机号码
     *  获取手机号码,先验证,验证通过发送验证码 开启倒计时,不通过提示异常倒计时不触发
     */
    fun getSendSms(phoneNum: String) {
        //先检测手机号 暂时做假数据处理
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.indexService.sendSms(
                    SmsRequest(phoneNum)
                )
            }
            _smsResult.postValue(result)
        }
        _phoneMsg.value = Pair(null, true)
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
                 LogUtil.d(result.toString())
             }

        }

    }
    //提交密码
    fun resetPassword(phone: String, confirmPassword :String,
                      newPassword: String){
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.indexService.resetPassWord(
                    ResetPasswordRequest(phone,confirmPassword,newPassword)
                )
            }
            _resetPasswordResult.postValue(result)
        }
    }

}