package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityAccountHistoryLogBinding
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.finance.df.AccountHistory
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.DividerItemDecorator

/**
 * @app_destination 历史记录
 */
class AccountHistoryLogFragment : BindingFragment<FinanceViewModel,ActivityAccountHistoryLogBinding>() {

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (!recyclerView.canScrollVertically(1)) {
                viewModel.getUserAccountHistory(
                    false,
                    binding.dateRangeSelector.startTime.toString(),
                    binding.dateRangeSelector.endTime.toString(),
                    binding.selectorOrderStatus.selectedTag,
                )
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

        }
    }

    private val accountHistoryAdapter by lazy {
        AccountHistoryAdapter()
    }

    override fun onInitView(view: View) {
        setupRechargeLogList()
        setupSearch()
        initNoRecordView()
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

    private fun setupRechargeLogList() {
        binding.rvlist.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            addOnScrollListener(recyclerViewOnScrollListener)
            adapter = accountHistoryAdapter
            addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.divider_gray)))
        }
    }

    private fun setupSearch() {
        binding.dateRangeSelector.setOnClickSearchListener {
            viewModel.getUserAccountHistory(
                true,
                binding.dateRangeSelector.startTime.toString(),
                binding.dateRangeSelector.endTime.toString(),
                binding.selectorOrderStatus.selectedTag
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectorOrderStatus.setItemData(accountHistoryStateList as MutableList<StatusSheetData>)


        viewModel.isFinalPage.observe(this.viewLifecycleOwner) {
            accountHistoryAdapter.isFinalPage = it
        }

        viewModel.userSportBillListResult.observe(this.viewLifecycleOwner) {
            it?.apply {
//                tv_total_number.text = it.total.toString()
            }
        }

        viewModel.accountHistoryList.observe(this.viewLifecycleOwner) {
            accountHistoryAdapter.data = it
            if (!binding.rvlist.canScrollVertically(1) && !it.isNullOrEmpty()) {
                binding.tvNoDataHistory.visibility = View.VISIBLE
            } else {
                binding.tvNoDataHistory.visibility = View.GONE
            }
            setupNoRecordView(accountHistoryAdapter.data.isEmpty())
        }

        viewModel.getUserAccountHistory(isFirstFetch = true)
    }

    private fun setupNoRecordView(visible: Boolean) {
        if (visible) {
            binding.viewNoRecord.root.visibility = View.VISIBLE
        } else {
            binding.viewNoRecord.root.visibility = View.GONE
        }
    }

    private val accountHistoryStateList by lazy {
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
                getString(R.string.third_party) -> {
                    StatusSheetData(AccountHistory.THIRD.tranTypeGroup, it)
                }
                else -> {
                    StatusSheetData(AccountHistory.BET.tranTypeGroup, it)
                }
            }
        }
    }

}

