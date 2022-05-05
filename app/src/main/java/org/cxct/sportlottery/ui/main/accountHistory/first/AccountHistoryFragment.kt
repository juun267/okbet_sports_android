package org.cxct.sportlottery.ui.main.accountHistory.first

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_account_history.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.main.accountHistory.AccountHistoryViewModel

class AccountHistoryFragment : BaseFragment<AccountHistoryViewModel>(AccountHistoryViewModel::class) {

    private var isFirstTime = true //避免重複call兩次api

    private val rvAdapter by lazy {
        AccountHistoryAdapter(ItemClickListener {
            it.let { data ->
                viewModel.setSelectedDate(data.statDate)
                val action = AccountHistoryFragmentDirections.actionAccountHistoryFragmentToAccountHistoryNextFragment(data.statDate)
                findNavController().navigate(action)
                isFirstTime = true
            }
        }, BackClickListener {
            activity?.finish()
        }, SportSelectListener {
            if (isFirstTime) isFirstTime = false
            else viewModel.setSelectedSport(it)
        })
    }

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            recyclerView.layoutManager?.let {
                val visibleItemCount: Int = it.childCount
                val totalItemCount: Int = it.itemCount
                val firstVisibleItemPosition: Int = (it as LinearLayoutManager).findFirstVisibleItemPosition()
                viewModel.getNextPage(visibleItemCount, firstVisibleItemPosition, totalItemCount)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_history, container, false).apply {
            viewModel.searchBetRecord()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRv()
        initObserver()
    }

    private fun initObserver() {

        viewModel.loading.observe(viewLifecycleOwner) {
            if (it) loading() else hideLoading()
        }

        viewModel.betRecordResult.observe(viewLifecycleOwner) {
            if (it.success) {
                rvAdapter.addFooterAndSubmitList(viewModel.recordDataList, viewModel.isLastPage)
                rv_account_history.scrollToPosition(0)
            } else {
                Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.selectedSport.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.apply {
                viewModel.searchBetRecord(this)
            }
        }

    }

    private fun initRv() {
        rv_account_history.apply {
            adapter = rvAdapter
            addOnScrollListener(recyclerViewOnScrollListener)
        }
    }

}

