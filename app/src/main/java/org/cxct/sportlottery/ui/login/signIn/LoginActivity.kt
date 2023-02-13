package org.cxct.sportlottery.ui.login.signIn

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.provider.Settings
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import cn.jpush.android.api.JPushInterface
import com.bumptech.glide.Glide
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.view_status_bar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityLoginBinding
import org.cxct.sportlottery.network.index.login.LoginRequest
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.LOGIN_SRC
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.SelfLimitFrozeErrorDialog
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.login.checkRegisterListener
import org.cxct.sportlottery.ui.login.foget.ForgetPasswordActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.widget.boundsEditText.SimpleTextChangedWatcher

/**
 * @app_destination 登入
 */
class LoginActivity : BaseActivity<LoginViewModel>(LoginViewModel::class) {

    private val loginScope = CoroutineScope(Dispatchers.Main)

    private lateinit var binding: ActivityLoginBinding

    companion object {
        private const val SELF_LIMIT = 1130
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .statusBarView(v_statusbar)
            .fitsSystemWindows(false)
            .init()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBackButton()
        setupAccount()
        setupPassword()
        setupValidCode()
        setupLoginButton()
        setupRememberPWD()
        setupForgetPasswordButton()
        setupRegisterButton()
        setupServiceButton()
        initObserve()
        setLetterSpace()
        viewModel.focusChangeCheckAllInputComplete()
    }

    private fun setLetterSpace() {
        if (LanguageManager.getSelectLanguage(this) == LanguageManager.Language.ZH) {
            binding.btnLogin.letterSpacing = 0.6f
        }
    }
    private fun setupBackButton() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupAccount() {
        binding.eetAccount.checkRegisterListener { viewModel.checkAccount(it) }
        binding.eetPassword.checkRegisterListener { viewModel.checkPassword(it) }
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
            adjustEnableLoginButton(true)
        }
    }

    private fun setupValidCode() {
        if (sConfigData?.enableValidCode == FLAG_OPEN) {
            binding.blockValidCode.visibility = View.VISIBLE
            updateValidCode()
        } else {
            binding.blockValidCode.visibility = View.GONE
        }
        binding.ivReturn.setOnClickListener { updateValidCode() }
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            login()
        }
        binding.btnLogin.setTitleLetterSpacing()
    }

    private fun updateValidCode() {
        val data = viewModel.validCodeResult.value?.validCodeData
        viewModel.getValidCode(data?.identity)
        binding.eetVerificationCode.apply {
            if (text.isNotBlank()) {
                text = null
            }
        }
    }

    private fun login() {
        loading()

        val account = binding.eetAccount.text.toString()
        val password = binding.eetPassword.text.toString()
        val validCodeIdentity = viewModel.validCodeResult.value?.validCodeData?.identity
        val validCode = binding.eetVerificationCode.text.toString()
        val deviceSn = JPushInterface.getRegistrationID(applicationContext)
//        val deviceSn =
//            getSharedPreferences(UUID_DEVICE_CODE, Context.MODE_PRIVATE).getString(UUID, "") ?: ""
//        Timber.d("UUID = $deviceSn")
        val deviceId = Settings.Secure.getString(
            applicationContext.contentResolver, Settings.Secure.ANDROID_ID
        )
        val loginRequest = LoginRequest(
            account = account,
            password = MD5Util.MD5Encode(password),
            loginSrc = LOGIN_SRC,
            deviceSn = deviceSn,
            validCodeIdentity = validCodeIdentity,
            validCode = validCode,
            appVersion = BuildConfig.VERSION_NAME,
            loginEnvInfo = deviceId,
        )
        viewModel.login(loginRequest, password)

    }

    private fun setupRememberPWD() {
        binding.cbRememberPassword.isChecked = viewModel.isRememberPWD
        binding.cbRememberPassword.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isRememberPWD = isChecked
        }
    }

    private fun setupForgetPasswordButton() {
        binding.btnForgetPassword.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        binding.btnForgetPassword.getPaint().setAntiAlias(true);//抗锯齿
        binding.btnForgetPassword.setOnClickListener {
//            showPromptDialog(
//                getString(R.string.prompt),
//                getString(R.string.desc_forget_password)
//            ) {}
            startActivity(Intent(this@LoginActivity, ForgetPasswordActivity::class.java))
        }
    }

    private fun setupRegisterButton() {
        binding.tvSignUp.setVisibilityByCreditSystem()
        binding.linRegister.isVisible = !isUAT()
        binding.tvSignUp.setOnClickListener {
            startRegister()
            finish()
        }
    }

    private fun setupServiceButton() {
        binding.btnCustomSerivce.setOnClickListener {
            val serviceUrl = sConfigData?.customerServiceUrl
            val serviceUrl2 = sConfigData?.customerServiceUrl2
            when {
                !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    ServiceDialog().show(supportFragmentManager, null)
                }
                serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(this@LoginActivity, serviceUrl2)
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(this@LoginActivity, serviceUrl)
                }
            }
        }
    }

    private fun initObserve() {
        viewModel.accountMsg.observe(this) {
            binding.etAccount.setError(
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
        viewModel.validateCodeMsg.observe(this) {
            binding.etVerificationCode.setError(
                it.first,
                false
            )
        }
        viewModel.loginEnable.observe(this) {
            adjustEnableLoginButton(it)
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
                startActivity(Intent(this@LoginActivity, PhoneVerifyActivity::class.java))
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
            updateValidCode()
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
            val bitmap = BitmapUtil.stringToBitmap(validCodeResult.validCodeData?.img)
            Glide.with(this)
                .load(bitmap)
                .into(binding.ivVerification)
        } else {
            updateValidCode()
            //et_verification_code.setVerificationCode(null)
            ToastUtil.showToastInCenter(
                this@LoginActivity,
                getString(R.string.get_valid_code_fail_point)
            )
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

    private fun adjustEnableLoginButton(isEnable: Boolean) {
        if (isEnable) {
            binding.btnLogin.isEnabled = true
            binding.btnLogin.alpha = 1.0f
        } else {
            binding.btnLogin.isEnabled = false
            binding.btnLogin.alpha = 0.5f
        }
    }
}