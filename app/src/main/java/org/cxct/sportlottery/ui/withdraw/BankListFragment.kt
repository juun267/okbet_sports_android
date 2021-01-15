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

class BankListFragment : BaseFragment<WithdrawViewModel>(WithdrawViewModel::class) {

    private val mNavController by lazy {
        findNavController()
    }

    private val mBankListAdapter by lazy {
        BankListAdapter(BankListClickListener({
            val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(it)
            mNavController.navigate(action)
        }, {
            val action = BankListFragmentDirections.actionBankListFragmentToBankCardFragment(null)
            mNavController.navigate(action)
        }))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bank_list, container, false).apply {
            setupRecyclerView(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getBankCardList()
        setupObserve()
    }

    private fun setupObserve() {
        viewModel.bankCardList.observe(this.viewLifecycleOwner, Observer {
            it.bankCardList?.let { data ->
                mBankListAdapter.bankList = data
                if (data.isNotEmpty()) {
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