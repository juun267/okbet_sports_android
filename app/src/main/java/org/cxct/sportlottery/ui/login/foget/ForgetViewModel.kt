package org.cxct.sportlottery.ui.login.foget

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.checkAccount.CheckAccountResult
import org.cxct.sportlottery.network.index.forgetPassword.*
import org.cxct.sportlottery.network.index.validCode.ValidCodeRequest
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.VerifyConstUtil


class ForgetViewModel(
    private val androidContext: Context,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
) : BaseViewModel(loginRepository, betInfoRepository, infoCenterRepository) {
    //手机号异常提示
    val phoneMsg: LiveData<Pair<String?, Boolean>>
        get() = _phoneMsg
    private val _phoneMsg = MutableLiveData<Pair<String?, Boolean>>()

    //验证码异常提示
    val accountCodeMsg: LiveData<Pair<String?, Boolean>>
        get() = _accountCodeMsg
    val _accountCodeMsg = MutableLiveData<Pair<String?, Boolean>>()

    //密码异常提示
    val passwordMsg: LiveData<Pair<String?, Boolean>>
        get() = _passwordMsg
    val _passwordMsg = MutableLiveData<Pair<String?, Boolean>>()

    //确认密码异常提示
    val confirmPasswordMsg: LiveData<Pair<String?, Boolean>>
        get() = _confirmPasswordMsg
    private val _confirmPasswordMsg = MutableLiveData<Pair<String?, Boolean>>()

    //验证码异常提示
    val validateCodeMsg: LiveData<Pair<String?, Boolean>>
        get() = _validateCodeMsg
    private val _validateCodeMsg = MutableLiveData<Pair<String?, Boolean>>()

    val validCodeResult: LiveData<ValidCodeResult?>
        get() = _validCodeResult
    private val _validCodeResult = MutableLiveData<ValidCodeResult?>()
    //提交按钮状态
    val putEnable: LiveData<Boolean>
        get() = _putEnable
    private val _putEnable = MutableLiveData<Boolean>()

    //提交按钮状态
    val smsEnable: LiveData<Boolean>
        get() = _smsEnable
    private val _smsEnable = MutableLiveData<Boolean>()
    //用户校验返回值
    val validDateResult: LiveData<ValidateUserResult?>
        get() = _validDateResult
    private val _validDateResult = MutableLiveData<ValidateUserResult?>()

    val smsResult: LiveData<SendSmsResult?>
        get() = _smsResult
    private val _smsResult = MutableLiveData<SendSmsResult?>()
    //短信验证码返回值
    val smsCodeResult: LiveData<NetResult?>
        get() = _smsCodeResult
    private val _smsCodeResult = MutableLiveData<NetResult?>()
    //重设密码数据
    val resetPasswordResult: LiveData<ResetPasswordResult?>
        get() = _resetPasswordResult
    private val _resetPasswordResult = MutableLiveData<ResetPasswordResult?>()

    //用户
    val accountMsg: LiveData<Pair<String?, Boolean>>
        get() = _accountMsg
    val _accountMsg = MutableLiveData<Pair<String?, Boolean>>()

    val checkAccountMsg: LiveData<CheckAccountResult>
        get() = _checkAccountMsg
    private val _checkAccountMsg = MutableLiveData<CheckAccountResult>()

    //用户输入框校验
    fun checkAccount(username: String): String? {
        val msg = when {
            username.isBlank() -> LocalUtils.getString(R.string.error_input_empty)
//            !VerifyConstUtil.verifyCombinationAccount(username) -> {
//                LocalUtils.getString(R.string.error_member_account)
//            }
//            !VerifyConstUtil.verifyAccount(username) -> LocalUtils.getString(R.string.error_member_account)
            else -> null
        }
        _accountMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(0)
        return msg
    }
    //用户随机验证码
    fun checkValidCode(validCode: String): String? {
        val msg = when {
            validCode.isBlank() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyValidCode(validCode) -> LocalUtils.getString(R.string.error_verification_code_forget)
            else -> null
        }
        _accountCodeMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete(0)
        return msg
    }
    //手机号码输入验证
    fun checkPhone(phoneNum: String): String? {
        val msg = when {
            phoneNum.isBlank() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPhone(phoneNum) -> {
                LocalUtils.getString(R.string.pls_enter_correct_mobile)
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
            !VerifyConstUtil.verifyPwd(password) -> LocalUtils.getString(R.string.error_new_password)
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
            password != confirmPassword -> LocalUtils.getString(R.string.error_tips_confirm_password)
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
            0->{
                if (checkInputPair(accountMsg)) {
                    return false
                }
                if (checkInputPair(accountCodeMsg)) {
                    return false
                }
            }
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

    fun sendEmail(email: String,  identity: String, validCode: String) {
        val params = mapOf("email" to email, "validCodeIdentity" to identity, "validCode" to validCode)
        doRequest(androidContext, { OneBoSportApi.indexService.sendEmailForget(params) }) {
            _smsResult.value = it
        }
    }

    fun checkEmailCode(email: String, emailCode: String) {
        val params = mapOf("email" to email, "emailCode" to emailCode)
        doRequest(androidContext, { OneBoSportApi.indexService.validateEmailCode(params) }) {
            _smsCodeResult.value = it
        }
    }

    fun resetPassWorkByEmail(userName: String, newPassword: String) {
        val request = ResetPasswordRequest(userName, newPassword, newPassword)
        doRequest(androidContext, { OneBoSportApi.indexService.resetPassWordByEmail(request) }) {
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
                    SendSmsRequest(phone, identity, validCode)
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
             }

        }

    }
    //提交密码
    fun resetPassword(userName: String, confirmPassword :String,
                      newPassword: String){
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.indexService.resetPassWord(
                    ResetPasswordRequest(userName,confirmPassword,newPassword)
                )
            }

            _resetPasswordResult.postValue(result)
        }
    }
    //随机验证码网络请求
    fun getValidCode(identity: String?) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.indexService.getValidCode(ValidCodeRequest(identity))
            }
            _validCodeResult.postValue(result)
        }
    }
    //校验用户名
    fun checkAccountExist(account: String) {

        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.indexService.checkAccountExist(account)
            }.let {
                _checkAccountMsg.value = it

                focusChangeCheckAllInputComplete(0)

            }
        }
    }

    //账户认证
    /**
     * @param validCode 验证码
     * @param userName  用户账户
     * @param validCodeIdentity 验证码标识
     */
    fun checkValidateUser(validCode: String,userName: String,validCodeIdentity: String){
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.indexService.checkValidateUser(
                    ValidateUserRequest(validCode,userName,validCodeIdentity)
                )
            }.let {
                _validDateResult.postValue(it)
            }
        }

    }





}