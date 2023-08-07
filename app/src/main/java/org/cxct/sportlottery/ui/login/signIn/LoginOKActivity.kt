package org.cxct.sportlottery.ui.login.signIn

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import cn.jpush.android.api.JPushInterface
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.activity_login_ok.*
import kotlinx.android.synthetic.main.view_status_bar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.crash.FirebaseLog
import org.cxct.sportlottery.common.event.LoginGlifeOrRegistEvent
import org.cxct.sportlottery.common.event.LoginSelectAccountEvent
import org.cxct.sportlottery.common.event.RegisterInfoEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.databinding.ActivityLoginOkBinding
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.login.LoginCodeRequest
import org.cxct.sportlottery.network.index.login.LoginData
import org.cxct.sportlottery.network.index.login.LoginRequest
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.repository.ImageType
import org.cxct.sportlottery.repository.LOGIN_SRC
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.common.dialog.SelfLimitFrozeErrorDialog
import org.cxct.sportlottery.ui.login.VerifyCodeDialog
import org.cxct.sportlottery.ui.login.foget.ForgetWaysActivity
import org.cxct.sportlottery.ui.login.selectAccount.SelectAccountActivity
import org.cxct.sportlottery.ui.login.signUp.info.RegisterInfoActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.boundsEditText.SimpleTextChangedWatcher
import org.cxct.sportlottery.view.checkRegisterListener
import org.cxct.sportlottery.view.boundsEditText.TextFormFieldBoxes
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import splitties.activities.start


/**
 * @app_destination 登入
 */
class LoginOKActivity : BaseActivity<LoginViewModel>(LoginViewModel::class) {

    private val loginScope = CoroutineScope(Dispatchers.Main)

    private lateinit var binding: ActivityLoginOkBinding

    companion object {
        private const val SELF_LIMIT = 1130
        const val LOGIN_TYPE_CODE = 0
        const val LOGIN_TYPE_PWD = 1
        const val LOGIN_TYPE_GOOGLE = 2

        fun googleLoging(context: Context) {
            val intent = Intent(context, LoginOKActivity::class.java)
            intent.putExtra("login_type", LOGIN_TYPE_GOOGLE)
            context.startActivity(intent)
        }

        fun startRegist(context: Context) {
            val intent = Intent(context, LoginOKActivity::class.java)
            intent.putExtra("login_type", LOGIN_TYPE_CODE)
            context.startActivity(intent)
        }
    }

    private var countDownGoing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .statusBarView(v_statusbar)
            .fitsSystemWindows(false)
            .init()
        binding = ActivityLoginOkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupSummary(binding.includeSubtitle.tvSummary)
        initOnClick()
        setupInvite()
        setupAccount()
        setupPassword()
        setupValidCode()
        setupLoginButton()
        setupAuthLogin()
        setupPrivacy()
        setupServiceButton()
        initObserve()
        viewModel.focusChangeCheckAllInputComplete()
        EventBusUtil.targetLifecycle(this)

        val loginType = intent.getIntExtra("login_type", LOGIN_TYPE_PWD)
        if (LOGIN_TYPE_CODE == loginType) {
            switchLoginType(LOGIN_TYPE_CODE)
        } else if (loginType == LOGIN_TYPE_GOOGLE) {
            googleLogin()
        }
    }

    private fun initOnClick() {
        binding.btnBack.setOnClickListener { finish() }
        tv_pwd_login.setOnClickListener { switchLoginType(LOGIN_TYPE_PWD) }
        tv_code_login.setOnClickListener { switchLoginType(LOGIN_TYPE_CODE) }
        tv_forget_password.setOnClickListener { startActivity(ForgetWaysActivity::class.java) }
    }

    private fun setupInvite() {
        val defaultInviteCode = Constants.getInviteCode()
        binding.eetRecommendCode.apply {
            checkRegisterListener {
                if (it != "") {
                    viewModel.checkInviteCode(it)
                }
            }
        }
        binding.eetRecommendCode.setText(defaultInviteCode)
        binding.eetRecommendCode.isEnabled = defaultInviteCode.isNullOrEmpty()
        setupRecommendCodeVisible()
    }

    private fun setupAccount() {
        binding.eetAccount.checkRegisterListener {
            viewModel.checkAccount(it).let { result ->
                if (result.isNullOrBlank() && !binding.eetAccount.isFocused) {
                    viewModel.checkUserExist(it)
                }
            }
        }
        if (sConfigData?.enableEmailReg == "0") {
            binding.etAccount.setHintText(getString(R.string.phone_number))
            binding.eetAccount.inputType = InputType.TYPE_CLASS_PHONE
            binding.eetAccount.maxEms = 11
        }
        binding.eetPassword.checkRegisterListener { viewModel.checkPassword(it) }
        binding.eetUsername.checkRegisterListener { viewModel.checkUserName(it) }
        binding.eetVerificationCode.checkRegisterListener { viewModel.checkMsgCode(it) }
        binding.etAccount.endIconImageButton.setOnClickListener {
            binding.eetAccount.text = null
        }
    }

    private fun setupPassword() {
        if (!viewModel.password.isNullOrBlank()) {
            binding.eetPassword.setText(viewModel.password)
        }
        binding.etPassword.endIconImageButton.setOnClickListener {
            if (binding.etPassword.endIconResourceId == R.drawable.ic_eye_open) {
                binding.eetPassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                binding.etPassword.setEndIcon(R.drawable.ic_eye_close)
            } else {
                binding.etPassword.setEndIcon(R.drawable.ic_eye_open)
                binding.eetPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            }
            binding.eetPassword.setSelection(binding.eetPassword.text.toString().length)
        }
        //避免自動記住密碼被人看到，把顯示密碼按鈕功能隱藏，直到密碼被重新編輯才顯示
        if (binding.eetPassword.text.toString().isEmpty()) {
            binding.etPassword.endIconImageButton.visibility = View.GONE
        } else {
            binding.etPassword.endIconImageButton.visibility = View.VISIBLE
        }
        binding.etPassword.setSimpleTextChangeWatcher(object : SimpleTextChangedWatcher {
            override fun onTextChanged(theNewText: String?, isError: Boolean) {
                if (binding.etPassword.endIconImageButton.visibility == View.GONE) {
                    binding.etPassword.endIconImageButton.visibility = View.VISIBLE
                }

            }
        })
        binding.btnLogin.requestFocus()
        if (binding.eetAccount.text.length > 0) {
            binding.btnLogin.setBtnEnable(true)
        }
    }

    private fun setupValidCode() {
        binding.btnSendSms.setOnClickListener {
            VerifyCodeDialog().run {
                callBack = { identity, validCode ->
                    updateValidCode(identity, validCode)
                }
                show(supportFragmentManager, null)
            }
        }
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            login()
        }
        binding.btnLogin.setTitleLetterSpacing()
    }

    private fun updateValidCode(validCodeIdentity: String?, validCode: String) {
        val account = binding.eetAccount.text.toString()
        viewModel.loginOrRegSendValidCode(LoginCodeRequest(account, validCodeIdentity, validCode))
        binding.eetVerificationCode.apply {
            if (text.isNotBlank()) {
                text = null
            }
        }
    }

    private fun login() {
        val deviceSn = JPushInterface.getRegistrationID(this)
        val deviceId = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        var appVersion = org.cxct.sportlottery.BuildConfig.VERSION_NAME
        hideSoftKeyboard(this)
        if (viewModel.loginType == LOGIN_TYPE_CODE) {
            val account = binding.eetAccount.text.toString()
            val smsCode = binding.eetVerificationCode.text.toString()
            var inviteCode = binding.eetRecommendCode.text.toString()
            val loginRequest = LoginRequest(
                account = account,
                password = null,
                loginSrc = LOGIN_SRC,
                deviceSn = deviceSn,
                appVersion = appVersion,
                loginEnvInfo = deviceId,
                securityCode = smsCode,
                inviteCode = inviteCode
            )

            viewModel.loginOrReg(loginRequest)
            return
        }


        val verifyCodeDialog = VerifyCodeDialog()
        verifyCodeDialog.callBack = { identity, validCode ->
            val account = binding.eetUsername.text.toString()
            val password = binding.eetPassword.text.toString()
            val loginRequest = LoginRequest(
                account = account,
                password = MD5Util.MD5Encode(password),
                loginSrc = LOGIN_SRC,
                deviceSn = deviceSn,
                appVersion = appVersion,
                loginEnvInfo = deviceId,
                securityCode = null,
                validCodeIdentity = identity,
                validCode = validCode
            )
            viewModel.login(loginRequest, password)
        }

        verifyCodeDialog.show(supportFragmentManager, null)
    }

    /**
     * 登录完善用户信息event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRegisterInfoCompleted(event: RegisterInfoEvent) {
        updateUiWithResult(event.loginResult)
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSelectAccount(event: LoginSelectAccountEvent) {
        val loginResult = viewModel.selectAccount.value
        loginResult?.rows?.let {
            it.first { it.vipType==(if(event.isVip) 1 else 0)}.let {
                lifecycleScope.launch {
                    viewModel.dealWithLoginData(loginResult!!,it)
                }
            }
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoginGlifeOrRegist(event: LoginGlifeOrRegistEvent) {
        val loginResult = viewModel.loginGlifeOrRegist.value
        loginResult?.rows?.let {
            it.firstOrNull()?.let {
                lifecycleScope.launch {
                    if (event.login){
                        viewModel.dealWithLoginData(loginResult!!,it)
                    }else{
                        //新的注册接口
                        val deviceSn = JPushInterface.getRegistrationID(this@LoginOKActivity)
                        val deviceId = Settings.Secure.getString(
                            applicationContext.contentResolver,
                            Settings.Secure.ANDROID_ID
                        )
                        var appVersion = org.cxct.sportlottery.BuildConfig.VERSION_NAME
                        val loginRequest = LoginRequest(
                            account = it.userName?:"",
                            loginSrc = LOGIN_SRC,
                            deviceSn = deviceSn,
                            appVersion = appVersion,
                            loginEnvInfo = deviceId,
                        )
                        viewModel.regPlatformUser(it.token?:"",loginRequest)
                    }
                }
            }
        }
    }

    private fun googleLogin() {
        loading()
        AuthManager.authGoogle(this@LoginOKActivity)
    }

    override fun onPause() {
        super.onPause()
//        hideLoading()
    }

    private fun setupAuthLogin() {
        btn_google.setOnClickListener {
//            if (binding.cbPrivacy.isChecked) {
                googleLogin()
//            }

        }

        btn_facebook.setOnClickListener {
//            AuthManager.authFacebook(this@LoginOKActivity, { token ->
//                viewModel.loginFacebook(token)
//            }, { errorMsg ->
//                showErrorDialog(errorMsg)
//            })
        }
    }

    private fun setupPrivacy() {
        binding.tvPrivacy.setVisibilityByMarketSwitch()
//        binding.tvPrivacy.setOnCheckedChangeListener { buttonView, isChecked ->
//            viewModel.agreeChecked = isChecked
//        }
        binding.tvPrivacy.makeLinks(
            Pair(
                applicationContext.getString(R.string.login_privacy_policy),
                View.OnClickListener {
                    JumpUtil.toInternalWeb(
                        this,
                        Constants.getPrivacyRuleUrl(this),
                        resources.getString(R.string.login_privacy_policy)
                    )
                })
        )
        binding.tvPrivacy.makeLinks(
            Pair(
                applicationContext.getString(R.string.login_terms_conditions),
                View.OnClickListener {
                    JumpUtil.toInternalWeb(
                        this,
                        Constants.getAgreementRuleUrl(this),
                        resources.getString(R.string.login_terms_conditions)
                    )
                })
        )
    }

    private fun setupServiceButton() {
        binding.tvCustomerService.setServiceClick(supportFragmentManager)
    }

    private fun initObserve() {
        viewModel.isLoading.observe(this) {
            if (it) {
                loading()
            } else {
                hideLoading()
            }
        }
        viewModel.inviteCodeMsg.observe(this) {
            binding.etRecommendCode.setError(
                it,
                false
            )
            if (it == null) {
                viewModel.queryPlatform(binding.eetRecommendCode.text.toString())
            }
        }
        viewModel.checkUserExist.observe(this) {
            setupRecommendCodeVisible()
        }
        viewModel.accountMsg.observe(this) {
            binding.etAccount.setError(
                it.first,
                false
            )
            if (!countDownGoing) {
                binding.btnSendSms.setBtnEnable(it.first.isNullOrBlank())
            }
        }
        viewModel.msgCodeMsg.observe(this) {
            binding.etVerificationCode.setError(
                it.first,
                false
            )
        }
        viewModel.userNameMsg.observe(this) {
            binding.etUsername.setError(
                it.first,
                false
            )
        }
        viewModel.passwordMsg.observe(this) {
            binding.etPassword.setError(
                it.first,
                false
            )
        }
        viewModel.passwordMsg.observe(this) {
            binding.etPassword.setError(
                it.first,
                false
            )
        }

        viewModel.loginEnable.observe(this) {
            binding.btnLogin.setBtnEnable(it)
        }

        viewModel.loginResult.observe(this, Observer {
            loginScope.launch {
                updateUiWithResult(it)
            }
        })
        viewModel.selectAccount.observe(this, Observer {
             start<SelectAccountActivity> {
                 putExtra(SelectAccountActivity.TYPE_SELECT,SelectAccountActivity.TYPE_LOGIN)
             }
        })
        viewModel.loginGlifeOrRegist.observe(this, Observer {
            start<SelectAccountActivity> {
                putExtra(SelectAccountActivity.TYPE_SELECT,SelectAccountActivity.TYPE_LOGINGLIFE_OR_REGIST)
            }
        })

        //跳转至完善注册信息
        viewModel.registerInfoEvent.observe(this) {
            val intent = Intent(this, RegisterInfoActivity::class.java)
            intent.putExtra("data", it)
            startActivity(intent)
        }
        viewModel.msgCodeResult.observe(this, Observer {
            if (it?.success == true) {
                CountDownUtil.smsCountDown(this@LoginOKActivity.lifecycleScope, {
                    binding.btnSendSms.setBtnEnable(false)
                    countDownGoing = true
                }, {
                    binding.btnSendSms.setBtnEnable(false)
                    binding.btnSendSms.text = "${it}s"
                }, {
                    binding.btnSendSms.setBtnEnable(viewModel.accountMsg?.value?.first.isNullOrBlank())
                    binding.btnSendSms.text = getString(R.string.send)
                    countDownGoing = false
                })
            } else {
                it?.msg?.let { msg -> showErrorPromptDialog(msg) {} }
            }
        })
    }

    private fun updateUiWithResult(loginResult: LoginResult) {
        hideLoading()
        val loginData = loginResult.rows?.get(0)
        //将userName信息添加到firebase崩溃日志中
        loginData?.let {
            FirebaseLog.addLogInfo(
                "userName",
                "${loginData}"
            )
        }
        if (loginResult.success) {
            if (loginData?.deviceValidateStatus == 0) {
                PhoneVerifyActivity.loginData = loginData
                startActivity(Intent(this@LoginOKActivity, PhoneVerifyActivity::class.java))
            } else {
                this.run {
//                    if (sConfigData?.thirdOpen == FLAG_OPEN)
//                        MainActivity.reStart(this)
//                    else
                    MainTabActivity.reStart(this,fromLoginOrReg = true)
//                        finish()
                }
            }
        } else {
            if (loginResult.code == SELF_LIMIT) {
                showSelfLimitFrozeErrorDialog(loginResult.msg)
            } else {
                showErrorPromptDialog(
                    getString(R.string.prompt),
                    loginResult.msg
                ) {}
            }
        }
    }

    private fun showErrorDialog(errorMsg: String?) {
        val dialog = CustomAlertDialog(this)
        dialog.setMessage(errorMsg)
        dialog.setNegativeButtonText(null)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show(supportFragmentManager, null)
    }

    private fun showSelfLimitFrozeErrorDialog(errorMsg: String?) {
        val dialog = SelfLimitFrozeErrorDialog()
        dialog.setMessage(errorMsg)
        dialog.show(supportFragmentManager, null)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        AuthManager.facebookCallback(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        AuthManager.googleCallback(requestCode, resultCode, data) { success, msg ->
            if (success) {
                if (msg.isEmptyStr()) {
                    hideLoading()
                } else {
                    viewModel.loginGoogle(msg!!)
                }
            } else {
                hideLoading()
                showErrorDialog(getString(R.string.P038))
            }
        }
    }

    private fun switchLoginType(loginType: Int) {
        viewModel.loginType = loginType
        hideSoftKeyboard(this)
        (loginType == 0).let {
            lin_login_pwd.isVisible = !it
            lin_login_code.isVisible = it
            tv_pwd_login.isVisible = it
            tv_code_login.isVisible = !it
            tv_forget_password.isVisible = !it
            if (it) {
                binding.btnLogin.text =
                    "${getString(R.string.btn_register)} / ${getString(R.string.btn_login)}"
                if (binding.eetAccount.text.isNullOrBlank()) {
                    binding.etAccount.setError(null, false)
                }
                if (binding.eetVerificationCode.text.isNullOrBlank()) {
                    binding.etVerificationCode.setError(null, false)
                }
            } else {
                binding.btnLogin.text = getString(R.string.btn_login)
                if (binding.eetUsername.text.isNullOrBlank()) {
                    binding.etUsername.setError(null, false)
                }
                if (binding.eetPassword.text.isNullOrBlank()) {
                    binding.etPassword.setError(null, false)
                }
            }
            setupRecommendCodeVisible()
            viewModel.focusChangeCheckAllInputComplete()
        }
    }

    private fun setupRecommendCodeVisible() {
        binding.etRecommendCode.isVisible =
            viewModel.loginType == LOGIN_TYPE_CODE && viewModel.checkUserExist.value == false
    }

}