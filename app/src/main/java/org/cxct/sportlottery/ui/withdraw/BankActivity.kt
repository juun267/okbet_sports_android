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

    private var startForResult: ActivityResultLauncher<Intent>? = null
    private val mNavController by lazy {
        findNavController(R.id.bank_container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK){
                viewModel.getBankCardList()
            }else{
                viewModel.checkPermissions()
            }
        }
    }

    override fun setContentView(): Int {
        viewModel.apply {
            needToUpdateWithdrawPassword.observe(this@BankActivity, Observer {
                if (it == true) {
                    showPromptDialog(getString(R.string.withdraw_setting), getString(R.string.please_setting_withdraw_password)) {
                        val intent = Intent(this@BankActivity, SettingPasswordActivity::class.java).putExtra(WithdrawFragment.PWD_PAGE, SettingPasswordActivity.PwdPage.BANK_PWD)
                        startForResult?.launch(intent)
                    }
                }
            })

            userInfo.observe(this@BankActivity, Observer {
                viewModel.checkPermissions()
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