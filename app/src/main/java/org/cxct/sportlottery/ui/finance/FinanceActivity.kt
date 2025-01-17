package org.cxct.sportlottery.ui.finance

import androidx.navigation.findNavController
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityFinanceBinding
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity.Companion.RechargeViewLog

/**
 * @app_destination 资金明细
 */
class FinanceActivity : BaseSocketActivity<FinanceViewModel, ActivityFinanceBinding>(FinanceViewModel::class) {

    override fun pageName() = "资金明细页面"

    private val navController by lazy {
        findNavController(R.id.financeFragment)
    }
    private val financeFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.financeFragment)
    }
    private val viewLog by lazy { intent.getStringExtra(RechargeViewLog) }

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
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
        viewLog?.let {
            setupToolbarTitle(it)
            when (it) {
                getString(R.string.record_recharge) -> {
                    navRechargeLogFragment()
                }
            }
        }
    }

    private fun setupToolbarTitle(title: String) {
        binding.customToolBar.titleText = title
    }

    private fun setupToolbarBack() {
        binding.customToolBar.setOnBackPressListener {
            onBackPressed()
        }
    }
    override fun onBackPressed() {
        financeFragment?.let {
            if (it.childFragmentManager.backStackEntryCount > 0 && viewLog.isNullOrEmpty()) {
                navController.navigateUp()
                if (!it.isHidden) setupToolbarTitle(getString(R.string.fund_detail))

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