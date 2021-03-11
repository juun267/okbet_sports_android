package org.cxct.sportlottery.ui.profileCenter.money_transfer.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_money_transfer_record.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel


class MoneyTransferRecordFragment : BaseSocketFragment<MoneyTransferViewModel>(MoneyTransferViewModel::class) {


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
            viewModel.queryTransfers(
                startTime = date_search_bar.startTime.toString(),
                endTime = date_search_bar.endTime.toString(),
                firmTypeIn = selector_in_plat.selectedTag,
                firmTypeOut = selector_out_plat.selectedTag,
                status = selector_transfer_status.selectedTag
            )
        }
    }



    private fun initObserver() {
        viewModel.queryTransfersResult.observe(viewLifecycleOwner) {
            rvAdapter.addFooterAndSubmitList(viewModel.recordDataList, viewModel.isLastPage)
        }

        viewModel.allBalanceResultList.observe(viewLifecycleOwner) {
            selector_out_plat.dataList = viewModel.getRecordPlatNameList(MoneyTransferViewModel.PLAT.OUT_PLAT, it)
            selector_in_plat.dataList = viewModel.getRecordPlatNameList(MoneyTransferViewModel.PLAT.IN_PLAT, it)
        }
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

}
