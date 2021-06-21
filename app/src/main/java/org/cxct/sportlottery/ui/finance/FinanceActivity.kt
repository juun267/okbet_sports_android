package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_finance.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity.Companion.RechargeViewLog


class FinanceActivity : BaseOddButtonActivity<FinanceViewModel>(FinanceViewModel::class) {

    private val navController by lazy {
        findNavController(R.id.financeFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_finance)

        setupToolbarBack()

        setupToolbarTitle(getString(R.string.finance))

        viewModel.recordType.observe(this, {
            setupToolbarTitle(it)

            when (it) {
                getString(R.string.record_recharge) -> {
                    navRechargeLogFragment()
                }
                getString(R.string.record_withdrawal) -> {
                    navRechargeWithdrawFragment()
                }
                getString(R.string.record_conversion) -> {
                    navController.navigate(FinanceFragmentDirections.actionFinanceFragmentToMoneyTransferRecordFragment())
                }
            }
        })

        checkQuickJump()
    }

    private fun checkQuickJump() {
        intent.apply {
            val viewLog = getStringExtra(RechargeViewLog)
            viewLog?.let {
                setupToolbarTitle(it)
                when (it) {
                    getString(R.string.record_recharge) -> {
                        navRechargeLogFragment()
                    }
                }
            }
        }
    }

    private fun setupToolbarTitle(title: String) {
        tv_toolbar_title.text = title
    }

    private fun setupToolbarBack() {
        btn_toolbar_back.setOnClickListener {
            if (financeFragment.childFragmentManager.backStackEntryCount > 0) {
                navController.navigateUp()
                if (!financeFragment.isHidden) setupToolbarTitle(getString(R.string.finance))

            } else finish()
        }
    }

    private fun navRechargeLogFragment() {
        when (navController.currentDestination?.id) {
            R.id.financeFragment -> {
                val action = FinanceFragmentDirections.actionFinanceFragmentToRechargeLogFragment()
                navController.navigate(action)
            }
        }
    }

    private fun navRechargeWithdrawFragment() {
        when (navController.currentDestination?.id) {
            R.id.financeFragment -> {
                val action = FinanceFragmentDirections.actionFinanceFragmentToWithdrawLogFragment()
                navController.navigate(action)
            }
        }
    }

}