package org.cxct.sportlottery.ui.login.signIn

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.InputType
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.crash.FirebaseLog
import org.cxct.sportlottery.common.event.LoginGlifeOrRegistEvent
import org.cxct.sportlottery.common.event.LoginSelectAccountEvent
import org.cxct.sportlottery.common.event.RegisterInfoEvent
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityLoginOkBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.login.LoginCodeRequest
import org.cxct.sportlottery.network.index.login.LoginRequest
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.repository.LOGIN_SRC
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.login.VerifyCallback
import org.cxct.sportlottery.view.checkRegisterListener
import org.cxct.sportlottery.ui.login.foget.ForgetWaysActivity
import org.cxct.sportlottery.ui.login.selectAccount.SelectAccountActivity
import org.cxct.sportlottery.ui.login.signUp.info.RegisterInfoActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.boundsEditText.SimpleTextChangedWatcher
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import splitties.activities.start


/**
 * @app_destination 登入
 */
class LoginOKActivity : BaseActivity<LoginViewModel,ActivityLoginOkBinding>(), VerifyCallback {

    private val loginScope = CoroutineScope(Dispatchers.Main)
    private val TAG_SEND_MSG = "TAG_SEND_MSG"
    private val TAG_LOGIN = "TAG_LOGIN"
    private val TAG_QUESTION = "TAG_QUESTION"
    private val TAG_ANSWER = "TAG_ANSWER"

    companion object {
        private const val SELF_LIMIT = 1130
        const val LOGIN_TYPE_CODE = 0
        const val LOGIN_TYPE_PWD = 1
        const val LOGIN_TYPE_QUESTION = 2
        const val LOGIN_TYPE_ANSWER = 3

        fun startRegist(context: Context) {
            val intent = Intent(context, LoginOKActivity::class.java)
            intent.putExtra("login_type", LOGIN_TYPE_CODE)
            context.startActivity(intent)
        }
        fun startWithSafeQuestion(context: Context) {
            val intent = Intent(context, LoginOKActivity::class.java)
            intent.putExtra("login_type", LOGIN_TYPE_QUESTION)
            context.startActivity(intent)
        }
    }

    private var countDownGoing = false
    private val loginType by lazy { intent.getIntExtra("login_type", LOGIN_TYPE_PWD) }

    override fun onInitView() {
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .statusBarView(binding.vTop.root)
            .fitsSystemWindows(false)
            .init()
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
        binding.bottomLiences.tvLicense.text = Constants.copyRightString
        initObserve()
        viewModel.focusChangeCheckAllInputComplete()
        EventBusUtil.targetLifecycle(this)
        binding.includeSubtitle.tvSubTitle1.isVisible = false
        binding.includeSubtitle.tvSubTitle2.isVisible = false
        switchLoginType(loginType)
    }

    private fun initOnClick()=binding.run {
        btnBack.setOnClickListener { finish() }
        tvPwdLogin.setOnClickListener { switchLoginType(LOGIN_TYPE_PWD) }
        tvCodeLogin.setOnClickListener { switchLoginType(LOGIN_TYPE_CODE) }
        tvForgetPassword.setOnClickListener { startActivity(ForgetWaysActivity::class.java) }
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
        binding.eetAccount.excludeInputChar("#")
        binding.eetAccount.checkRegisterListener {
            viewModel.checkAccount(it).let { result ->
                if (result.isNullOrBlank() && loginType == LOGIN_TYPE_CODE && !binding.eetAccount.isFocused) {
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
        binding.eetUsername.excludeInputChar("#")
        binding.eetUsername.checkRegisterListener { viewModel.checkUserName(it) }
        binding.eetVerificationCode.checkRegisterListener { viewModel.checkMsgCode(it) }
        binding.etAccount.endIconImageButton.setOnClickListener {
            binding.eetAccount.text = null
        }
        binding.eetUsername1.checkRegisterListener { viewModel.checkUserName(it) }
        binding.eetQuestion.checkRegisterListener { viewModel.checkQuestion(it) }
        binding.eetAnswer.checkRegisterListener { viewModel.checkAnswer(it) }
    }

    private fun setupPassword() {

        binding.etPassword.endIconImageButton.setOnClickListener {
            resetInputTransformationMethod(binding.etPassword, binding.eetPassword)
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
        if (binding.eetAccount.text.isNotEmpty()) {
            binding.btnLogin.setBtnEnable(true)
        }
    }

    private fun setupValidCode() {
        binding.btnSendSms.setOnClickListener { showCaptchaDialog(TAG_SEND_MSG) }
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            login()
        }
        binding.btnLogin.setTitleLetterSpacing()
    }

    private fun updateValidCode(validCodeIdentity: String, validCode: String) {
        val account = binding.eetAccount.text.toString()
        viewModel.loginOrRegSendValidCode(LoginCodeRequest(account).apply { buildParams(validCodeIdentity,validCode) })
        binding.eetVerificationCode.apply {
            if (text.isNotBlank()) {
                text = null
            }
        }
    }

    private fun login() {

        hideSoftKeyboard()
        when(viewModel.loginType){
            LOGIN_TYPE_CODE->{
                val account = binding.eetAccount.text.toString()
                val smsCode = binding.eetVerificationCode.text.toString()
                var inviteCode = binding.eetRecommendCode.text.toString()
                viewModel.loginOrReg(account, smsCode, inviteCode)
            }
            LOGIN_TYPE_PWD->{
                showCaptchaDialog(TAG_LOGIN)
            }
            LOGIN_TYPE_QUESTION->{
                showCaptchaDialog(TAG_QUESTION)
            }
            LOGIN_TYPE_ANSWER->{
                showCaptchaDialog(TAG_ANSWER)
            }
        }
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
                        var inviteCode = binding.eetRecommendCode.text.toString()
                        //新的注册接口
                        val deviceId = Settings.Secure.getString(
                            applicationContext.contentResolver,
                            Settings.Secure.ANDROID_ID
                        )
                        var appVersion = org.cxct.sportlottery.BuildConfig.VERSION_NAME
                        val loginRequest = LoginRequest(
                            account = it.userName?:"",
                            loginSrc = LOGIN_SRC,
                            appVersion = appVersion,
                            loginEnvInfo = deviceId,
                            inviteCode = inviteCode,
                        )
                        viewModel.regPlatformUser(it.token?:"",loginRequest)
                    }
                }
            }
        }
    }

    private fun setupAuthLogin() {
        binding.btnGoogle.clickDelay {
            AuthManager.authGoogle(this@LoginOKActivity)
        }
        binding.btnFacebook.clickDelay {
            AuthManager.authFacebook(this@LoginOKActivity, { token ->
                viewModel.loginFacebook(token)
            }, { errorMsg ->
                if (!errorMsg.isNullOrEmpty()) showErrorDialog(getString(R.string.P472))
            })
        }
    }

    private fun setupPrivacy() = binding.run {
       if (sConfigData?.registerTermsDefaultCheckedSwitch==1){
           layoutPrivacy.root.isVisible = true
           layoutPrivacyNew.root.isVisible = false
           layoutPrivacy.ivPrivacy.isSelected = true
           viewModel.agreeChecked = true
           layoutPrivacy.ivPrivacy.setOnClickListener {
               layoutPrivacy.ivPrivacy.isSelected = !layoutPrivacy.ivPrivacy.isSelected
               if (layoutPrivacy.ivPrivacy.isSelected) {
                   layoutPrivacy.ivPrivacy.setImageResource(R.drawable.ic_radiobtn_1_sel)
               } else {
                   layoutPrivacy.ivPrivacy.setImageResource(R.drawable.ic_radiobtn_1_nor)
               }
               btnGoogle.setBtnEnable(layoutPrivacy.ivPrivacy.isSelected)
               btnFacebook.setBtnEnable(layoutPrivacy.ivPrivacy.isSelected)
               viewModel.agreeChecked = layoutPrivacy.ivPrivacy.isSelected
           }

           layoutPrivacy.tvPrivacy.setVisibilityByMarketSwitch()
           layoutPrivacy.tvPrivacy.makeLinks(
               Pair(
                   applicationContext.getString(R.string.login_privacy_policy),
                   View.OnClickListener {
                       JumpUtil.toInternalWeb(
                           it.context,
                           Constants.getPrivacyRuleUrl(it.context),
                           resources.getString(R.string.login_privacy_policy)
                       )
                   })
           )
           layoutPrivacy.tvPrivacy.makeLinks(
               Pair(
                   applicationContext.getString(R.string.login_terms_conditions),
                   View.OnClickListener {
                       JumpUtil.toInternalWeb(
                           it.context,
                           Constants.getAgreementRuleUrl(it.context),
                           resources.getString(R.string.login_terms_conditions)
                       )
                   })
           )
       }else{
           layoutPrivacy.root.isVisible = false
           layoutPrivacyNew.root.isVisible = true
           layoutPrivacyNew.cbPrivacy.isChecked = false
           viewModel.agreeChecked = false
           btnGoogle.setBtnEnable(false)
           btnFacebook.setBtnEnable(false)
           layoutPrivacyNew.cbPrivacy.setOnCheckedChangeListener { compoundButton, b ->
               btnGoogle.setBtnEnable(b)
               btnFacebook.setBtnEnable(b)
               viewModel.agreeChecked = b
           }
           layoutPrivacyNew.tvPrivacyLine1.makeLinks(
               Pair(
                   applicationContext.getString(R.string.login_privacy_policy_new),
                   View.OnClickListener {
                       JumpUtil.toInternalWeb(
                           it.context,
                           Constants.getPrivacyRuleUrl(it.context),
                           resources.getString(R.string.login_privacy_policy_new)
                       )
                   })
           )
           layoutPrivacyNew.tvPrivacyLine2.makeLinks(
               Pair(
                   applicationContext.getString(R.string.login_terms_conditions_new),
                   View.OnClickListener {
                       JumpUtil.toInternalWeb(
                           it.context,
                           Constants.getAgreementRuleUrl(it.context),
                           resources.getString(R.string.login_terms_conditions_new)
                       )
                   })
           )
       }
    }
    private fun setupServiceButton() {
        binding.layoutPrivacy.tvCustomerService.setServiceClick(supportFragmentManager)
        binding.layoutPrivacyNew.tvCustomerService.setServiceClick(supportFragmentManager)
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
        viewModel.userQuestionEvent.observe(this){
            hideLoading()
            if (it.succeeded()){
                binding.eetQuestion.setText(it.getData()?.safeQuestion)
                switchLoginType(3)
            }else{
                showErrorPromptDialog(it.msg){}
            }
        }
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
            showErrorPromptDialog(
                getString(R.string.prompt),
                loginResult.msg
            ) {}
        }
    }

    private fun showErrorDialog(errorMsg: String?) {
        val dialog = CustomAlertDialog()
        dialog.isError = true
        dialog.setMessage(errorMsg)
        dialog.setNegativeButtonText(null)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
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
        hideSoftKeyboard()
        if (loginType in LOGIN_TYPE_CODE..LOGIN_TYPE_PWD){
            (loginType == LOGIN_TYPE_CODE).let {
                binding.linLoginQuestion.isVisible = false
                binding.linLoginPwd.isVisible = !it
                binding.linLoginCode.isVisible = it
                binding.tvPwdLogin.isVisible = it
                binding.tvCodeLogin.isVisible = !it
                binding.tvForgetPassword.isVisible = !it
                binding.includeSubtitle.tvSubTitle1.isVisible = it
                binding.includeSubtitle.tvSubTitle2.isVisible = it
                if (it) {
                    binding.btnLogin.text =
                        "${getString(R.string.btn_register)} / ${getString(R.string.btn_login)}"
                    (binding.btnLogin.layoutParams as ConstraintLayout.LayoutParams).apply {
                        this.topMargin = 24.dp
                        binding.btnLogin.layoutParams = this
                    }
                    if (binding.eetAccount.text.isNullOrBlank()) {
                        binding.etAccount.setError(null, false)
                    }
                    if (binding.eetVerificationCode.text.isNullOrBlank()) {
                        binding.etVerificationCode.setError(null, false)
                    }
                } else {
                    binding.btnLogin.text = getString(R.string.btn_login)
                    (binding.btnLogin.layoutParams as ConstraintLayout.LayoutParams).apply {
                        this.topMargin = 20.dp
                        binding.btnLogin.layoutParams = this
                    }
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
        }else{
            binding.linLoginQuestion.isVisible = true
            binding.linLoginPwd.isVisible = false
            binding.linLoginCode.isVisible = false
            binding.tvPwdLogin.isVisible = false
            binding.tvCodeLogin.isVisible = true
            binding.tvForgetPassword.isVisible = true
            binding.includeSubtitle.tvSubTitle1.isVisible = true
            binding.includeSubtitle.tvSubTitle2.isVisible = true
            binding.btnLogin.text = getString(R.string.btn_login)
            (binding.btnLogin.layoutParams as ConstraintLayout.LayoutParams).apply {
                this.topMargin = 20.dp
                binding.btnLogin.layoutParams = this
            }
            if (binding.eetUsername1.text.isNullOrBlank()) {
                binding.etUsername1.setError(null, false)
            }
            if (binding.eetQuestion.text.isNullOrBlank()) {
                binding.etQuestion.setError(null, false)
            }
            if (binding.eetAnswer.text.isNullOrBlank()) {
                binding.etAnswer.setError(null, false)
            }
            if (loginType== LOGIN_TYPE_QUESTION){
                binding.etQuestion.isVisible = false
                binding.etAnswer.isVisible = false
            }else {
                binding.etQuestion.isVisible = true
                binding.etAnswer.isVisible = true
                binding.etUsername1.isEnabled = false
                binding.etQuestion.isEnabled = false
            }
        }
    }

    private fun setupRecommendCodeVisible() {
        binding.etRecommendCode.isVisible =
            viewModel.loginType == LOGIN_TYPE_CODE && viewModel.checkUserExist.value == false
    }

    override fun onVerifySucceed(identity: String, validCode: String, tag: String?) {
        if (tag == TAG_SEND_MSG) {
            updateValidCode(identity, validCode)
        } else if (tag == TAG_LOGIN) {
            val account = binding.eetUsername.text.toString()
            val password = binding.eetPassword.text.toString()
            viewModel.login(account, password, "$identity", validCode) {
                LoginVerifyActivity.startLoginVerify(this@LoginOKActivity, it)
            }
        }else if (tag == TAG_QUESTION) {
            loading()
            viewModel.getUserQuestion(binding.eetUsername1.text.toString(),identity,validCode)
        }else if (tag == TAG_ANSWER) {
            viewModel.loginWithSafeQuestion(binding.eetUsername1.text.toString(),binding.eetAnswer.text.toString(),identity,validCode)
        }
    }

}