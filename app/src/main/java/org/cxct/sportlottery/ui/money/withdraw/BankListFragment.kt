package org.cxct.sportlottery.ui.money.withdraw

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_bank_list.*
import kotlinx.android.synthetic.main.fragment_bank_list.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.ui.base.BaseFragment

/**
 * @app_destination 提款设置
 */
class BankListFragment : BaseFragment<WithdrawViewModel>(WithdrawViewModel::class) {
    private val mNavController by lazy {
        findNavController()
    }


    private val mBankListAdapter by lazy {
        BankCardListAdapter(
            BankCardListClickListener(
                editBankListener = {
                    val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(it, it.transferType, null)
                    mNavController.navigate(action)
                },
                editCryptoListener = {
                    val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(it, it.transferType, null)
                    mNavController.navigate(action)
                }
            ))

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
               // loading()
            else
                hideLoading()
        })

        viewModel.rechargeConfigs.observe(this.viewLifecycleOwner, Observer {
            mBankListAdapter.moneyConfig = it
        })

        viewModel.addMoneyCardSwitch.observe(this.viewLifecycleOwner, Observer {
         //   mBankListAdapter.transferAddSwitch = it
           it.run {
                val stringList = arrayListOf<String>()
                if ((!bankTransfer) && (!cryptoTransfer) && (!walletTransfer) && (!paymataTransfer)) {
                    return@Observer
                }
               if (bankTransfer) stringList.add(getString(R.string.bank_list_bank))
                if(cryptoTransfer) stringList.add(getString(R.string.bank_list_crypto))
               if (walletTransfer) stringList.add(getString(R.string.bank_list_e_wallet))
               if (paymataTransfer) stringList.add(getString(R.string.online_maya))
               tv_unbind_bank_card.text =  getString(R.string.bank_list_not_bink, stringList.joinToString("/"))
               tv_add_money_card_type.text= getString(R.string.add_credit_or_virtual, stringList.joinToString("/"))
               tv_money_card_type.text = getString(R.string.my_bank_card, stringList.joinToString("/"))
            }
        })
        viewModel.bankCardList.observe(this.viewLifecycleOwner, Observer { bankCardList ->
            bankCardList.let { data ->
                mBankListAdapter.bankList = data ?: listOf()
                if (!data.isNullOrEmpty()) {
                    tv_no_bank_card.visibility = View.GONE
                }
                viewModel.checkBankCardCount()
            }
            if (bankCardList.isNullOrEmpty()){
                tv_unbind_bank_card.visibility = View.VISIBLE
            }else{
                tv_unbind_bank_card.visibility = View.GONE
            }
        })
        //银行卡数量
        viewModel.numberOfBankCard.observe(this.viewLifecycleOwner,Observer{
            tv_bank_card_number.text = it
        })
        cv_add_bank.setOnClickListener{
            val action =
                org.cxct.sportlottery.ui.money.withdraw.BankListFragmentDirections.actionBankListFragmentToBankCardFragment(
                    null,
                    TransferType.BANK)
            mNavController.navigate(action)
        }
    }

    private fun setupRecyclerView(view: View) {
        view.rv_bank_list.apply {
            layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false).apply {
            }

            adapter = mBankListAdapter
        }
    }
}