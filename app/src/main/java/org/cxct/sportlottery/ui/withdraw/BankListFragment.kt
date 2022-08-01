package org.cxct.sportlottery.ui.withdraw

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_bank_list.*
import kotlinx.android.synthetic.main.fragment_bank_list.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.ui.base.BaseFragment

class BankListFragment : BaseFragment<WithdrawViewModel>(WithdrawViewModel::class) {

    private val mNavController by lazy {
        findNavController()
    }


    private val mBankListAdapter by lazy {
        BankListAdapter(
            BankListClickListener(
                editBankListener = {
                    val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(it, it.transferType, null)
                    mNavController.navigate(action)
                },
                editCryptoListener = {
                    val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(it, it.transferType, null)
                    mNavController.navigate(action)
                },
                addListener = {
                    val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(null, if (it.bankTransfer) TransferType.BANK else if (it.cryptoTransfer) TransferType.CRYPTO else TransferType.E_WALLET, it)
                    mNavController.navigate(action)
                }
            )
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

    private fun setupObserve() {
        viewModel.loading.observe(this.viewLifecycleOwner, Observer {
            if (it)
                loading()
            else
                hideLoading()
        })

        viewModel.rechargeConfigs.observe(this.viewLifecycleOwner, Observer {
            mBankListAdapter.moneyConfig = it
        })

        viewModel.userInfo.observe(this.viewLifecycleOwner, Observer {
            mBankListAdapter.fullName = it?.fullName ?: ""
        })

        viewModel.bankCardList.observe(this.viewLifecycleOwner, Observer { bankCardList ->
            bankCardList.let { data ->
                mBankListAdapter.bankList = data ?: listOf()
                if (!data.isNullOrEmpty()) {
                    tv_no_bank_card.visibility = View.GONE
                }
                viewModel.checkBankCardCount()
            }
        })

        viewModel.addMoneyCardSwitch.observe(this.viewLifecycleOwner, Observer {
            mBankListAdapter.transferAddSwitch = it
            tv_no_bank_card.text = it.run {
                val stringList = arrayListOf<String>()
                if ((!bankTransfer) && (!cryptoTransfer) && (!walletTransfer)) {
                    return@Observer
                }
                if(bankTransfer) stringList.add(getString(R.string.bank_list_bank))
                if(cryptoTransfer) stringList.add(getString(R.string.bank_list_crypto))
                if(walletTransfer) stringList.add(getString(R.string.bank_list_e_wallet))
                getString(R.string.bank_list_not_bink, stringList.joinToString("/"))
            }
        })
    }

    private fun setupRecyclerView(view: View) {
        view.rv_bank_list.apply {
            layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (mBankListAdapter.bankList.size) {
                            0 -> 2
                            else -> 1
                        }
                    }

                }
            }
            adapter = mBankListAdapter
        }
    }
}