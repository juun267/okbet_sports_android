package org.cxct.sportlottery.ui.withdraw

import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_bank.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.ui.base.BaseSocketActivity

/**
 * @app_destination 提款設置
 */
class BankActivity : BaseSocketActivity<WithdrawViewModel>(WithdrawViewModel::class) {

    companion object {
        const val ModifyBankTypeKey = "modify_bank_type_key"
        const val TransferTypeAddSwitch = "TransferTypeAddSwitch"
    }

    private val mNavController by lazy {
        findNavController(R.id.bank_container)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        setContentView(R.layout.activity_bank)
        setupBankSetting()
        setupBackButton()
    }


    override fun onResume() {
        super.onResume()
        checkMode()
    }

    private fun checkMode() = runWithCatch {
        when (intent.getIntExtra("add_bank", 0)) { //从提款页面携带数据跳转相应的新增Tab
            1 -> {
                goTOTabFragment(TransferType.BANK)
            }
            2 -> {
                goTOTabFragment(TransferType.CRYPTO)
            }
            3 -> {
                goTOTabFragment(TransferType.E_WALLET)
            }
        }

        val modifyType = (intent.getSerializableExtra(ModifyBankTypeKey) as TransferType?) ?: return
        val transferTypeAddSwitch =
            intent.getParcelableExtra<TransferTypeAddSwitch>(TransferTypeAddSwitch)
        val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(
            null, modifyType, transferTypeAddSwitch
        )
        mNavController.navigate(action)
    }

    private fun goTOTabFragment(type: TransferType) {
        if (mNavController.currentDestination?.id != R.id.bankCardFragment) {
            val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(
                null,
                type,
            )
            mNavController.navigate(action)
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
        custom_tool_bar.setOnBackPressListener {
            onBackPressed()
        }
    }

    fun changeTitle(title: String) {
        custom_tool_bar.titleText = title
    }
}