package org.cxct.sportlottery.ui.finance

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityRedenvelopeLogBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.DividerItemDecorator

class RedEnvelopeHistoryFragment : BaseFragment<FinanceViewModel, ActivityRedenvelopeLogBinding>() {

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {

            private fun scrollToTopControl(firstVisibleItemPosition: Int) {
                binding.ivScrollToTop.apply {
                    when {
                        firstVisibleItemPosition > 0 && alpha == 0f -> {
                            visibility = View.VISIBLE
                            animate().alpha(1f).setDuration(300).setListener(null)
                        }
                        firstVisibleItemPosition <= 0 && alpha == 1f -> {
                            animate().alpha(0f).setDuration(300)
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
                    val firstVisibleItemPosition: Int =
                        (it as LinearLayoutManager).findFirstVisibleItemPosition()
                    viewModel.getRedEnvelopeHistoryList(
                        false,
                        binding.dateRangeSelector.startTime.toString(),
                        binding.dateRangeSelector.endTime.toString(),
                    )
                    scrollToTopControl(firstVisibleItemPosition)
                }
            }
        }

    private val redEnvelopeLogAdapter by lazy { RedEnvelopeLogAdapter() }

    override fun onInitView(view: View) {
        setupLogList()
        setupSearch()
        initOnclick()
        initNoRecordView()
    }


    private fun initOnclick() {

        binding.ivScrollToTop.setOnClickListener {
            binding.rvlist.smoothScrollToPosition(0)
        }

        binding.dateRangeSelector.setOnClickSearchListener {
            viewModel.getRedEnvelopeHistoryList(
                true, binding.dateRangeSelector.startTime.toString(),
                binding.dateRangeSelector.endTime.toString(),
            )
        }
    }

    private fun initNoRecordView() {
        binding.viewNoRecord.listNoRecordImg?.apply {
            viewTreeObserver.addOnGlobalLayoutListener {
                val lp = layoutParams as LinearLayout.LayoutParams
                lp.topMargin = 20.dp
                layoutParams = lp
            }
        }
    }

    private fun setupLogList() {
        binding.rvlist.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            addOnScrollListener(recyclerViewOnScrollListener)
            this.adapter = redEnvelopeLogAdapter
            addItemDecoration(
                DividerItemDecorator(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.divider_gray
                    )
                )
            )
        }
    }

    private fun setupSearch() {
        binding.dateRangeSelector.setOnClickSearchListener {
            viewModel.getRedEnvelopeHistoryList(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.redEnvelopeListResult.observe(this.viewLifecycleOwner) {
            it?.let {
                redEnvelopeLogAdapter.setList(it)
                setupNoRecordView(it.isNullOrEmpty())
            }
        }
        viewModel.getRedEnvelopeHistoryList(true)
    }

    private fun setupNoRecordView(visible: Boolean) {
        if (visible) {
            binding.viewNoRecord.root.visibility = View.VISIBLE
        } else {
            binding.viewNoRecord.root.visibility = View.GONE
        }
    }



}