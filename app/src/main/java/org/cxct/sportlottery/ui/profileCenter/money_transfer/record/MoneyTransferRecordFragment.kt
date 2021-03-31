package org.cxct.sportlottery.ui.profileCenter.money_transfer.record

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_money_transfer_record.*
import kotlinx.android.synthetic.main.fragment_money_transfer_record.date_search_bar
import kotlinx.android.synthetic.main.fragment_money_transfer_record.iv_scroll_to_top
import kotlinx.android.synthetic.main.fragment_money_transfer_record.rv_record
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel


class MoneyTransferRecordFragment : BaseSocketFragment<MoneyTransferViewModel>(MoneyTransferViewModel::class) {

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

    private val detailDialog by lazy { MoneyRecordDetailDialog() }

    private val rvAdapter by lazy {
        MoneyTransferRecordAdapter(ItemClickListener {
            detailDialog.data = it
            detailDialog.show(parentFragmentManager, MoneyTransferRecordFragment::class.simpleName)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.getAllBalance()
        viewModel.queryTransfers()
        viewModel.setToolbarName(getString(R.string.record_conversion))

        viewModel.setInSheetDataList()
        viewModel.setOutSheetDataList()

        return inflater.inflate(R.layout.fragment_money_transfer_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initOnclick()
        initObserver()
    }

    private fun initView() {
        rv_record.adapter = rvAdapter
        rv_record.addOnScrollListener(recyclerViewOnScrollListener)
        selector_transfer_status.dataList = viewModel.statusList
    }

    private fun initOnclick() {
        date_search_bar.setOnClickSearchListener {
            viewModel.queryTransfers(startTime = date_search_bar.startTime.toString(), endTime = date_search_bar.endTime.toString(), firmTypeIn = selector_in_plat.selectedTag, firmTypeOut = selector_out_plat.selectedTag, status = selector_transfer_status.selectedTag)
        }

        selector_out_plat.setOnItemSelectedListener {
            selector_in_plat.dataList = viewModel.getPlatRecordList(MoneyTransferViewModel.PLAT.IN_PLAT, it.showName)

        }

        selector_in_plat.setOnItemSelectedListener {
            selector_out_plat.dataList = viewModel.getPlatRecordList(MoneyTransferViewModel.PLAT.OUT_PLAT, it.showName)
        }

    }

    private fun initObserver() {
        viewModel.queryTransfersResult.observe(viewLifecycleOwner) {
            rvAdapter.addFooterAndSubmitList(viewModel.recordDataList, viewModel.isLastPage)
        }

        viewModel.allBalanceResultList.observe(viewLifecycleOwner) {
            selector_out_plat.dataList = viewModel.getPlatRecordList(MoneyTransferViewModel.PLAT.OUT_PLAT, selector_in_plat.selectedText)
            selector_in_plat.dataList = viewModel.getPlatRecordList(MoneyTransferViewModel.PLAT.IN_PLAT, selector_out_plat.selectedText)
        }
    }

}
