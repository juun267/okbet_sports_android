package org.cxct.sportlottery.ui.withdraw

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_withdraw.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketActivity

class WithdrawActivity : BaseSocketActivity<WithdrawViewModel>(WithdrawViewModel::class) {

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