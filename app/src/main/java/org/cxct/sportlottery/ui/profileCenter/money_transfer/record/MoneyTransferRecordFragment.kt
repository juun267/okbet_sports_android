package org.cxct.sportlottery.ui.profileCenter.money_transfer.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_money_transfer_record.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.query_transfers.Row
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel


class MoneyTransferRecordFragment : BaseSocketFragment<MoneyTransferViewModel>(MoneyTransferViewModel::class) {

    private val recordDataList = mutableListOf<Row>()

    private val detailDialog by lazy { MoneyRecordDetailDialog() }

    private val rvAdapter by lazy {
        MoneyTransferRecordAdapter(ItemClickListener {
            detailDialog.data = it
            detailDialog.show(parentFragmentManager, MoneyTransferRecordFragment::class.simpleName)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewModel.queryTransfers(viewModel.nowPage)

        return inflater.inflate(R.layout.fragment_money_transfer_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
    }


    private fun initView() {
        rv_record.adapter = rvAdapter
        rv_record.addOnScrollListener(recyclerViewOnScrollListener)
    }

    private fun initObserver() {
        viewModel.queryTransfersResult.observe(viewLifecycleOwner) {
            recordDataList.addAll(it.rows as List<Row>)
            viewModel.isLastPage = (rvAdapter.itemCount >= (it.total ?:0))
            rvAdapter.addFooterAndSubmitList(recordDataList)
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
