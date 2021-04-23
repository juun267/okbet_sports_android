package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.appbar_finance.*
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

        viewModel.recordType.observe(this, Observer {
            setupToolbarTitle(it)

            when (it) {
                getString(R.string.record_recharge) -> {
                    navRechargeLogFragment(it)
                }
                getString(R.string.record_withdrawal) -> {
                    navRechargeWithdrawFragment(it)
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
                        navRechargeLogFragment(it)
                    }
                }
            }
        }
    }

    private fun setupToolbarTitle(title: String) {
        tv_toolbar_title.text = title

    }

    private fun navRechargeLogFragment(type: String) {
        when (navController.currentDestination?.id) {
            R.id.financeFragment -> {
                val action = FinanceFragmentDirections.actionFinanceFragmentToRechargeLogFragment()
                navController.navigate(action)
            }
        }
    }

    private fun navRechargeWithdrawFragment(type: String) {
        when (navController.currentDestination?.id) {
            R.id.financeFragment -> {
                val action = FinanceFragmentDirections.actionFinanceFragmentToWithdrawLogFragment()
                navController.navigate(action)
            }
        }
    }

}