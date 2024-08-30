package org.cxct.sportlottery.ui.money.withdraw

import androidx.navigation.findNavController
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.databinding.ActivityBankBinding
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.ui.base.BaseActivity

/**
 * @app_destination 提款設置
 */
class BankActivity : BaseActivity<WithdrawViewModel, ActivityBankBinding>() {

    override fun pageName() = "提款设置页面"

    companion object {
        const val ModifyBankTypeKey = "modify_bank_type_key"
        const val TransferTypeAddSwitch = "TransferTypeAddSwitch"
    }

    private val mNavController by lazy {
        findNavController(R.id.bank_container)
    }

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
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
        binding.customToolBar.setOnBackPressListener {
            onBackPressed()
        }
    }

    fun changeTitle(title: String) {
        binding.customToolBar.titleText = title
    }

}