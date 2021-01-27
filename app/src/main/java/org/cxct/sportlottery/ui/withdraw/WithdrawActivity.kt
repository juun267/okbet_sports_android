package org.cxct.sportlottery.ui.withdraw

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseToolBarActivity

class WithdrawActivity : BaseToolBarActivity<WithdrawViewModel>(WithdrawViewModel::class) {

    companion object {
        const val navigateKey = "NAVIGATE_FROM"
    }

    private val mNavController by lazy {
        findNavController(R.id.withdraw_container)
    }

    override fun setContentView(): Int {
        viewModel.apply {
            checkBankCardOrNot.observe(this@WithdrawActivity, Observer {
                if (!it && (mNavController.currentDestination?.id != R.id.bankListFragment)) {
                    showPromptDialog(getString(R.string.withdraw_setting), getString(R.string.please_setting_bank_card)) {
                        Log.e("Dean", "setting")
                        val bundle = Bundle()
                        bundle.putSerializable(navigateKey, PageFrom.WITHDRAW)
                        mNavController.navigate(R.id.bankListFragment, bundle)
                    }
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