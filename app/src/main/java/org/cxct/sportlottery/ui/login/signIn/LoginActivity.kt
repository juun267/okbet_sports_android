package org.cxct.sportlottery.ui.login.signIn

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import cn.jpush.android.api.JPushInterface
import kotlinx.android.synthetic.main.activity_login.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.index.login.LoginRequest
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.LOGIN_SRC
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.util.BitmapUtil
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.MD5Util
import org.cxct.sportlottery.util.ToastUtil
import timber.log.Timber


class LoginActivity : BaseActivity<LoginViewModel>(LoginViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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
        btn_visit_first.setOnClickListener {
            viewModel.loginAsGuest()
        }
    }

    private fun setupBackButton() {
        btn_back.setOnClickListener { finish() }
    }

    private fun setupAccount() {
        et_account.setText(viewModel.account)
        et_account.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupPassword() {
        et_password.setText(viewModel.password)

        //避免自動記住密碼被人看到，把顯示密碼按鈕功能隱藏，直到密碼被重新編輯才顯示
        et_password.eyeVisibility = View.GONE
        et_password.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (hasFocus && et_password.eyeVisibility == View.GONE) {
                et_password.eyeVisibility = View.VISIBLE
                et_password.setText(null)
            }

            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupValidCode() {
        if (sConfigData?.enableValidCode == FLAG_OPEN) {
            et_verification_code.visibility = View.VISIBLE
            updateValidCode()
        } else {
            et_verification_code.visibility = View.GONE
        }

        et_verification_code.setVerificationCodeBtnOnClickListener(View.OnClickListener {
            updateValidCode()
        })

        et_verification_code.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupLoginButton() {
        btn_login.setOnClickListener {
            if (checkInputData()) {
                login()
            }
        }
    }

    private fun checkInputData(): Boolean {
        return viewModel.checkInputData(this, et_account.getText(), et_password.getText(), et_verification_code.getText())
    }

    private fun updateValidCode() {
        val data = viewModel.validCodeResult.value?.validCodeData
        viewModel.getValidCode(data?.identity)
        et_verification_code.setText(null)
    }

    private fun login() {
        loading()

        val account = et_account.getText()
        val password = et_password.getText()
        val validCodeIdentity = viewModel.validCodeResult.value?.validCodeData?.identity
        val validCode = et_verification_code.getText()
        val deviceSn = JPushInterface.getRegistrationID(applicationContext)
        Timber.d("極光推播: RegistrationID = $deviceSn")

        val loginRequest = LoginRequest(
            account = account,
            password = MD5Util.MD5Encode(password),
            loginSrc = LOGIN_SRC,
            deviceSn = deviceSn,
            validCodeIdentity = validCodeIdentity,
            validCode = validCode,
            appVersion = BuildConfig.VERSION_NAME
        )
        viewModel.login(loginRequest, password)
    }

    private fun setupRememberPWD() {
        cb_remember_password.isChecked = viewModel.isRememberPWD
        cb_remember_password.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isRememberPWD = isChecked
        }
    }

    private fun setupForgetPasswordButton() {
        btn_forget_password.setOnClickListener {
            val dialog = CustomAlertDialog(this)
            dialog.setTitle(getString(R.string.prompt))
            dialog.setMessage(getString(R.string.desc_forget_password))
            dialog.setNegativeButtonText(null)
            dialog.setCanceledOnTouchOutside(true)
            dialog.show()
        }
    }

    private fun setupRegisterButton() {
        btn_sign_up.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            finish()
        }
    }

    private fun initObserve() {
        viewModel.loginFormState.observe(this, Observer {
            val loginState = it ?: return@Observer
            et_account.setError(loginState.accountError)
            et_password.setError(loginState.passwordError)
            et_verification_code.setError(loginState.validCodeError)
        })

        viewModel.loginResult.observe(this, Observer {
            updateUiWithResult(it)
        })

        viewModel.validCodeResult.observe(this, Observer {
            updateUiWithResult(it)
        })
    }

    private fun updateUiWithResult(loginResult: LoginResult) {
        hideLoading()
        if (loginResult.success) {
            finish()
        } else {
            updateValidCode()
            showErrorDialog(loginResult.msg)
        }
    }

    private fun updateUiWithResult(validCodeResult: ValidCodeResult?) {
        if (validCodeResult?.success == true) {
            val bitmap = BitmapUtil.stringToBitmap(validCodeResult.validCodeData?.img)
            et_verification_code.setVerificationCode(bitmap)
        } else {
            updateValidCode()
            et_verification_code.setVerificationCode(null)
            ToastUtil.showToastInCenter(this@LoginActivity, getString(R.string.get_valid_code_fail_point))
        }
    }

    private fun showErrorDialog(errorMsg: String?) {
        val dialog = CustomAlertDialog(this)
        dialog.setMessage(errorMsg)
        dialog.setNegativeButtonText(null)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
    }
}