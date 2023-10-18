package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_finance.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity.Companion.RechargeViewLog

/**
 * @app_destination 资金明细
 */
class FinanceActivity : BaseSocketActivity<FinanceViewModel>(FinanceViewModel::class) {

    private val navController by lazy {
        findNavController(R.id.financeFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        setContentView(R.layout.activity_finance)

        setupToolbarBack()

        setupToolbarTitle(getString(R.string.fund_detail))

        viewModel.recordType.observe(this) {
            setupToolbarTitle(it)

            when (it) {
                getString(R.string.record_recharge) -> {
                    navRechargeLogFragment()
                }
                getString(R.string.record_withdrawal) -> {
                    navRechargeWithdrawFragment()
                }
                getString(R.string.record_conversion) -> {
                    if (navController.currentDestination?.id == R.id.financeFragment) {
                        navController.navigate(FinanceFragmentDirections.actionFinanceFragmentToMoneyTransferRecordFragment())
                    }
                }
                getString(R.string.record_history) -> {
                    if (navController.currentDestination?.id == R.id.financeFragment) {
                        navController.navigate(FinanceFragmentDirections.actionFinanceFragmentToAccountHistoryFragment())
                    }
                }
                getString(R.string.redenvelope_record) -> {
                    if (navController.currentDestination?.id == R.id.financeFragment)
                    navController.navigate(FinanceFragmentDirections.actionFinanceFragmentToRedEnvelopeFragment())
                }
            }
        }

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
        custom_tool_bar.titleText = title
    }

    private fun setupToolbarBack() {
        custom_tool_bar.setOnBackPressListener {
            if (financeFragment.childFragmentManager.backStackEntryCount > 0) {
                navController.navigateUp()
                if (!financeFragment.isHidden) setupToolbarTitle(getString(R.string.fund_detail))

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