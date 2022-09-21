package org.cxct.sportlottery.ui.withdraw

import android.content.Intent
import android.os.Bundle
import android.view.View
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
        if (viewModel.isVisibleView.value == false){
            tv_toolbar_title_right.visibility = View.GONE
        }else{
            tv_toolbar_title_right.visibility = View.VISIBLE
        }
        tv_toolbar_title_right.text = getString(R.string.withdraw_setting)

    }

    /*private fun setupServiceButton() {
        btn_floating_service.setView(this)
    }*/
}