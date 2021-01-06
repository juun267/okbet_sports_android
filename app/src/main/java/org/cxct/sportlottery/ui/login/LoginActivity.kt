package org.cxct.sportlottery.ui.login

import android.os.Bundle
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
        initObserve()
    }

    private fun setupBack() {
        btn_back.setOnClickListener { finish() }
    }

    private fun setupAccount() {
        et_account.afterTextChanged {
            viewModel.loginDataChanged(this, et_account.getText(), et_password.getText())
        }
    }

    private fun setupPassword() {
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