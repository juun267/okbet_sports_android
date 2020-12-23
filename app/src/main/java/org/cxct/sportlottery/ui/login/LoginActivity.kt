package org.cxct.sportlottery.ui.login

import androidx.lifecycle.Observer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.activity_login.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityLoginBinding
import org.cxct.sportlottery.interfaces.OnCheckConnectClickListener
import org.cxct.sportlottery.network.index.LoginResult
import org.cxct.sportlottery.ui.base.BaseActivity


class LoginActivity : BaseActivity<LoginViewModel>(LoginViewModel::class) {

    private lateinit var loginBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        loginBinding.apply {
            loginViewModel = this@LoginActivity.viewModel
            lifecycleOwner = this@LoginActivity
        }

        viewModel.loginFormState.observe(this, Observer {
            val loginState = it ?: return@Observer

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        viewModel.baseResult.observe(this, Observer {
            loading.visibility = View.GONE

            when (it) {
                is LoginResult? -> {
                    updateUiWithResult(it)
                }
            }
        })

        setupUserName()
        setupPassword()
        setupLoginButton()
    }

    private fun setupUserName() {
        username.afterTextChanged {
            viewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }
    }

    private fun setupPassword() {
        password.apply {
            afterTextChanged {
                viewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        viewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }
        }
    }

    private fun setupLoginButton() {

        login.setOnClickListener(
            OnCheckConnectClickListener(
                this,
                object : OnCheckConnectClickListener.Doing {
                    override fun onClick() {
                        loading.visibility = View.VISIBLE
                        viewModel.login(username.text.toString(), password.text.toString())
                    }
                })
        )

    }

    private fun updateUiWithResult(loginResult: LoginResult?) {
        if (loginResult != null && loginResult.success) {
            loginResult.loginData?.let { loginData ->
                updateUiWithUser(loginData.userName)
            }
            finish()
        } else {
            loginResult?.let { loginFailedResult ->
                showLoginFailed(loginFailedResult.msg)
            }
        }
    }

    private fun updateUiWithUser(displayName: String) {
        val welcome = getString(R.string.welcome)
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(error: String) {
        Toast.makeText(applicationContext, error, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}