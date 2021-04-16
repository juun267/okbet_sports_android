package org.cxct.sportlottery.ui.finance

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_recharge_log.*
import kotlinx.android.synthetic.main.activity_recharge_log.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.DividerItemDecorator

class RechargeLogFragment : BaseFragment<FinanceViewModel>(FinanceViewModel::class) {

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
                val firstVisibleItemPosition: Int = (it as LinearLayoutManager).findFirstVisibleItemPosition()
                viewModel.getUserRechargeList(false)
                scrollToTopControl(firstVisibleItemPosition)
            }
        }
    }

    private val logDetailDialog by lazy {
        RechargeLogDetailDialog()
    }

    private val rechargeLogAdapter by lazy {
        RechargeLogAdapter().apply {
            rechargeLogListener = RechargeLogListener {
                viewModel.setLogDetail(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_recharge_log, container, false).apply {
            this.selector_method_status.dataList = viewModel.rechargeChannelList
            this.selector_order_status.dataList = viewModel.rechargeStateList

            this.iv_scroll_to_top.setOnClickListener {
                rvlist.smoothScrollToPosition(0)
            }

            setupListColumn(this)
            setupRechargeLogList(this)
            setupSearch(this)
        }
    }

    private fun setupListColumn(view: View) {
        view.rech_log_recharge_amount.text = getString(R.string.recharge_log_recharge_amount)
    }

    private fun setupRechargeLogList(view: View) {
        view.rvlist.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            addOnScrollListener(recyclerViewOnScrollListener)
            this.adapter = rechargeLogAdapter
            addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.divider_gray)))
        }
    }

    private fun setupSearch(view: View) {
        view.date_range_selector.setOnClickSearchListener {
            viewModel.getUserRechargeList(true, date_range_selector.startTime.toString(), date_range_selector.endTime.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isLoading.observe(this.viewLifecycleOwner,  {
            if (it) {
                loading()
            } else {
                hideLoading()
            }
        })

        viewModel.userRechargeListResult.observe(this.viewLifecycleOwner, Observer {
            if (it?.success == true) {
                val list = it.rows ?: listOf()

                rechargeLogAdapter.data = list
                setupNoRecordView(list.isEmpty())
            }
        })

        viewModel.rechargeLogDetail.observe(this.viewLifecycleOwner, Observer {
            if (logDetailDialog.dialog?.isShowing != true) {

                logDetailDialog.show(
                    parentFragmentManager,
                    RechargeLogFragment::class.java.simpleName
                )
            }
        })

        viewModel.isFinalPage.observe(this.viewLifecycleOwner, Observer {
            rechargeLogAdapter.isFinalPage = it
        })

        viewModel.getUserRechargeList(true)
    }

    private fun setupNoRecordView(visible: Boolean) {
        if (visible) {
            view_no_record.visibility = View.VISIBLE
        } else {
            view_no_record.visibility = View.GONE
        }
    }
}

