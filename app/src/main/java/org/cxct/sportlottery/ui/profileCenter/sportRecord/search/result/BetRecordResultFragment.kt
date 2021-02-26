package org.cxct.sportlottery.ui.profileCenter.sportRecord.search.result

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_bet_record_result.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetRecordResultBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.profileCenter.sportRecord.BetRecordViewModel


class BetRecordResultFragment : BaseFragment<BetRecordViewModel>(BetRecordViewModel::class) {

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        private fun scrollToTopControl(firstVisibleItemPosition: Int) {
            iv_scroll_to_top.apply {
                if (firstVisibleItemPosition > 0) {
                    if (alpha == 0f) {
                        alpha = 0f
                        visibility = View.VISIBLE
                        animate()
                            .alpha(1f)
                            .setDuration(300)
                            .setListener(null)
                    }
                } else {
                    if (alpha == 1f) {
                        alpha = 1f
                        animate()
                            .alpha(0f)
                            .setDuration(300)
                            .setListener(object : AnimatorListenerAdapter() {
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
        initTopButton()
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
        rv_bet_record.apply {
            adapter = rvAdapter
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
            addOnScrollListener(recyclerViewOnScrollListener)
        }
        viewModel.betRecordResult.observe(viewLifecycleOwner, Observer {
            it.let {
                viewModel.isLastPage = (rvAdapter.itemCount >= (it.peekContent().total ?: 0))
                rvAdapter.addFooterAndSubmitList(viewModel.recordDataList, viewModel.isLastPage)
            }
        })

    }

    private fun initTopButton() {
        iv_scroll_to_top.setOnClickListener {
            rv_bet_record.smoothScrollToPosition(0)
        }
    }
}