package org.cxct.sportlottery.ui.main.accountHistory

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_account_history.*
import kotlinx.android.synthetic.main.fragment_account_history.iv_scroll_to_top
import kotlinx.android.synthetic.main.fragment_account_history.status_selector
import kotlinx.android.synthetic.main.fragment_sport_bet_record.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.component.StatusSheetData
import org.cxct.sportlottery.ui.profileCenter.sportRecord.dialog.BetRecordDetailDialog

class AccountHistoryFragment : BaseFragment<AccountHistoryViewModel>(AccountHistoryViewModel::class) {

    private val betStatusList by lazy {
        listOf(StatusSheetData("0", context?.getString(R.string.all_sport)),
               StatusSheetData("1", context?.getString(R.string.all_sport)),
               StatusSheetData("2", context?.getString(R.string.all_sport)))
    }

    private val rvAdapter = AccountHistoryAdapter(ItemClickListener {
        it.let { data ->
        }
    })

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        private fun scrollToTopControl(firstVisibleItemPosition: Int) {
            iv_scroll_to_top.apply {
                when {
                    firstVisibleItemPosition > 0 && alpha == 0f -> {
                        visibility = View.VISIBLE
                        animate().alpha(1f).setDuration(300).setListener(null)
                    }
                    firstVisibleItemPosition <= 0 && alpha == 1f -> {
                        animate().alpha(0f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                visibility = View.GONE
                            }
                        })
                    }
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            recyclerView.layoutManager?.let {
                val visibleItemCount: Int = it.childCount
                val totalItemCount: Int = it.itemCount
                val firstVisibleItemPosition: Int = (it as LinearLayoutManager).findFirstVisibleItemPosition()
                viewModel.getNextPage(visibleItemCount, firstVisibleItemPosition, totalItemCount)
                scrollToTopControl(firstVisibleItemPosition)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRv()
        initOnclick()
        initObserver()
    }

    private fun initView() {
        status_selector.setCloseBtnText(getString(R.string.bottom_sheet_close))
        status_selector.dataList = betStatusList
    }

    private fun initOnclick() {

        iv_scroll_to_top.setOnClickListener {
            rv_account_history.smoothScrollToPosition(0)
        }

    }

    private fun initObserver() {

        viewModel.loading.observe(viewLifecycleOwner, {
            if (it) loading() else hideLoading()
        })

        viewModel.betRecordResult.observe(viewLifecycleOwner, {
            if (it.success) {
                rvAdapter.addFooterAndSubmitList(viewModel.recordDataList, viewModel.isLastPage)
            } else {
                Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.oddsType.observe(viewLifecycleOwner, {
            rvAdapter.oddsType = it
        })

    }

    private fun initRv() {
        rv_account_history.apply {
            adapter = rvAdapter
            addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.divider_gray)))
            addOnScrollListener(recyclerViewOnScrollListener)
        }

    }

}

