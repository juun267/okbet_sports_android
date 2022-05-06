package org.cxct.sportlottery.ui.main.accountHistory.next

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_account_history_next.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.main.accountHistory.AccountHistoryViewModel

class AccountHistoryNextFragment : BaseFragment<AccountHistoryViewModel>(AccountHistoryViewModel::class) {

    private var needScrollToTop = true //用來記錄是否需要滾動至最上方

    private val rvAdapter = AccountHistoryNextAdapter(ItemClickListener {
    }, BackClickListener {
        findNavController().navigateUp()
    }, SportSelectListener {
        viewModel.setSelectedSport(it)
    }, DateSelectListener {
        viewModel.setSelectedDate(it)
    }, ScrollToTopListener {
        rv_account_history.smoothScrollToPosition(0)
    })

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            recyclerView.layoutManager?.let {
                val visibleItemCount: Int = it.childCount
                val totalItemCount: Int = it.itemCount
                val firstVisibleItemPosition: Int = (it as LinearLayoutManager).findFirstVisibleItemPosition()
                needScrollToTop = false
                viewModel.getDetailNextPage(visibleItemCount, firstVisibleItemPosition, totalItemCount)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_history_next, container, false).apply {
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

        viewModel.betDetailResult.observe(viewLifecycleOwner) {
            if (it.success) {
                rvAdapter.addFooterAndSubmitList(
                    it.other,
                    viewModel.detailDataList,
                    viewModel.isDetailLastPage
                )
            } else {
                Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.oddsType.observe(viewLifecycleOwner) {
            rvAdapter.oddsType = it
        }

        viewModel.selectedDate.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.apply {
                needScrollToTop = true
                rvAdapter.nowSelectedDate = this
                viewModel.searchDetail(date = this)
            }
        }

        viewModel.selectedSport.observe(viewLifecycleOwner) {
            rvAdapter.nowSelectedSport = it.peekContent()
            it.getContentIfNotHandled()?.apply {
                needScrollToTop = true
                viewModel.searchDetail(gameType = this)
            }
        }

    }

    private fun initRv() {
        rv_account_history.apply {
            adapter = rvAdapter
            addOnScrollListener(recyclerViewOnScrollListener)
        }

        rvAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() { //submitList 資料更改後，滑至頂
            override fun onChanged() {
                if (needScrollToTop)
                    rv_account_history.scrollToPosition(0)
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                if (needScrollToTop)
                    rv_account_history.scrollToPosition(0)
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                if (needScrollToTop)
                    rv_account_history.scrollToPosition(0)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (needScrollToTop)
                    rv_account_history.scrollToPosition(0)
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                if (needScrollToTop)
                    rv_account_history.scrollToPosition(0)
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                if (needScrollToTop)
                    rv_account_history.scrollToPosition(0)
            }
        })
    }

}

