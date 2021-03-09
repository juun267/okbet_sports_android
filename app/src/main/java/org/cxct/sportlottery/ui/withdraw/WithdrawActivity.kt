package org.cxct.sportlottery.ui.withdraw

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_setting_password.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity

class WithdrawActivity : BaseOddButtonActivity<WithdrawViewModel>(WithdrawViewModel::class) {

    companion object {
        const val navigateKey = "NAVIGATE_FROM"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw)

        setupBackButton()
    }

    private fun setupBackButton() {
        btn_back.setOnClickListener {
            finish()
        }
    }
}