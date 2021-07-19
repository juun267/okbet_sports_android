package org.cxct.sportlottery.ui.main.accountHistory.next

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_account_history_next.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.main.accountHistory.AccountHistoryViewModel

class AccountHistoryNextFragment : BaseFragment<AccountHistoryViewModel>(AccountHistoryViewModel::class) {

    //TODO 等新api, 傳遞總金額
    private val args: AccountHistoryNextFragmentArgs by navArgs()

    private val rvAdapter = AccountHistoryNextAdapter(ItemClickListener {
    }, BackClickListener {
        findNavController().navigateUp()
    }, SportSelectListener { sport, date ->
        viewModel.setSelectedSport(sport)
        viewModel.setSelectedDate(date)
//        viewModel.searchDetail(gameType = sport, date = date)
    }, DateSelectListener { sport, date ->
        viewModel.setSelectedSport(sport)
        viewModel.setSelectedDate(date)
//        viewModel.searchDetail(gameType = sport, date = date)
    })

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            recyclerView.layoutManager?.let {
                val visibleItemCount: Int = it.childCount
                val totalItemCount: Int = it.itemCount
                val firstVisibleItemPosition: Int = (it as LinearLayoutManager).findFirstVisibleItemPosition()
                viewModel.getDetailNextPage(visibleItemCount, firstVisibleItemPosition, totalItemCount)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_history_next, container, false).apply {
            viewModel.setSelectedDate(args.row.statDate)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRv()
        initOnclick()
        initObserver()
    }

    private fun initOnclick() {

        btn_back_to_top.setOnClickListener {
            rv_account_history.smoothScrollToPosition(0)
        }

    }

    private fun initObserver() {

        viewModel.loading.observe(viewLifecycleOwner, {
            if (it) loading() else hideLoading()
        })

        viewModel.betDetailResult.observe(viewLifecycleOwner, {
            if (it.success) {
                rvAdapter.addFooterAndSubmitList(it.other, viewModel.detailDataList, viewModel.isDetailLastPage)
                rv_account_history.scrollToPosition(0)
            } else {
                Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.oddsType.observe(viewLifecycleOwner, {
            rvAdapter.oddsType = it
        })

        viewModel.selectSport.observe(viewLifecycleOwner, {
            viewModel.searchDetail()
        })

        viewModel.selectDate.observe(viewLifecycleOwner, {
            viewModel.searchDetail()
        })

    }

    private fun initRv() {
        rv_account_history.apply {
            adapter = rvAdapter
            addOnScrollListener(recyclerViewOnScrollListener)
        }

    }

}

