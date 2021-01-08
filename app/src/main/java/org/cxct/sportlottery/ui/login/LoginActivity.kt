package org.cxct.sportlottery.ui.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_login.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.index.LoginResult
import org.cxct.sportlottery.ui.base.BaseActivity


class LoginActivity : BaseActivity<LoginViewModel>(LoginViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupBack()
        setupAccount()
        setupPassword()
        setupLoginButton()
        setupRememberPWD()
        initObserve()
    }

    private fun setupBack() {
        btn_back.setOnClickListener { finish() }
    }

    private fun setupAccount() {
        et_account.setText(viewModel.account)
        et_account.afterTextChanged {
            viewModel.loginDataChanged(this, et_account.getText(), et_password.getText())
        }
    }

    private fun setupPassword() {
        et_password.setText(viewModel.password)

        //避免自動記住密碼被人看到，把顯示密碼按鈕功能隱藏，直到密碼被重新編輯才顯示
        et_password.eyeVisibility = View.GONE
        et_password.getEditText().setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && et_password.eyeVisibility == View.GONE) {
                et_password.eyeVisibility = View.VISIBLE
                et_password.setText(null)
            }
        }

        et_password.afterTextChanged {
            viewModel.loginDataChanged(this, et_account.getText(), et_password.getText())
        }
    }

    private fun setupLoginButton() {
        btn_login.setOnClickListener {
            if (viewModel.loginFormState.value?.isDataValid == true) {
                loading()
                viewModel.login(et_account.getText(), et_password.getText())
            } else {
                viewModel.loginDataChanged(this, et_account.getText(), et_password.getText())
            }
        }
    }

    private fun setupRememberPWD() {
        cb_remember_password.isChecked = viewModel.isRememberPWD
        cb_remember_password.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isRememberPWD = isChecked
        }
    }

    private fun initObserve() {
        viewModel.loginFormState.observe(this, Observer {
            val loginState = it ?: return@Observer
            et_account.setError(loginState.accountError)
            et_password.setError(loginState.passwordError)
        })

        viewModel.loginResult.observe(this, Observer {
            hideLoading()
            updateUiWithResult(it)
        })
    }

    private fun updateUiWithResult(loginResult: LoginResult) {
        if (loginResult.success) {
            finish()
        } else {
            showLoginFailed(loginResult.msg)
        }
    }

    private fun showLoginFailed(error: String) {
        Toast.makeText(applicationContext, error, Toast.LENGTH_SHORT).show()
    }
}