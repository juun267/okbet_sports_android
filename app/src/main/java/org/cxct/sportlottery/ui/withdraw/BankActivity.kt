package org.cxct.sportlottery.ui.withdraw

import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_bank.*
import kotlinx.android.synthetic.main.activity_setting_password.btn_back
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.TransferType
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity

class BankActivity : BaseOddButtonActivity<WithdrawViewModel>(WithdrawViewModel::class) {

    companion object {
        const val ModifyBankTypeKey = "modify_bank_type_key"
    }

    private val mNavController by lazy {
        findNavController(R.id.bank_container)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank)

        setupBankSetting()
        setupBackButton()
    }


    override fun onResume() {
        super.onResume()
        checkMode()
    }

    private fun checkMode() {
        val modifyType = intent.getSerializableExtra(ModifyBankTypeKey)?.let { it as TransferType? }
        modifyType?.let { type ->
            when (type) {
                TransferType.BANK -> {
                    val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(null, modifyType)
                    mNavController.navigate(action)
                }
                TransferType.CRYPTO -> {
                    val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(null, modifyType)
                    mNavController.navigate(action)
                }
            }
        }
    }

    private fun setupBankSetting() {
        viewModel.getMoneyConfigs()
    }

    override fun onBackPressed() {
        if (mNavController.previousBackStackEntry != null) {
            mNavController.popBackStack()
            return
        }
        finish()
    }

    private fun setupBackButton() {
        btn_back.setOnClickListener {
            finish()
        }
    }

    fun changeTitle(title: String) {
        tv_title.text = title
    }
}