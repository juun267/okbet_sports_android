package org.cxct.sportlottery.ui.withdraw

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseToolBarActivity

class WithdrawActivity : BaseToolBarActivity<WithdrawViewModel>(WithdrawViewModel::class) {

    companion object {
        val navigateKey = "NAVIGATE_FROM"
    }

    private val mNavController by lazy {
        findNavController(R.id.withdraw_container)
    }

    override fun setContentView(): Int {
        viewModel.apply {
            checkBankCardOrNot.observe(this@WithdrawActivity, Observer {
                if (!it) {
                    if (mNavController.currentDestination?.id != R.id.bankListFragment) {
                        val bundle = Bundle()
                        bundle.putSerializable(navigateKey, PageFrom.WITHDRAW)
                        mNavController.navigate(R.id.bankListFragment, bundle)
                    }
                }
            })
            needToUpdateWithdrawPassword.observe(this@WithdrawActivity, Observer {
                if (it) {
                    //TODO Dean : start update withdraw password Activity
                    Log.e("Dean", "start update withdraw password Activity")
                }
            })
        }

        return R.layout.activity_withdraw
    }

    override fun setToolBarName(): String {
        return getString(R.string.withdraw)
    }

    override fun onBackPressed() {
        if (mNavController.previousBackStackEntry != null) {
            mNavController.popBackStack()
            return
        }
        super.onBackPressed()
    }
}