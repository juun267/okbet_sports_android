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
import kotlinx.android.synthetic.main.activity_recharge_log.*
import kotlinx.android.synthetic.main.activity_recharge_log.view.*
import kotlinx.android.synthetic.main.component_date_range_new_selector.*
import kotlinx.android.synthetic.main.component_date_range_selector.view.*
import kotlinx.android.synthetic.main.fragment_money_transfer_record.*
import kotlinx.android.synthetic.main.fragment_money_transfer_record.date_range_selector

import kotlinx.android.synthetic.main.fragment_money_transfer_record.iv_scroll_to_top
import kotlinx.android.synthetic.main.fragment_money_transfer_record.rv_record
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel

/**
 * @app_destination 转换记录
 */
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
        viewModel.setToolbarName(getString(R.string.account_transfer))

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
        selector_transfer_status.setItemData(viewModel.statusList as MutableList<StatusSheetData>)
        selector_transfer_status.setSelectCode(viewModel.statusList.first().code)
    }

    private fun initOnclick() {
        iv_scroll_to_top.setOnClickListener {
            rv_record.smoothScrollToPosition(0)
        }

        date_range_selector.btn_search.setOnClickListener  {
            viewModel.queryTransfers(startTime = date_range_selector.startTime.toString(),
                                     endTime = date_range_selector.endTime.toString(),
                                     firmTypeIn = selector_in_plat.selectedTag,
                                     firmTypeOut = selector_out_plat.selectedTag,
                                     status = selector_transfer_status.selectedTag)
        }

        selector_out_plat.setOnItemSelectedListener {
            viewModel.filterRecordList(MoneyTransferViewModel.PLAT.IN_PLAT, it.showName)
        }

        selector_in_plat.setOnItemSelectedListener {
            viewModel.filterRecordList(MoneyTransferViewModel.PLAT.OUT_PLAT, it.showName)
        }

    }

    private fun initObserver() {
        viewModel.queryTransfersResult.observe(viewLifecycleOwner) {
            rvAdapter.addFooterAndSubmitList(viewModel.recordDataList, viewModel.isLastPage)
        }

        viewModel.recordInPlatSheetList.observe(viewLifecycleOwner) {
            selector_in_plat.setItemData(it as MutableList<StatusSheetData>)
            selector_in_plat.setSelectCode(it.first().code)
        }

        viewModel.recordOutPlatSheetList.observe(viewLifecycleOwner) {
            selector_out_plat.setItemData(it as MutableList<StatusSheetData>)
            selector_out_plat.setSelectCode(it.first().code)
        }
    }

}
