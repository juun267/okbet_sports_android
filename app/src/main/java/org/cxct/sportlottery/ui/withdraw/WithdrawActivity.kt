package org.cxct.sportlottery.ui.withdraw

import android.os.Bundle
import androidx.navigation.findNavController
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseToolBarActivity

class WithdrawActivity : BaseToolBarActivity<WithdrawViewModel>(WithdrawViewModel::class) {

    private val mNavController by lazy {
        findNavController(R.id.withdraw_container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(): Int {
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