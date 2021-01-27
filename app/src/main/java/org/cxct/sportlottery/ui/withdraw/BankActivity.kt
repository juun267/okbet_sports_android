package org.cxct.sportlottery.ui.withdraw

import android.util.Log
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseToolBarActivity

class BankActivity : BaseToolBarActivity<WithdrawViewModel>(WithdrawViewModel::class) {


    private val mNavController by lazy {
        findNavController(R.id.bank_container)
    }

    override fun setContentView(): Int {
        viewModel.apply {
            needToUpdateWithdrawPassword.observe(this@BankActivity, Observer {
                if (it) {
                    //TODO Dean : start update withdraw password Activity
                    Log.e("Dean", "start update withdraw password Activity")
                }
            })
        }
        return R.layout.activity_bank
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