package org.cxct.sportlottery.ui.finance

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.archit.calendardaterangepicker.customviews.DateSelectedType
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_info_center.*
import kotlinx.android.synthetic.main.activity_recharge_log.*
import kotlinx.android.synthetic.main.activity_recharge_log.iv_scroll_to_top
import kotlinx.android.synthetic.main.activity_recharge_log.view.*
import kotlinx.android.synthetic.main.component_date_range_selector.view.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_calendar.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_rech_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import java.util.*


class WithdrawLogFragment : BaseFragment<FinanceViewModel>(FinanceViewModel::class) {

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        private fun scrollToTopControl(firstVisibleItemPosition: Int) {
            iv_scroll_to_top.apply {
                if (firstVisibleItemPosition > 0) {
                    if (alpha == 0f) {
                        alpha = 0f
                        visibility = View.VISIBLE
                        animate().alpha(1f).setDuration(300).setListener(null)
                    }
                } else {
                    if (alpha == 1f) {
                        alpha = 1f
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
                viewModel.getUserWithdrawList(false)
                scrollToTopControl(firstVisibleItemPosition)
            }
        }
    }

    private val logDetailDialog by lazy {
        WithdrawLogDetailDialog()
    }

    private val withdrawLogAdapter by lazy {
        WithdrawLogAdapter().apply {
            withdrawLogListener = WithdrawLogListener {
                viewModel.setWithdrawLogDetail(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_recharge_log, container, false).apply {
            this.selector_order_status.dataList = viewModel.withdrawStateList
            this.selector_method_status.dataList = viewModel.withdrawTypeList
            setupListColumn(this)
            setupWithdrawLogList(this)
            setupSearch(this)
            initOnclick(this)
        }
    }

    private fun initOnclick(view: View) {

        iv_scroll_to_top.setOnClickListener {
            rv_data.smoothScrollToPosition(0)
        }

        view.date_range_selector.setOnClickSearchListener {
            viewModel.getUserWithdrawList(false, date_range_selector.startTime.toString(), date_range_selector.endTime.toString())
        }
    }


    private fun setupListColumn(view: View) {
        view.rech_log_recharge_amount.text = getString(R.string.withdraw_log_withdraw_amount)
    }

    private fun setupWithdrawLogList(view: View) {
        view.rvlist.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            addOnScrollListener(recyclerViewOnScrollListener)
            this.adapter = withdrawLogAdapter
            addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.divider_gray)))
        }
    }

    private fun setupSearch(view: View) {
        view.date_range_selector.btn_search.setOnClickListener {
            viewModel.getUserWithdrawList(true)
            loading()
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

        viewModel.userWithdrawListResult.observe(this.viewLifecycleOwner, Observer {
            if (it?.success == true) {
                val list = it.rows ?: listOf()

                withdrawLogAdapter.data = list
                setupNoRecordView(list.isEmpty())
            }

            hideLoading()
        })

        viewModel.withdrawLogDetail.observe(this.viewLifecycleOwner, Observer {
            if (logDetailDialog.dialog?.isShowing != true) {

                logDetailDialog.show(
                    parentFragmentManager,
                    WithdrawLogFragment::class.java.simpleName
                )
            }
        })

        viewModel.isFinalPage.observe(this.viewLifecycleOwner, Observer {
            withdrawLogAdapter.isFinalPage = it
        })

        viewModel.getUserWithdrawList(true)
        loading()
    }

    private fun setupNoRecordView(visible: Boolean) {
        if (visible) {
            view_no_record.visibility = View.VISIBLE
        } else {
            view_no_record.visibility = View.GONE
        }
    }
}