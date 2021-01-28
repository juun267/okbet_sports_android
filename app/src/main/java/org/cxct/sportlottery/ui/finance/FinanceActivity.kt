package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.appbar_finance.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity

class FinanceActivity : BaseActivity<FinanceViewModel>(FinanceViewModel::class) {

    private val navController by lazy {
        findNavController(R.id.financeFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_finance)

        viewModel.recordType.observe(this, Observer {
            setupToolbar(it)

            when (it) {
                getString(R.string.record_recharge) -> {
                    navRechargeLogFragment(it)
                }
                getString(R.string.record_withdrawal) -> {
                    navRechargeWithdrawFragment(it)
                }
            }
        })
    }

    private fun setupToolbar(title: String) {
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