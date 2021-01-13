package org.cxct.sportlottery.ui.login.signUp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_register.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.index.ValidCodeResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.util.BitmapUtil
import org.cxct.sportlottery.util.ToastUtil

class RegisterActivity : BaseActivity<RegisterViewModel>(RegisterViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setupBackButton()
        setupRecommendCode()
        setupMemberAccount()
        setupLoginPassword()
        setupConfirmPassword()
        setupFullName()
        setupValidCode()
        setupAgreementButton()
        setupRegisterButton()
        setupGoToLoginButton()
        initObserve()
    }

    private fun checkInputData() {
        viewModel.registerDataChanged(this, et_member_account.getText(), et_login_password.getText(),
            et_confirm_password.getText(), et_full_name.getText(), et_verification_code.getText(), cb_agreement.isChecked)
    }

    private fun setupBackButton() {
        btn_back.setOnClickListener { finish() }
    }

    private fun setupRecommendCode() {
        et_recommend_code.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupMemberAccount() {
        et_member_account.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupLoginPassword() {
        et_login_password.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupConfirmPassword() {
        et_confirm_password.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupFullName() {
        et_full_name.visibility = if (sConfigData?.enableFullName == FLAG_OPEN) View.VISIBLE else View.GONE
        et_full_name.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupValidCode() {
        et_verification_code.visibility = if (sConfigData?.enableRegValidCode == FLAG_OPEN) View.VISIBLE else View.GONE
        et_verification_code.afterTextChanged {
            checkInputData()
        }
    }

    private fun setupAgreementButton() {
        btn_agreement.setOnClickListener {
            val dialog = CustomAlertDialog(this)
            dialog.setTitle(getString(R.string.btn_agreement))
            val message = viewModel.getAgreementContent(this)
            dialog.setMessage(message)
            dialog.setNegativeButtonText(null)
            dialog.setPositiveButtonText(getString(R.string.btn_confirm))
            dialog.setPositiveClickListener(View.OnClickListener {
                dialog.dismiss()
            })
            dialog.show()
        }
    }

    private fun setupRegisterButton() {
        btn_register.setOnClickListener {
            if (viewModel.registerFormState.value?.isDataValid == true) {
                register()
            } else {
               checkInputData()
            }
        }
    }

    private fun updateValidCode() {
        val data = viewModel.validCodeResult.value?.validCodeData
        viewModel.getValidCode(data?.identity)
        et_verification_code.setText(null)
    }

    private fun register() {
//        loading()
//
//        val account = et_account.getText().trim()
//        val password = et_password.getText().trim()
//        val validCodeIdentity = viewModel.validCodeResult.value?.validCodeData?.identity
//        val validCode = et_verification_code.getText().trim()
//        val deviceSn = "" //JPushInterface.getRegistrationID(applicationContext) //極光推播 //TODO 極光推波建置好，要來補齊 deviceSn 參數
//        Timber.d("極光推播: RegistrationID = $deviceSn")
//
//        val loginRequest = LoginRequest(
//            account = account,
//            password = MD5Util.MD5Encode(password),
//            loginSrc = 2,
//            validCodeIdentity = validCodeIdentity,
//            validCode = validCode,
//            deviceSn = deviceSn,
//            appVersion = BuildConfig.VERSION_NAME
//        )
//        viewModel.login(loginRequest, password)
    }

    private fun setupGoToLoginButton() {
        btn_login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun initObserve() {
        viewModel.registerFormState.observe(this, Observer {
            val registerState = it ?: return@Observer
            et_member_account.setError(registerState.memberAccountError)
            et_login_password.setError(registerState.loginPasswordError)
            et_confirm_password.setError(registerState.confirmPasswordError)
            et_full_name.setError(registerState.fullNameError)
            et_verification_code.setError(registerState.validCodeError)
        })

//        viewModel.registerResult.observe(this, Observer {
//            updateUiWithResult(it)
//        })

        viewModel.validCodeResult.observe(this, Observer {
            updateUiWithResult(it)
        })
    }

//    private fun updateUiWithResult(loginResult: LoginResult) {
//        hideLoading()
//        if (loginResult.success) {
//            finish()
//        } else {
//            updateValidCode()
//            showErrorDialog(loginResult.msg)
//        }
//    }

    private fun updateUiWithResult(validCodeResult: ValidCodeResult?) {
        if (validCodeResult?.success == true) {
            val bitmap = BitmapUtil.stringToBitmap(validCodeResult.validCodeData?.img)
            et_verification_code.setVerificationCode(bitmap)
        } else {
            updateValidCode()
            et_verification_code.setVerificationCode(null)
            ToastUtil.showToastInCenter(this@RegisterActivity, getString(R.string.get_valid_code_fail_point))
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
