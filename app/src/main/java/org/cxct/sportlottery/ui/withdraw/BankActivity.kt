package org.cxct.sportlottery.ui.withdraw

import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_bank.*
import kotlinx.android.synthetic.main.activity_setting_password.btn_back
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity

class BankActivity : BaseActivity<WithdrawViewModel>(WithdrawViewModel::class) {

    private val mNavController by lazy {
        findNavController(R.id.bank_container)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank)

        setupBackButton()
    }

    override fun onBackPressed() {
        if (mNavController.previousBackStackEntry != null) {
            mNavController.popBackStack()
            return
        }
    }

    private fun setupBackButton() {
        btn_back.setOnClickListener {
            finish()
        }
    }

    fun changeTitle(title: String) {
        tv_title.text = title
    }
}