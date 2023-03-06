package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_account_history_log.*
import kotlinx.android.synthetic.main.activity_account_history_log.view.*
import kotlinx.android.synthetic.main.view_no_record.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.FLAG_CREDIT_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.finance.df.AccountHistory
import org.cxct.sportlottery.util.DisplayUtil.dp

/**
 * @app_destination 历史记录
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

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
           /* if ( !recyclerView.canScrollVertically(1)){//1表示是否能向上滚动 false表示已经到底部 -1表示是否能向下滚动false表示已经到顶部
                viewModel.accountHistoryList.observe(this@AccountHistoryLogFragment) {
                    if (it.isNullOrEmpty()){
                        tv_no_data_history.visibility = View.GONE
                    }else{
                        tv_no_data_history.visibility = View.VISIBLE
                    }
                }
            }else{
                tv_no_data_history.visibility = View.GONE
            }*/
        }
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
            initNoRecordView(this)
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

        selector_order_status.setItemData(accountHistoryStateList as MutableList<StatusSheetData>)


        viewModel.isFinalPage.observe(this.viewLifecycleOwner, {
            accountHistoryAdapter.isFinalPage = it
        })

        viewModel.userSportBillListResult.observe(this.viewLifecycleOwner, {
            it?.apply {
                tv_total_number.text = it.total.toString()
                setupNoRecordView(accountHistoryAdapter.data.isEmpty())
            }
        })

        viewModel.accountHistoryList.observe(this.viewLifecycleOwner, {
            accountHistoryAdapter.data = it
            if (!view.rvlist.canScrollVertically(1)&&!it.isNullOrEmpty()){
                tv_no_data_history.visibility = View.VISIBLE
            }else{
                tv_no_data_history.visibility = View.GONE
            }
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
        if (sConfigData?.creditSystem == FLAG_CREDIT_OPEN) {
            this.resources.getStringArray(R.array.account_history_state_by_credit_array).map {
                when (it) {
                    getString(R.string.text_account_history_bet) -> {
                        StatusSheetData(AccountHistory.BET.tranTypeGroup, it)
                    }
                    getString(R.string.text_account_history_credit) -> {
                        StatusSheetData(AccountHistory.CREDIT.tranTypeGroup, it)
                    }
                    else -> {
                        StatusSheetData(AccountHistory.BET.tranTypeGroup, it)
                    }
                }
            }
        } else {
            this.resources.getStringArray(R.array.account_history_state_array).map {
                when (it) {
                    getString(R.string.label_all) -> {
                        StatusSheetData(AccountHistory.ALL.tranTypeGroup, it)
                    }
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

}

