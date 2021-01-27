package org.cxct.sportlottery.ui.withdraw

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseToolBarActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity

class BankActivity : BaseToolBarActivity<WithdrawViewModel>(WithdrawViewModel::class) {

    private val mNavController by lazy {
        findNavController(R.id.bank_container)
    }

    override fun setContentView(): Int {
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