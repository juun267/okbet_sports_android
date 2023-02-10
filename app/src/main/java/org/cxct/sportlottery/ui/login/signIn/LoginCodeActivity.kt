package org.cxct.sportlottery.ui.login.signIn

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import cn.jpush.android.api.JPushInterface
import com.bumptech.glide.Glide
import com.facebook.*
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.activity_login_code.*
import kotlinx.android.synthetic.main.view_status_bar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityLoginCodeBinding
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.SelfLimitFrozeErrorDialog
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.login.checkRegisterListener
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.*


/**
 * @app_destination 登入
 */
class LoginCodeActivity : BaseActivity<LoginViewModel>(LoginViewModel::class) {

    private val loginScope = CoroutineScope(Dispatchers.Main)

    private lateinit var binding: ActivityLoginCodeBinding
    private val callbackManager = CallbackManager.Factory.create()

    companion object {
        private const val SELF_LIMIT = 1130
        private const val RC_SIGN_IN = 0x123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .statusBarView(v_statusbar)
            .fitsSystemWindows(false)
            .init()
        binding = ActivityLoginCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBackButton()
        setupAccount()
        setupValidCode()
        setupLoginButton()
        setupGoogle()
        setupFacebook()
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
        binding.eetVerificationCode.checkRegisterListener { viewModel.checkValidCode(it) }
        if (!viewModel.account.isNullOrBlank()) {
            binding.eetAccount.setText(viewModel.account)
        }
        binding.etAccount.endIconImageButton.setOnClickListener {
            binding.eetAccount.text = null
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
        val validCodeIdentity = viewModel.validCodeResult.value?.validCodeData?.identity
        val validCode = binding.eetVerificationCode.text.toString()
        val deviceSn = JPushInterface.getRegistrationID(applicationContext)
//        val deviceSn =
//            getSharedPreferences(UUID_DEVICE_CODE, Context.MODE_PRIVATE).getString(UUID, "") ?: ""
//        Timber.d("UUID = $deviceSn")
        val deviceId = Settings.Secure.getString(
            applicationContext.contentResolver, Settings.Secure.ANDROID_ID
        )
//        val loginRequest = LoginRequest(
//            account = account,
//            password = MD5Util.MD5Encode(password),
//            loginSrc = LOGIN_SRC,
//            deviceSn = deviceSn,
//            validCodeIdentity = validCodeIdentity,
//            validCode = validCode,
//            appVersion = BuildConfig.VERSION_NAME,
//            loginEnvInfo = deviceId,
//        )
//        viewModel.login(loginRequest, password)

    }

    private fun setupGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        var mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        google_btn.setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun setupFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<com.facebook.login.LoginResult> {
                override fun onSuccess(result: com.facebook.login.LoginResult) {
                    TODO("Not yet implemented")
                }

                override fun onError(error: FacebookException) {
                    TODO("Not yet implemented")
                }

                override fun onCancel() {
                    TODO("Not yet implemented")
                }
            })
        facebook_btn.setOnClickListener {
            LoginManager.getInstance()
                .retrieveLoginStatus(this@LoginCodeActivity, object : LoginStatusCallback {
                    override fun onCompleted(accessToken: AccessToken) {
                        TODO("Not yet implemented")
                    }

                    override fun onError(exception: Exception) {
                        TODO("Not yet implemented")
                    }

                    override fun onFailure() {
                        TODO("Not yet implemented")
                    }

                })
        }
    }

    private fun setupRegisterButton() {
        binding.tvSignUp.setVisibilityByCreditSystem()
        binding.linRegister.isVisible = !isUAT()
        binding.tvSignUp.setOnClickListener {
            startRegister(this@LoginCodeActivity)
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
                    JumpUtil.toExternalWeb(this@LoginCodeActivity, serviceUrl2)
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(this@LoginCodeActivity, serviceUrl)
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
                startActivity(Intent(this@LoginCodeActivity, PhoneVerifyActivity::class.java))
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
                this@LoginCodeActivity,
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
//            updateUiWithResult(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("hjq", "signInResult:failed code=" + e.statusCode)
            updateUiWithResult(null)
        }
    }
}