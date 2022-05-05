package org.cxct.sportlottery.ui.login.signIn

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.lifecycle.Observer
import cn.jpush.android.api.JPushInterface
import com.bumptech.glide.Glide
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
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.util.BitmapUtil
import org.cxct.sportlottery.util.MD5Util
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.widget.boundsEditText.SimpleTextChangedWatcher





class LoginActivity : BaseActivity<LoginViewModel>(LoginViewModel::class) {

    private val loginScope = CoroutineScope(Dispatchers.Main)

    private lateinit var binding: ActivityLoginBinding
    companion object {
        private const val SELF_LIMIT = 1130
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        setUpLoginForGuestButton()
        initObserve()
    }

    private fun setUpLoginForGuestButton() {
        binding.btnVisitFirst.setOnClickListener {
            viewModel.loginAsGuest()
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupAccount() {
        binding.eetAccount.setText(viewModel.account)
//        binding.eetAccount.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
//            if (!hasFocus)
//                checkInputData()
//        }
//        binding.eetAccount.setOnFocusChangeListener { v, hasFocus ->
//            if (!hasFocus)
//                checkInputData()
//        }
    }

    private fun setupPassword() {
        binding.eetPassword.setText(viewModel.password)
        binding.etPassword.endIconImageButton.setOnClickListener {
            if (binding.etPassword.endIconResourceId == R.drawable.ic_eye_open) {
                binding.eetPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.etPassword.setEndIcon(R.drawable.ic_eye_close)
            } else {
                binding.etPassword.setEndIcon(R.drawable.ic_eye_open)
                binding.eetPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
            binding.eetPassword.setSelection(binding.eetPassword.text.toString().length)
        }
        //避免自動記住密碼被人看到，把顯示密碼按鈕功能隱藏，直到密碼被重新編輯才顯示
        if(binding.eetPassword.text.toString().isNotEmpty()) {
            binding.etPassword.endIconImageButton.visibility = View.GONE
        }else{
            binding.etPassword.endIconImageButton.visibility = View.VISIBLE
        }
//        binding.etPassword.setOnFocusChangeListener { v, hasFocus ->
//            if (!hasFocus)
//                checkInputData()
//        }
        binding.etPassword.setSimpleTextChangeWatcher(object : SimpleTextChangedWatcher {
            override fun onTextChanged(theNewText: String?, isError: Boolean) {
                    if (binding.etPassword.endIconImageButton.visibility == View.GONE) {
                        binding.etPassword.endIconImageButton.visibility = View.VISIBLE
                        binding.eetPassword.setText(null)
                    }

            }
        })
        binding.btnLogin.requestFocus()
//        et_password.eyeVisibility = View.GONE
//        et_password.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
//            if (hasFocus && et_password.eyeVisibility == View.GONE) {
//                et_password.eyeVisibility = View.VISIBLE
//                et_password.setText(null)
//            }
//
//            if (!hasFocus)
//                checkInputData()
//        }
    }

    private fun setupValidCode() {
        if (sConfigData?.enableValidCode == FLAG_OPEN) {
            binding.blockValidCode.visibility = View.VISIBLE
            updateValidCode()
        } else {
            binding.blockValidCode.visibility = View.GONE
        }
        binding.ivReturn.setOnClickListener { updateValidCode() }
//        binding.eetVerificationCode.setOnFocusChangeListener { v, hasFocus ->
//            if (!hasFocus)
//                checkInputData()
//        }
//        binding.ivReturn.setVerificationCodeBtnOnClickListener(View.OnClickListener {
//            updateValidCode()
//        })

//        et_verification_code.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
//            if (!hasFocus)
//                checkInputData()
//        }
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            if (checkInputData()) {
                login()
            }
        }
    }

    private fun checkInputData(): Boolean {
        return viewModel.checkInputData(
            this,
            binding.eetAccount.text.toString(),
            binding.eetPassword.text.toString(),
            binding.eetVerificationCode.text.toString()
        )
    }

    private fun updateValidCode() {
        val data = viewModel.validCodeResult.value?.validCodeData
        viewModel.getValidCode(data?.identity)
        binding.eetVerificationCode.setText(null)
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
            loginEnvInfo = deviceId
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
        binding.btnForgetPassword.setOnClickListener {
            showPromptDialog(
                getString(R.string.prompt),
                getString(R.string.desc_forget_password)
            ) {}
        }
    }

    private fun setupRegisterButton() {
        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            finish()
        }
    }

    private fun initObserve() {
        viewModel.loginFormState.observe(this, Observer {
            val loginState = it ?: return@Observer
            binding.etAccount.setError(loginState.accountError, false)
            binding.etPassword.setError(loginState.passwordError, false)
            binding.etVerificationCode.setError(loginState.validCodeError, false)
        })

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
                startActivity(Intent(this@LoginActivity, PhoneVerifyActivity::class.java))
            } else {
                this.run {
                    if (sConfigData?.thirdOpen == FLAG_OPEN)
                        MainActivity.reStart(this)
                    else
                        GamePublicityActivity.reStart(this)
//                        finish()
                }
            }
        } else {
            updateValidCode()
            if(loginResult.code == SELF_LIMIT){
                showSelfLimitFrozeErrorDialog(loginResult.msg)
            }else{
                showErrorDialog(loginResult.msg)
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
}