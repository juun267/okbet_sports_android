package org.cxct.sportlottery.ui.transactionStatus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_transaction_status.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment

class TransactionStatusFragment : BaseFragment<TransactionStatusViewModel>(TransactionStatusViewModel::class) {
    private val recordDiffAdapter by lazy { TransactionRecordDiffAdapter() }
    private val recyclerViewScrollListener: RecyclerView.OnScrollListener by lazy{ object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            /*recyclerView.layoutManager?.let {
                val visibleViewCount = it.childCount
                val totalItem = it.itemCount
                val firstVisibleItemPosition = (it as LinearLayoutManager).findFirstVisibleItemPosition()
                if(firstVisibleItemPosition + visibleViewCount == totalItem)
                    viewModel.getBetList()
            }*/
            if(!recyclerView.canScrollVertically(1)){
                viewModel.getBetList()
            }
        }
    }}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initObserve()
        getBetListData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transaction_status, container, false)
    }

    private fun initRecyclerView() {
        rv_record.apply {
            adapter = recordDiffAdapter
            addOnScrollListener(recyclerViewScrollListener)
        }
    }

    private fun initObserve() {
        //TODO observe data
        viewModel.betListData.observe(this, {
            recordDiffAdapter.setupBetList(it)
        })
    }

    private fun getBetListData() {
        //TODO 設置投注列表資料
        viewModel.getBetList(true)
    }
}