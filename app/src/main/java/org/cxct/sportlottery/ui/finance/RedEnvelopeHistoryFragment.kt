package org.cxct.sportlottery.ui.finance

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_recharge_log.*
import kotlinx.android.synthetic.main.activity_recharge_log.view.*
import kotlinx.android.synthetic.main.component_date_range_selector.view.*
import kotlinx.android.synthetic.main.view_no_record.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.DividerItemDecorator

class RedEnvelopeHistoryFragment : BaseFragment<FinanceViewModel>(FinanceViewModel::class) {

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {

            private fun scrollToTopControl(firstVisibleItemPosition: Int) {
                iv_scroll_to_top.apply {
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
                        date_range_selector.startTime.toString(),
                        date_range_selector.endTime.toString(),
                    )
                    scrollToTopControl(firstVisibleItemPosition)
                }
            }
        }

    private val redEnvelopeLogAdapter by lazy { RedEnvelopeLogAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_redenvelope_log, container, false).apply {
            setupLogList(this)
            setupSearch(this)
            initOnclick(this)
            initNoRecordView(this)
        }
    }

    private fun initOnclick(view: View) {

        view.iv_scroll_to_top.setOnClickListener {
            view.rvlist.smoothScrollToPosition(0)
        }

        view.date_range_selector.setOnClickSearchListener {
            viewModel.getRedEnvelopeHistoryList(
                true, date_range_selector.startTime.toString(),
                date_range_selector.endTime.toString(),
            )
        }
    }

    private fun initNoRecordView(view: View) {
        view.view_no_record.list_no_record_img?.apply {
            viewTreeObserver.addOnGlobalLayoutListener {
                val lp = layoutParams as LinearLayout.LayoutParams
                lp.topMargin = 20.dp
                layoutParams = lp
            }
        }
    }

    private fun setupLogList(view: View) {
        view.rvlist.apply {
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

    private fun setupSearch(view: View) {
        view.date_range_selector.btn_search.setOnClickListener {
            viewModel.getRedEnvelopeHistoryList(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.redEnvelopeListResult.observe(this.viewLifecycleOwner) {
            it?.let {
                redEnvelopeLogAdapter.data = it
                setupNoRecordView(it.isNullOrEmpty())
            }
        }

        viewModel.isFinalPage.observe(this.viewLifecycleOwner) {
            redEnvelopeLogAdapter.isFinalPage = it
        }

        viewModel.getRedEnvelopeHistoryList(true)
    }

    private fun setupNoRecordView(visible: Boolean) {
        if (visible) {
            view_no_record.visibility = View.VISIBLE
        } else {
            view_no_record.visibility = View.GONE
        }
    }

}