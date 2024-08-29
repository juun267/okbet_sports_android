package org.cxct.sportlottery.ui.login.signIn

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.appevent.AFInAppEventUtil
import org.cxct.sportlottery.common.event.SingleEvent
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.common.extentions.toast
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.user.UserRepository
import org.cxct.sportlottery.net.user.data.CheckSafeQuestionResp
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.login.*
import org.cxct.sportlottery.network.index.validCode.ValidCodeRequest
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.login.BindPhoneDialog
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity.Companion.LOGIN_TYPE_PWD
import org.cxct.sportlottery.ui.login.signUp.RegisterSuccessDialog
import org.cxct.sportlottery.util.*


class LoginViewModel(
    androidContext: Application
) : BaseSocketViewModel(androidContext) {

    val loginResult: LiveData<LoginResult>
        get() = _loginResult
    val loginSmsResult: LiveData<NetResult>
        get() = _loginSmsResult
    val validCodeResult: LiveData<ValidCodeResult?>
        get() = _validCodeResult
    val msgCodeResult: LiveData<NetResult?>
        get() = _msgCodeResult
    val validResult: LiveData<NetResult>
        get() = _validResult
    val isLoading: LiveData<Boolean> //使用者餘額
        get() = _isLoading
    val inviteCodeMsg: LiveData<String?>
        get() = _inviteCodeMsg
    val checkUserExist: LiveData<Boolean>
        get() = _checkUserExist
    val selectAccount: LiveData<LoginResult>
        get() = _selectAccount
    val loginGlifeOrRegist: LiveData<LoginResult>
        get() = _loginGlifeOrRegist

    val smsCodeVerify by lazy { SingleLiveEvent<ApiResult<String>>() }
    val resetWithdraw by lazy { SingleLiveEvent<ApiResult<String>>() }

    private val _isLoading = MutableLiveData<Boolean>()
    private val _loginResult = MutableLiveData<LoginResult>()
    private val _selectAccount= MutableLiveData<LoginResult>()
    private val _loginGlifeOrRegist= MutableLiveData<LoginResult>()
    private val _loginSmsResult = MutableLiveData<NetResult>()
    private val _validCodeResult = MutableLiveData<ValidCodeResult?>()
    private val _validResult = MutableLiveData<NetResult>()
    private val _msgCodeResult = MutableLiveData<NetResult?>()
    private val _inviteCodeMsg = MutableLiveData<String?>()
    private val _checkUserExist = MutableLiveData<Boolean>()

    val accountMsg: LiveData<Pair<String?, Boolean>>
        get() = _accountMsg
    private val _accountMsg = MutableLiveData<Pair<String?, Boolean>>()

    val msgCodeMsg: LiveData<Pair<String?, Boolean>>
        get() = _msgCodeMsg
    private val _msgCodeMsg = MutableLiveData<Pair<String?, Boolean>>()

    val userNameMsg: LiveData<Pair<String?, Boolean>>
        get() = _userNameMsg
    private val _userNameMsg = MutableLiveData<Pair<String?, Boolean>>()

    val passwordMsg: LiveData<Pair<String?, Boolean>>
        get() = _passwordMsg
    private val _passwordMsg = MutableLiveData<Pair<String?, Boolean>>()

    val validateCodeMsg: LiveData<Pair<String?, Boolean>>
        get() = _validateCodeMsg
    private val _validateCodeMsg = MutableLiveData<Pair<String?, Boolean>>()

    val questionMsg: LiveData<Pair<String?, Boolean>>
        get() = _questionMsg
    private val _questionMsg = MutableLiveData<Pair<String?, Boolean>>()

    val answerMsg: LiveData<Pair<String?, Boolean>>
        get() = _answerMsg
    private val _answerMsg = MutableLiveData<Pair<String?, Boolean>>()

    val loginEnable: LiveData<Boolean>
        get() = _loginEnable
    private val _loginEnable = MutableLiveData<Boolean>()

    //跳转至完善信息监听
    val registerInfoEvent by lazy { SingleEvent<LoginResult>() }

    val userQuestionEvent = SingleLiveEvent<ApiResult<CheckSafeQuestionResp>>()

    var loginType = LOGIN_TYPE_PWD
        set(value) {
            field = value
            checkAllInputComplete()
        }

    var agreeChecked = true
        set(value) {
            field = value
            focusChangeCheckAllInputComplete()
        }

    fun login(account: String, password: String, identity: String, validCode: String, onNeedVerifyPhone: (String) -> Unit) = launch {
        AFInAppEventUtil.logEvent("login","account",account)
        loading()
        val loginRequest = LoginRequest(
            account = account,
            password = MD5Util.MD5Encode(password),
        ).apply { buildParams(identity,validCode) }


        //預設存帳號
        LoginRepository.account = account
        val loginResult = doNetwork { LoginRepository.login(loginRequest) }

        if (loginResult != null && !loginResult.success) {
            hideLoading()
            toast(loginResult.msg)
            return@launch
        }

        if (loginResult == null || loginResult.rows.isNullOrEmpty()) {
            hideLoading()
            toast(androidContext.getString(R.string.unknown_error))
            return@launch
        }

        val needOptAcount = loginResult.rows.find { it.needOTPLogin }
        if (needOptAcount == null) {
            dealWithLoginResult(loginResult)
            return@launch
        }

        hideLoading()
        if (needOptAcount.phone.isEmptyStr()) {
            toast(androidContext.getString(R.string.text_cant_play))
            return@launch
        }

        onNeedVerifyPhone(needOptAcount.phone!!)

    }

    fun loginOrReg(account: String, smsCode: String, inviteCode: String) {
        AFInAppEventUtil.logEvent("loginOrReg","account",account)
        loading()
        LoginRepository.account = account
        val loginRequest = LoginRequest(
            account = account,
            password = null,
            securityCode = smsCode,
            inviteCode = inviteCode
        )

        doRequest({ LoginRepository.loginOrReg(loginRequest) }) {
            it?.let { launch { dealWithLoginResult(it) } }
            hideLoading()
        }
    }

    fun loginGoogle(token: String) {
        AFInAppEventUtil.logEvent("loginGoogle","token",token)
        loading()
        doRequest({ LoginRepository.googleLogin(token, inviteCode = Constants.getInviteCode()) }) {
            hideLoading()
            it?.let { launch { dealWithLoginResult(it) } }
        }
    }
    fun loginFacebook(token: String) {
        loading()
        doRequest({
            LoginRepository.facebookLogin(token,
                inviteCode = Constants.getInviteCode())
        }) {
            hideLoading()
            it?.let { launch { dealWithLoginResult(it) } }
        }
    }
    fun loginWithSafeQuestion(account: String, answer: String, identity: String, validCode: String){
        loading()
        val loginRequest = LoginRequest(
            account = account,
            safeQuestion = answer,
        ).apply { buildParams(identity,validCode) }
        callApi({UserRepository.loginBySafeQuestion(loginRequest)}){
            hideLoading()
            val loginResult = LoginResult(it.code,it.msg,it.succeeded(), null, rows = it.getData())
            launch { dealWithLoginResult(loginResult) }
        }
    }

    fun regPlatformUser(token: String,loginRequest: LoginRequest) {
        AFInAppEventUtil.logEvent("regPlatformUser","account",loginRequest.account)
        loading()
        doRequest({ LoginRepository.regPlatformUser(token,loginRequest) }) {
            hideLoading()
            it?.let { launch { dealWithLoginResult(it) } }
        }
    }

    private suspend fun dealWithLoginResult(loginResult: LoginResult) {
        if (loginResult.success) {
            //t不为空则t是登录账号，rows里面1个账号就直接登录，2个账号就选择账号
            when  {
                loginResult.t != null -> {
                    dealWithLoginData(loginResult, loginResult.t)
                }
                loginResult.rows?.size==1 -> {
                    val loginData = loginResult.rows[0]
                    // 询问是否登录GLIFE账号，或注册一个 okbet平台账号
                    if (loginData.isCreateAccount==1){
                       _loginGlifeOrRegist.postValue(loginResult)
                    }else{
                        dealWithLoginData(loginResult, loginData)
                    }
                }
                loginResult.rows?.size==2 -> {
                    _selectAccount.postValue(loginResult)
                }
            }
        } else {
           toast(loginResult.msg)
        }
    }
    suspend fun dealWithLoginData(loginResult: LoginResult,loginData: LoginData){
        if (!loginData.msg.isNullOrBlank()){
            toast(loginData.msg!!)
            return
        }
        if (loginData.ifnew != false) {
            AFInAppEventUtil.register("username",HashMap<String, Any>().apply {
                put("uid",loginData.uid.toString())
                put("userId",loginData.userId.toString())
                put("userName",loginData.userName.toString())
                put("phone",loginData.phone.toString())
                put("email",loginData.email.toString())
            })
        } else {
            AFInAppEventUtil.login(loginData.uid.toString(),HashMap<String, Any>().apply {
                put("uid",loginData.uid.toString())
                put("userId",loginData.userId.toString())
                put("userName",loginData.userName.toString())
                put("phone",loginData.phone.toString())
                put("email",loginData.email.toString())
            })
        }
        LoginRepository.setUpLoginData(loginData)
        RegisterSuccessDialog.ifNew = loginData.ifnew==true
        RegisterSuccessDialog.loginFirstPhoneGiveMoney = loginData.firstPhoneGiveMoney==true
        AFInAppEventUtil.regAndLogin(HashMap<String, Any>().apply {
            put("data",loginData.toJson())
        })
        LogUtil.toJson(loginData)
        BindPhoneDialog.afterLoginOrRegist = (sConfigData?.firstPhoneGiveMoney?:0)>0 && loginData.phone.isNullOrEmpty()
        checkBasicInfo(loginResult) {
            //继续登录
            if (loginData.deviceValidateStatus == 1)
                runWithCatch { UserInfoRepository.getUserInfo() }
            _loginResult.postValue(loginResult!!)
        }
    }


    /**
     * 检查用户完善基本信息
     */
    private suspend fun checkBasicInfo(loginResult: LoginResult, block: suspend () -> Unit) {
        if (!loginResult.success) {
            block()
            return
        }
        //本地缓存的是否完善过开关
        val loginSwitch = SPUtil.getLoginInfoSwitch()
        if (loginSwitch) {
            block()
            return
        }
        viewModelScope.launch {
            //用户完善信息开关
            val infoSwitchDefered =
                async { doNetwork(androidContext) { LoginRepository.getUserInfoSwitch() } }
            //是否已完善信息
            val userInfoCheckDefered =
                async { doNetwork(androidContext) { LoginRepository.getUserInfoCheck() } }
            val infoSwitchResult = infoSwitchDefered.await()
            val userInfoCheck = userInfoCheckDefered.await()
            if (infoSwitchResult != null && userInfoCheck != null) {
                val isSwitch = infoSwitchResult.t
                val isFinished = userInfoCheck.t
                //是否需要完善
                if (checkNeedCompleteInfo(isSwitch, isFinished)) {
                    //跳转到完善页面
                    registerInfoEvent.post(loginResult)
                } else {
                    //执行原有登录逻辑
                    block()
                }
            } else {
//            LoginRepository.clear()
                block()
            }
        }
    }

    fun sendLoginDeviceSms(token: String) {
        doRequest({ LoginRepository.sendLoginDeviceSms(token) }) { result ->
            result?.let { _loginSmsResult.postValue(result) }
        }
    }

    fun validateLoginDeviceSms(token: String, code: String, deviceId: String) {

        val validateRequest = ValidateLoginDeviceSmsRequest(
            loginEnvInfo = deviceId,
            validCode = code,
            loginSrc = LOGIN_SRC
        )

        doRequest({
            LoginRepository.validateLoginDeviceSms(token,
                validateRequest)
        }) { result ->
            if (result == null) {
                return@doRequest
            }
            //手機驗證成功後, 獲取最新的用戶資料
            if (result.success) {
                launch { runWithCatch { UserInfoRepository.getUserInfo() } }
            }
            _validResult.postValue(result)
        }
    }

    fun loginAsGuest() {
        doRequest({ LoginRepository.loginForGuest() }) {
            it?.let { _loginResult.value = it }
        }
    }

    fun getValidCode(identity: String?) {
        doRequest({ OneBoSportApi.indexService.getValidCode(ValidCodeRequest(identity)) }) { result ->
            _validCodeResult.postValue(result)
        }
    }

    fun loginOrRegSendValidCode(loginCodeRequest: LoginCodeRequest) {
        doRequest({ OneBoSportApi.indexService.loginOrRegSendValidCode(loginCodeRequest) }) { result ->
            _msgCodeResult.postValue(result)
        }
    }

    fun verifySMSCode(phoneNo: String, smsCode: String) {
        callApi({ UserRepository.verifySMSCode(phoneNo, smsCode) }) { result ->
            smsCodeVerify.value = result
        }
    }

    fun resetWithdraw(newPassword: String) {
        callApi({ UserRepository.resetWithdraw(newPassword) }) {
            resetWithdraw.value = it
        }

    }

    /**
     * 输入邀请码
     */
    fun checkInviteCode(inviteCode: String?) {
        _inviteCodeMsg.value = when {
            inviteCode.isNullOrEmpty() -> {
                androidContext.getString(R.string.error_input_empty)
            }
            !VerifyConstUtil.verifyInviteCode(inviteCode) -> androidContext.getString(R.string.referral_code_invalid)
            else -> null
        }
        focusChangeCheckAllInputComplete()
    }

    /**
     * 手机号/邮箱
     */
    fun checkAccount(username: String): String? {
        val msg = if (sConfigData?.enableEmailReg == "0") {
            when {
                username.isBlank() -> androidContext.getString(R.string.error_input_empty)
                !VerifyConstUtil.verifyPhone(username) -> {
                    androidContext.getString(R.string.pls_enter_correct_mobile)
                }
                else -> null
            }
        } else {
            when {
                username.isBlank() -> androidContext.getString(R.string.error_input_empty)
                !(VerifyConstUtil.verifyPhone(username) || VerifyConstUtil.verifyMail(
                    username
                )) -> {
                    androidContext.getString(R.string.pls_enter_correct_mobile_email)
                }
                else -> null
            }
        }
        _accountMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
        return msg
    }

    /**
     * 手机号/邮箱/用户名
     */
    fun checkUserName(username: String): String? {
        val msg = when {
            username.isBlank() -> androidContext.getString(R.string.error_input_empty)
            !(VerifyConstUtil.verifyPhone(username) || VerifyConstUtil.verifyMail(
                username
            ) || VerifyConstUtil.verifyLengthRange(
                username,
                4,
                20
            )) -> {
                androidContext.getString(R.string.pls_enter_correct_mobile_email_username)
            }
            else -> null
        }
        _userNameMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
        return msg
    }

    fun checkPassword(password: String): String? {
        val msg = when {
            password.isEmpty() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPwd(password) ->
                androidContext.getString(R.string.error_register_password)
            else -> null
        }
        _passwordMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
        return msg
    }

    fun checkValidCode(validCode: String): String? {
        val msg = when {
            validCode.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyValidCode(validCode) -> androidContext.getString(R.string.verification_not_correct)
            else -> null
        }
        _validateCodeMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
        return msg
    }

    fun checkMsgCode(validCode: String): String? {
        val msg = when {
            validCode.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyValidCode(validCode) -> androidContext.getString(R.string.verification_not_correct)
            else -> null
        }
        _msgCodeMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
        return msg
    }
    fun checkQuestion(question: String): String? {
        val msg = when {
            question.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        _questionMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
        return msg
    }
    fun checkAnswer(answer: String): String? {
        val msg = when {
            answer.isNullOrBlank() -> androidContext.getString(R.string.error_input_empty)
            else -> null
        }
        _answerMsg.value = Pair(msg, msg == null)
        focusChangeCheckAllInputComplete()
        return msg
    }

    fun focusChangeCheckAllInputComplete() {
        _loginEnable.value = checkAllInputComplete()
    }

    private fun checkAllInputComplete(): Boolean {
        if (loginType == 0) {
            if (checkInputPair(accountMsg)) {
                return false
            }
            if (checkInputPair(msgCodeMsg)) {
                return false
            }
        } else if(loginType == 1) {
            if (checkInputPair(userNameMsg)) {
                return false
            }
            if (checkInputPair(passwordMsg)) {
                return false
            }
        }else if(loginType == 2) {
            if (checkInputPair(userNameMsg)) {
                return false
            }
        }else if(loginType == 3) {
            if (checkInputPair(userNameMsg)) {
                return false
            }
            if (checkInputPair(questionMsg)) {
                return false
            }
            if (checkInputPair(answerMsg)) {
                return false
            }
        }
        return agreeChecked
    }

    private fun checkInputPair(data: LiveData<Pair<String?, Boolean>>): Boolean {
        return data.value?.first != null || data.value?.second != true
    }

    private fun loading() {
        _isLoading.postValue(true)
    }

    private fun hideLoading() {
        _isLoading.postValue(false)
    }

    fun queryPlatform(inviteCode: String) {
        doRequest({ OneBoSportApi.bettingStationService.queryPlatform(inviteCode) }) { result ->
            if (result?.success == true) {

            } else {
                _inviteCodeMsg.value = result?.msg
                focusChangeCheckAllInputComplete()
            }
        }
    }

    fun checkUserExist(phoneNumberOrEmail: String) {
        doRequest({
            OneBoSportApi.indexService.checkUserExist(CheckUserRequest(phoneNumberOrEmail))
        }) {
            it?.let { _checkUserExist.value = it.success }
        }
    }


    /**
     * 是否需要完善基础信息
     */
    private fun checkNeedCompleteInfo(isComplete: Boolean, isFinished: Boolean): Boolean {
        return isComplete && !isFinished
    }

    /**
     * 获取用户的密保问题
     */
    fun getUserQuestion(userName: String, identity: String, validCode: String){
        callApi({ UserRepository.getUserSafeQuestion(userName, identity, validCode) }){
            userQuestionEvent.postValue(it)
        }
    }
}