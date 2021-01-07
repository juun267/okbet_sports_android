package org.cxct.sportlottery.ui.login.signUp

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_register.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel

class RegisterActivity : BaseActivity<LoginViewModel>(LoginViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setupBackButton()
    }

    private fun setupBackButton() {
        btn_back.setOnClickListener { finish() }
    }

}
