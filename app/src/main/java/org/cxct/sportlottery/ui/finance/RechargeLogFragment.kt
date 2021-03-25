package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_recharge_log.*
import kotlinx.android.synthetic.main.activity_recharge_log.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.DividerItemDecorator

class RechargeLogFragment : BaseFragment<FinanceViewModel>(FinanceViewModel::class) {
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

            this.adapter = rechargeLogAdapter
            addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.divider_gray)))
        }
    }

    private fun setupSearch(view: View) {
        view.date_range_selector.setOnClickSearchListener {
            viewModel.getUserRechargeList(false, date_range_selector.startTime.toString(), date_range_selector.endTime.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userRechargeListResult.observe(this.viewLifecycleOwner, Observer {
            if (it?.success == true) {
                val list = it.rows ?: listOf()

                rechargeLogAdapter.data = list
                setupNoRecordView(list.isEmpty())
            }
            hideLoading()
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
        loading()
    }

    private fun setupNoRecordView(visible: Boolean) {
        if (visible) {
            list_swipe_refresh_layout.visibility = View.GONE
            view_no_record.visibility = View.VISIBLE
        } else {
            list_swipe_refresh_layout.visibility = View.VISIBLE
            view_no_record.visibility = View.GONE
        }
    }
}

