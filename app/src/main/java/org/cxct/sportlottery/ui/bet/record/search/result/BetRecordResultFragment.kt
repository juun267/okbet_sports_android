package org.cxct.sportlottery.ui.bet.record.search.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_bet_record_result.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetRecordResultBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.bet.record.BetRecordViewModel


class BetRecordResultFragment : BaseFragment<BetRecordViewModel>(BetRecordViewModel::class) {

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
        val binding: FragmentBetRecordResultBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bet_record_result, container, false)
        binding.apply {
            betRecordViewModel = this@BetRecordResultFragment.viewModel
            lifecycleOwner = this@BetRecordResultFragment
//            other = this@BetRecordResultFragment.viewModel.betRecordResult.value?.other
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {
        viewModel.loading.observe(viewLifecycleOwner, Observer {
            if (it) loading() else hideLoading()
        })
        initTv()
        initRv()
    }

    private fun initTv() {
        viewModel.selectedBetStatus.observe(viewLifecycleOwner, {
            tv_bet_status.text = it
        })
    }

    private fun initRv() {
        val rvAdapter = BetRecordAdapter(ItemClickListener {
            it.let { data ->
                val detailDialog = BetRecordDetailDialog(data)
                detailDialog.show(parentFragmentManager, "BetRecordDetailDialog")
            }
        })

        rv_bet_record.adapter = rvAdapter
        rv_bet_record.addOnScrollListener(recyclerViewOnScrollListener)
        viewModel.betRecordResult.observe(viewLifecycleOwner, Observer {
            it.let {
                viewModel.isLastPage = (rvAdapter.itemCount >= (it.peekContent().total ?: 0))
                rvAdapter.addFooterAndSubmitList(viewModel.recordDataList, viewModel.isLastPage)
            }
        })

    }

}