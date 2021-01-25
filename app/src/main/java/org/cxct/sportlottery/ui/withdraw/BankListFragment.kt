package org.cxct.sportlottery.ui.withdraw

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
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.withdraw.WithdrawActivity.Companion.navigateKey

class BankListFragment : BaseFragment<WithdrawViewModel>(WithdrawViewModel::class) {

    private val mNavController by lazy {
        findNavController()
    }


    private val mBankListAdapter by lazy {
        val navigateFrom = (arguments?.getSerializable(navigateKey) ?: PageFrom.WITHDRAW_SETTING) as PageFrom
        BankListAdapter(BankListClickListener({
            val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(it, navigateFrom)
            mNavController.navigate(action)
        }, {
            val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(null, navigateFrom)
            mNavController.navigate(action)
        }))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bank_list, container, false).apply {
            setupTitle()

            setupRecyclerView(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getUserInfoData()
        setupObserve()
    }

    private fun setupTitle() {
        when (val currentActivity = this.activity) {
            is WithdrawActivity -> {
                currentActivity.setToolBarName(getString(R.string.withdraw_setting))
            }
            is BankActivity -> {
                currentActivity.setToolBarName(getString(R.string.withdraw_setting))
            }
        }
    }

    private fun setupObserve() {
        viewModel.bankCardList.observe(this.viewLifecycleOwner, Observer {
            it.bankCardList.let { data ->
                mBankListAdapter.bankList = data ?: listOf()
                if (!data.isNullOrEmpty()) {
                    tv_no_bank_card.visibility = View.GONE
                }
            }
        })
    }

    private fun setupRecyclerView(view: View) {
        view.rv_bank_list.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = mBankListAdapter
        }
    }
}