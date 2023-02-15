package org.cxct.sportlottery.ui.login.signIn

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import cn.jpush.android.api.JPushInterface
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.activity_login_ok.*
import kotlinx.android.synthetic.main.view_status_bar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityLoginOkBinding
import org.cxct.sportlottery.extentions.startActivity
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.login.LoginCodeRequest
import org.cxct.sportlottery.network.index.login.LoginRequest
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.repository.LOGIN_SRC
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.SelfLimitFrozeErrorDialog
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.login.checkRegisterListener
import org.cxct.sportlottery.ui.login.foget2.ForgetWaysActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.widget.boundsEditText.SimpleTextChangedWatcher
import java.util.*


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
    }

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
        initOnClick()
        setupAccount()
        setupPassword()
        setupValidCode()
        setupLoginButton()
        setupAuthLogin()
        setupPrivacy()
        setupServiceButton()
        initObserve()
        viewModel.focusChangeCheckAllInputComplete()
    }

    private fun initOnClick() {
        binding.btnBack.setOnClickListener { finish() }
        tv_pwd_login.setOnClickListener { switchLoginType(LOGIN_TYPE_PWD) }
        tv_code_login.setOnClickListener { switchLoginType(LOGIN_TYPE_CODE) }
        tv_forget_password.setOnClickListener { startActivity(ForgetWaysActivity::class.java) }
    }

    private fun setupAccount() {
        binding.eetAccount.checkRegisterListener { viewModel.checkAccount(it) }
        binding.eetPassword.checkRegisterListener { viewModel.checkPassword(it) }
        binding.eetUsername.checkRegisterListener { viewModel.checkUserName(it) }
        binding.eetVerificationCode.checkRegisterListener { viewModel.checkValidCode(it) }
        if (!viewModel.account.isNullOrBlank()) {
            binding.eetAccount.setText(viewModel.account)
        }
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
            binding.btnLogin.adjustEnableButton(true)
        }
    }

    private fun setupValidCode() {
        binding.btnSendSms.setOnClickListener { updateValidCode() }
    }

    private fun setupLoginButton() {
        btn_login.text = "${getString(R.string.btn_login)} / ${getString(R.string.btn_register)}"
        binding.btnLogin.setOnClickListener {
            login()
        }
        binding.btnLogin.setTitleLetterSpacing()
    }

    private fun updateValidCode() {
        val account = binding.eetAccount.text.toString()
        viewModel.loginOrRegSendValidCode(LoginCodeRequest(account, ""))
        binding.eetVerificationCode.apply {
            if (text.isNotBlank()) {
                text = null
            }
        }
    }

    private fun login() {
        loading()
        val deviceSn = JPushInterface.getRegistrationID(applicationContext)
        val deviceId = Settings.Secure.getString(applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID)
        var appVersion = org.cxct.sportlottery.BuildConfig.VERSION_NAME
        if (lin_login_code.isVisible) {
            val account = binding.eetAccount.text.toString()
            val smsCode = binding.eetVerificationCode.text.toString()
            val loginRequest = LoginRequest(
                account = account,
                password = null,
                loginSrc = LOGIN_SRC,
                deviceSn = deviceSn,
                appVersion = appVersion,
                loginEnvInfo = deviceId,
                securityCode = smsCode,
            )
            viewModel.loginOrReg(loginRequest)
        } else {
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
                validCode = null
            )
            viewModel.login(loginRequest, password)
        }
    }

    private fun setupAuthLogin() {
        btn_google.setOnClickListener {
            AuthManager.authGoogle(this@LoginOKActivity)
        }
        btn_facebook.setOnClickListener {
            AuthManager.authFacebook(this@LoginOKActivity, { token ->
                viewModel.loginFacebook(token)
            }, { errorMsg ->
                showErrorDialog(errorMsg)
            })
        }
    }

    private fun setupPrivacy() {
        binding.tvPrivacy.makeLinks(
            Pair(
                applicationContext.getString(R.string.login_privacy_policy),
                View.OnClickListener {
                    JumpUtil.toInternalWeb(
                        this,
                        Constants.getPrivacyRuleUrl(this),
                        resources.getString(R.string.privacy_policy)
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
        binding.tvCustomerService.setOnClickListener {
            val serviceUrl = sConfigData?.customerServiceUrl
            val serviceUrl2 = sConfigData?.customerServiceUrl2
            when {
                !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    ServiceDialog().show(supportFragmentManager, null)
                }
                serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(this@LoginOKActivity, serviceUrl2)
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(this@LoginOKActivity, serviceUrl)
                }
            }
        }
    }

    private fun initObserve() {
        viewModel.isLoading.observe(this) {
            if (it) {
                loading()
            } else {
                hideLoading()
            }
        }

        viewModel.accountMsg.observe(this) {
            binding.etAccount.setError(
                it.first,
                false
            )
            binding.btnSendSms.adjustEnableButton(it.first.isNullOrBlank())
        }
        viewModel.validateCodeMsg.observe(this) {
            binding.etVerificationCode.setError(
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
            binding.btnLogin.adjustEnableButton(it)
        }

        viewModel.loginResult.observe(this, Observer {
            loginScope.launch {
                updateUiWithResult(it)
            }
        })

        viewModel.validCodeResult.observe(this, Observer {
            updateUiWithResult(it)
        })
    }

    private fun updateUiWithResult(loginResult: LoginResult) {
        hideLoading()
        if (loginResult.success) {
            if (loginResult.loginData?.deviceValidateStatus == 0) {
                PhoneVerifyActivity.loginData = loginResult.loginData
                startActivity(Intent(this@LoginOKActivity, PhoneVerifyActivity::class.java))
            } else {
                this.run {
//                    if (sConfigData?.thirdOpen == FLAG_OPEN)
//                        MainActivity.reStart(this)
//                    else
                    MainTabActivity.reStart(this)
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

    private fun updateUiWithResult(validCodeResult: ValidCodeResult?) {
        if (validCodeResult?.success == true) {
            showCountDown()
        } else {
            validCodeResult?.msg?.let { msg -> showErrorPromptDialog(msg) {} }
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

    private fun View.adjustEnableButton(isEnable: Boolean) {
        if (isEnable) {
            isEnabled = true
            alpha = 1.0f
        } else {
            isEnabled = false
            alpha = 0.5f
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        AuthManager.facebookCallback(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        AuthManager.googleCallback(requestCode, resultCode, data) { success, msg ->
            if (success) {
                msg?.let {
                    viewModel.loginGoogle(it)
                }
            } else {
                showErrorDialog(msg)
            }
        }
    }

    private fun switchLoginType(loginType: Int) {
        viewModel.loginType = loginType
        (loginType == 0).let {
            lin_login_pwd.isVisible = !it
            lin_login_code.isVisible = it
            tv_pwd_login.isVisible = it
            tv_code_login.isVisible = !it
            tv_forget_password.isVisible = !it
        }
    }

    private var mSmsTimer: Timer? = null
    private fun showCountDown() {
        try {
            stopSmeTimer()

            var sec = 90
            mSmsTimer = Timer()
            mSmsTimer?.schedule(object : TimerTask() {
                override fun run() {
                    Handler(Looper.getMainLooper()).post {
                        if (sec-- > 0) {
                            binding.btnSendSms.adjustEnableButton(false)
                            binding.btnSendSms.text = "${sec}s"
                        } else {
                            stopSmeTimer()
                            binding.btnSendSms.adjustEnableButton(true)
                            binding.btnSendSms.text =
                                getString(R.string.get_security_code)
                        }
                    }
                }
            }, 0, 1000) //在 0 秒後，每隔 1000L 毫秒執行一次
        } catch (e: Exception) {
            e.printStackTrace()

            stopSmeTimer()
            binding.btnSendSms.adjustEnableButton(true)
            binding.btnSendSms.text = getString(R.string.get_verification_code)
        }
    }

    private fun stopSmeTimer() {
        if (mSmsTimer != null) {
            mSmsTimer?.cancel()
            mSmsTimer = null
        }
    }

}