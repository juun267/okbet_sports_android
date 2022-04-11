package org.cxct.sportlottery.ui.withdraw

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_withdraw.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketActivity

class WithdrawActivity : BaseSocketActivity<WithdrawViewModel>(WithdrawViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw)

        initToolbar()
//        setupServiceButton()
    }

    private fun initToolbar() {
        tv_toolbar_title.text = getString(R.string.withdraw)
        btn_toolbar_back.setOnClickListener {
            finish()
        }
    }

    /*private fun setupServiceButton() {
        btn_floating_service.setView(this)
    }*/
}