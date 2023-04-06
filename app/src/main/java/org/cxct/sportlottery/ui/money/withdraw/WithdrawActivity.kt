package org.cxct.sportlottery.ui.money.withdraw

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.util.setTitleLetterSpacing

/**
 * @app_destination 提款
 */
class WithdrawActivity : BaseSocketActivity<WithdrawViewModel>(WithdrawViewModel::class) {
    val mNavController by lazy { findNavController(R.id.withdraw_container) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        setContentView(R.layout.activity_withdraw)

        initToolbar()
//        setupServiceButton()
    }

    private fun initToolbar() {
        tv_toolbar_title.setTitleLetterSpacing()
        tv_toolbar_title.text = getString(R.string.withdraw)
        btn_toolbar_back.setOnClickListener {
            finish()
        }

        tv_toolbar_title_right.setOnClickListener {
            startActivity(Intent(this, BankActivity::class.java))
        }
        tv_toolbar_title_right.isVisible = viewModel.isVisibleView.value ?: true
        tv_toolbar_title_right.text = getString(R.string.withdraw_setting)
        viewModel.isVisibleView.observe(this) {
            tv_toolbar_title_right.isVisible = it
        }
    }

    /*private fun setupServiceButton() {
        btn_floating_service.setView(this)
    }*/
}