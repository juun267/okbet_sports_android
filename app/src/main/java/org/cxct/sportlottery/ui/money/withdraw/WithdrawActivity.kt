package org.cxct.sportlottery.ui.money.withdraw

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.ActivityWithdrawBinding
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.util.setTitleLetterSpacing

/**
 * @app_destination 提款
 */
class WithdrawActivity : BindingActivity<WithdrawViewModel,ActivityWithdrawBinding>() {

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        initToolbar()
        viewModel.isVisibleView.observe(this) {
            binding.toolBar.tvToolbarTitleRight.isVisible = it
        }
    }

    private fun initToolbar() =binding.toolBar.run{
        tvToolbarTitle.setTitleLetterSpacing()
        tvToolbarTitle.text = getString(R.string.withdraw)
        btnToolbarBack.setOnClickListener {
            finish()
        }

        tvToolbarTitleRight.setOnClickListener {
            startActivity(BankActivity::class.java)
        }
        tvToolbarTitleRight.isVisible = viewModel.isVisibleView.value ?: true
        tvToolbarTitleRight.text = getString(R.string.withdraw_setting)
    }

}