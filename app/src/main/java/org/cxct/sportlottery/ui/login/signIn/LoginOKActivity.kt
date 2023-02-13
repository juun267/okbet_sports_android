package org.cxct.sportlottery.ui.login.signIn

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import cn.jpush.android.api.JPushInterface
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.activity_login_ok.*
import kotlinx.android.synthetic.main.view_status_bar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityLoginOkBinding
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
import org.cxct.sportlottery.ui.login.foget.ForgetPasswordActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.MD5Util
import org.cxct.sportlottery.util.setTitleLetterSpacing
import org.cxct.sportlottery.widget.boundsEditText.SimpleTextChangedWatcher


/**
 * @app_destination 登入
 */
class LoginOKActivity : BaseActivity<LoginViewModel>(LoginViewModel::class) {

    private val loginScope = CoroutineScope(Dispatchers.Main)

    private lateinit var binding: ActivityLoginOkBinding
    private val callbackManager = CallbackManager.Factory.create()

    companion object {
        private const val SELF_LIMIT = 1130
        private const val RC_SIGN_IN = 0x123
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
        setupGoogle()
        setupFacebook()
        setupServiceButton()
        initObserve()
        viewModel.focusChangeCheckAllInputComplete()
    }

    private fun initOnClick() {
        binding.btnBack.setOnClickListener { finish() }
        tv_pwd_login.setOnClickListener { switchLoginType(LOGIN_TYPE_PWD) }
        tv_code_login.setOnClickListener { switchLoginType(LOGIN_TYPE_CODE) }
        tv_forget_password.setOnClickListener {
            startActivity(Intent(this@LoginOKActivity,
                ForgetPasswordActivity::class.java))
        }
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
                SecurityCode = smsCode,
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
                SecurityCode = null,
            )
            viewModel.login(loginRequest, password)
        }
    }

    private fun setupGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()
        var mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        btn_google.setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)

//            val oneTapClient = Identity.getSignInClient(this)
//            var signInRequest = BeginSignInRequest.builder()
//                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
//                    .setSupported(true)
//                    .build())
//                .setGoogleIdTokenRequestOptions(
//                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                        .setSupported(true)
//                        // Your server's client ID, not your Android client ID.
//                        .setServerClientId(getString(R.string.server_client_id))
//                        // Only show accounts previously used to sign in.
//                        .setFilterByAuthorizedAccounts(true)
//                        .build())
//                // Automatically sign in when exactly one credential is retrieved.
//                .setAutoSelectEnabled(true)
//                .build()
//
//            oneTapClient.beginSignIn(signInRequest)
//                .addOnSuccessListener(this) { result ->
//                    try {
//                        startIntentSenderForResult(
//                            result.pendingIntent.intentSender, RC_SIGN_IN,
//                            null, 0, 0, 0, null)
//                    } catch (e: IntentSender.SendIntentException) {
//                        Log.e("hjq", "Couldn't start One Tap UI: ${e.localizedMessage}")
//                    }
//                }
//                .addOnFailureListener(this) { e ->
//                    // No saved credentials found. Launch the One Tap sign-up flow, or
//                    // do nothing and continue presenting the signed-out UI.
//                    Log.d("hjq", e.localizedMessage)
//                }
        }

    }

    private fun setupFacebook() {
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<com.facebook.login.LoginResult> {
                override fun onSuccess(result: com.facebook.login.LoginResult) {
                    LogUtil.toJson(result)
                }

                override fun onError(error: FacebookException) {
                    error.printStackTrace()
                }

                override fun onCancel() {
                    Log.d("hjq", "onCancel")
                }
            })
        btn_feedback.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile"));
//            LoginManager.getInstance()
//                .retrieveLoginStatus(this@LoginOKActivity, object : LoginStatusCallback {
//                    override fun onCompleted(accessToken: AccessToken) {
//                        TODO("Not yet implemented")
//                    }
//
//                    override fun onError(exception: Exception) {
//                        TODO("Not yet implemented")
//                    }
//
//                    override fun onFailure() {
//                        TODO("Not yet implemented")
//                    }
//
//                })
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
                    JumpUtil.toExternalWeb(this@LoginOKActivity, serviceUrl2)
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(this@LoginOKActivity, serviceUrl)
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
//        if (validCodeResult?.success == true) {
//            val bitmap = BitmapUtil.stringToBitmap(validCodeResult.validCodeData?.img)
//            Glide.with(this)
//                .load(bitmap)
//                .into(binding.ivVerification)
//        } else {
//            updateValidCode()
//            //et_verification_code.setVerificationCode(null)
//            ToastUtil.showToastInCenter(
//                this@LoginCodeActivity,
//                getString(R.string.get_valid_code_fail_point)
//            )
//        }
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
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
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
            e.printStackTrace()
            updateUiWithResult(null)
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
}