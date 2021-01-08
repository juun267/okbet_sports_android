package org.cxct.sportlottery.ui.login.signUp

import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_register.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.login.signIn.LoginActivity

class RegisterActivity : BaseActivity<RegisterViewModel>(RegisterViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setupBackButton()
        setupAgreementButton()
        setupLoginButton()
    }

    private fun setupBackButton() {
        btn_back.setOnClickListener { finish() }
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

    private fun setupLoginButton() {
        btn_login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
