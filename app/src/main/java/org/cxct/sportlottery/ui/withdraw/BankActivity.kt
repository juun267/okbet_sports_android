package org.cxct.sportlottery.ui.withdraw

import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_bank.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import timber.log.Timber

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
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        setContentView(R.layout.activity_bank)

        setupBankSetting()
        setupBackButton()
    }


    override fun onResume() {
        super.onResume()
        checkMode()
    }

    private fun checkMode() {
        val addBank = intent.getIntExtra("add_bank", 0)
        if (addBank == 1) {
            val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(
                null,
                TransferType.BANK
            )
            mNavController.navigate(action)
        }

        val modifyType = intent.getSerializableExtra(ModifyBankTypeKey)?.let { it as TransferType? }
        val transferTypeAddSwitch =
            intent.getParcelableExtra<TransferTypeAddSwitch>(TransferTypeAddSwitch)
//        Timber.e("BankActivity transferTypeAddSwitch: $transferTypeAddSwitch")
        modifyType?.let { type ->
            when (type) {
                TransferType.BANK -> {
                    val action =
                        BankListFragmentDirections.actionBankListFragmentToBankCardFragment(
                            null,
                            type,
                            transferTypeAddSwitch
                        )
                    mNavController.navigate(action)
                }
                TransferType.CRYPTO -> {
                    val action =
                        BankListFragmentDirections.actionBankListFragmentToBankCardFragment(
                            null,
                            type,
                            transferTypeAddSwitch
                        )
                    mNavController.navigate(action)
                }
                TransferType.E_WALLET -> {
                    val action =
                        BankListFragmentDirections.actionBankListFragmentToBankCardFragment(
                            null,
                            type,
                            transferTypeAddSwitch
                        )
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
        custom_tool_bar.setOnBackPressListener {
            onBackPressed()
        }
    }

    fun changeTitle(title: String) {
        custom_tool_bar.titleText = title
    }
}