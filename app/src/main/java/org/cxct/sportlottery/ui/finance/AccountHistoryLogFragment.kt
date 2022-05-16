package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_account_history_log.*
import kotlinx.android.synthetic.main.activity_account_history_log.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.finance.df.AccountHistory

/**
 * @app_destination 歷史紀錄
 */
class AccountHistoryLogFragment : BaseFragment<FinanceViewModel>(FinanceViewModel::class) {

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (!recyclerView.canScrollVertically(1)) {
                viewModel.getUserAccountHistory(
                    false,
                    date_range_selector.startTime.toString(),
                    date_range_selector.endTime.toString(),
                    selector_order_status.selectedTag,
                )
            }
        }
    }

    private val logDetailDialog by lazy {
        RechargeLogDetailDialog()
    }

    private val accountHistoryAdapter by lazy {
        AccountHistoryAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.activity_account_history_log, container, false).apply {

            //setupListColumn(this)
            setupRechargeLogList(this)
            setupSearch(this)
        }
    }

//    private fun setupListColumn(view: View) {
//        view.rech_log_recharge_amount.text = getString(R.string.recharge_log_recharge_amount)
//    }

    private fun setupRechargeLogList(view: View) {
        view.rvlist.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            addOnScrollListener(recyclerViewOnScrollListener)
            this.adapter = accountHistoryAdapter
            addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.divider_gray)))
        }
    }

    private fun setupSearch(view: View) {
        view.date_range_selector.setOnClickSearchListener {
            viewModel.getUserAccountHistory(
                true,
                date_range_selector.startTime.toString(),
                date_range_selector.endTime.toString(),
                selector_order_status.selectedTag
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selector_order_status.dataList = accountHistoryStateList

        viewModel.isLoading.observe(this.viewLifecycleOwner, {
            if (it) {
                loading()
            } else {
                hideLoading()
            }
        })

        viewModel.isFinalPage.observe(this.viewLifecycleOwner, {
            accountHistoryAdapter.isFinalPage = it
        })

        viewModel.userSportBillListResult.observe(this.viewLifecycleOwner, {
            it?.apply {
                tv_total_number.text = it.total.toString()
                setupNoRecordView(it.rows.isEmpty())
            }
        })

        viewModel.accountHistoryList.observe(this.viewLifecycleOwner, {
            accountHistoryAdapter.data = it
        })

        viewModel.getUserAccountHistory(isFirstFetch = true)
    }

    private fun setupNoRecordView(visible: Boolean) {
        if (visible) {
            view_no_record.visibility = View.VISIBLE
        } else {
            view_no_record.visibility = View.GONE
        }
    }

    private val accountHistoryStateList by lazy {
        this.resources.getStringArray(R.array.account_history_state_array).map {
            when (it) {
                getString(R.string.text_account_history_bet) -> {
                    StatusSheetData(AccountHistory.BET.tranTypeGroup, it)
                }
                getString(R.string.text_account_history_recharge) -> {
                    StatusSheetData(AccountHistory.RECHARGE.tranTypeGroup, it)
                }
                getString(R.string.text_account_history_withdraw) -> {
                    StatusSheetData(AccountHistory.WITHDRAW.tranTypeGroup, it)
                }
                getString(R.string.text_account_history_activity) -> {
                    StatusSheetData(AccountHistory.ACTIVITY.tranTypeGroup, it)
                }
                getString(R.string.text_account_history_credit) -> {
                    StatusSheetData(AccountHistory.CREDIT.tranTypeGroup, it)
                }
                else -> {
                    StatusSheetData(AccountHistory.BET.tranTypeGroup, it)
                }
            }
        }
    }

}

