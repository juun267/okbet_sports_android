package org.cxct.sportlottery.ui.money.withdraw

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_bank_list.*
import kotlinx.android.synthetic.main.fragment_bank_list.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.extentions.toLongS
import org.cxct.sportlottery.network.bank.my.BankCardList
import org.cxct.sportlottery.network.money.config.PAYMAYA
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.util.ToastUtil

/**
 * @app_destination 提款设置
 */
class BankListFragment : BaseFragment<WithdrawViewModel>(WithdrawViewModel::class) {

    private val mNavController by lazy {
        findNavController()
    }
    var cardTypeTitle = ""

    private val mBankListAdapter by lazy {
        BankCardListAdapter(
                {
                    val transferType =
                        if (it.transferType == TransferType.E_WALLET && it.bankCode == PAYMAYA) TransferType.PAYMAYA else it.transferType
                    val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(it,
                            transferType,
                            null)
                    mNavController.navigate(action)
                },
                {
                    val transferType =
                        if (it.transferType == TransferType.E_WALLET && it.bankCode == PAYMAYA) TransferType.PAYMAYA else it.transferType
                    val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(it,
                            transferType,
                            null)
                    mNavController.navigate(action)
                }, {

                    val phoneNo = UserInfoRepository.userInfo?.value?.phone
                    if (phoneNo.isEmptyStr()) {
                        ToastUtil.showToast(context, R.string.set_phone_no)
                        return@BankCardListAdapter
                    }

                    DeleteBankCardDialog(phoneNo!!) { pwd, code ->
                        loading()
                        viewModel.deleteBankCard(it.id.toString(), pwd, code)
                    }.show(childFragmentManager, null)
                }

            )

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bank_list, container, false).apply {
            setupRecyclerView(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        setupObserve()
        setupTitle()
    }

    private fun initData() {
        viewModel.getBankCardList()
    }

    private fun setupTitle() {
        (activity as BankActivity).changeTitle(getString(R.string.withdraw_setting))
    }

    @SuppressLint("StringFormatInvalid")
    private fun setupObserve() {
        viewModel.loading.observe(this.viewLifecycleOwner, Observer {
            if (it)
//                loading()
            else
                hideLoading()
        })

        viewModel.rechargeConfigs.observe(this.viewLifecycleOwner, Observer {
            mBankListAdapter.uwTypes = it.uwTypes
        })
        viewModel.addMoneyCardSwitch.observe(this.viewLifecycleOwner, Observer {
         //   mBankListAdapter.transferAddSwitch = it
           val stringList = arrayListOf<String>()
           if ((!it.bankTransfer) && (!it.cryptoTransfer) && (!it.walletTransfer) && (!it.paymataTransfer)) {
               return@Observer
           }
           if (it.bankTransfer) stringList.add(getString(R.string.bank_list_bank))
           if (it.cryptoTransfer) stringList.add(getString(R.string.bank_list_crypto))
           if (it.walletTransfer) stringList.add(getString(R.string.bank_list_e_wallet))
           if (it.paymataTransfer) stringList.add(getString(R.string.online_maya))
           cardTypeTitle = stringList.joinToString("/")
           tv_unbind_bank_card.text = getString(R.string.bank_list_not_bink, cardTypeTitle)
           tv_add_money_card_type.text =
               getString(R.string.add_credit_or_virtual, cardTypeTitle)
            updateCardNumbers("${viewModel.numberOfBankCard.value}")

        })
        viewModel.bankCardList.observe(this.viewLifecycleOwner, Observer { bankCardList ->
            mBankListAdapter.setNewInstance(bankCardList?.toMutableList())

            viewModel.checkBankCardCount()
            if (bankCardList.isNullOrEmpty()){
                tv_unbind_bank_card.visibility = View.VISIBLE
            }else{
                tv_unbind_bank_card.visibility = View.GONE
            }
        })
        //银行卡数量
        viewModel.numberOfBankCard.observe(this.viewLifecycleOwner) {
            updateCardNumbers(it)
        }
        cv_add_bank.setOnClickListener{
            val addSwitch=viewModel.addMoneyCardSwitch.value
            var transferType =when{
                addSwitch?.bankTransfer==true-> TransferType.BANK
                addSwitch?.cryptoTransfer==true-> TransferType.CRYPTO
                addSwitch?.walletTransfer==true-> TransferType.E_WALLET
                addSwitch?.paymataTransfer == true -> TransferType.PAYMAYA
                else-> TransferType.BANK
            }
            val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(
                    null,
                    transferType)
            mNavController.navigate(action)
        }

        viewModel.bankDeleteResult.observe(this.viewLifecycleOwner) {
            val result = it?.second ?: return@observe
            if (result.success) {
                val bankCardList = mBankListAdapter.removeCard(it.first)
                updateCardNumbers(mBankListAdapter.dataCount().toString())
                showPromptDialog(message = getDeleteInfo(bankCardList), buttonText = null, isShowDivider = false,) { }
            } else {
                showErrorPromptDialog(title = getString(R.string.prompt), message = result.msg, hasCancel = false) { }
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        view.rv_bank_list.setLinearLayoutManager()
        view.rv_bank_list.adapter = mBankListAdapter
    }

    private fun updateCardNumbers(number: String) {
        tv_money_card_type.text = getString(R.string.my_bank_card, cardTypeTitle, "($number)")
    }

    private fun getDeleteInfo(bankCardList: BankCardList?): String {
        return when (bankCardList?.transferType?.type) {
            TransferType.CRYPTO.type -> getString(R.string.text_crypto_delete_success)
            TransferType.E_WALLET.type -> getString(R.string.text_e_wallet_delete_success)
            TransferType.PAYMAYA.type -> getString(R.string.text_pay_maya_delete_success)
            else -> getString(R.string.text_bank_card_delete_success)
        }
    }
}