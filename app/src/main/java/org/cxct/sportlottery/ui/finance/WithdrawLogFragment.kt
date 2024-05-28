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
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.databinding.ActivityWithdrawLogBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.finance.df.CheckStatus
import org.cxct.sportlottery.ui.finance.df.UWType
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.RefreshHelper
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.view.DividerItemDecorator

/**
 * @app_destination 提款记录
 */
class WithdrawLogFragment : BaseFragment<FinanceViewModel, ActivityWithdrawLogBinding>() {
    private var reserveTime: String = ""
    private lateinit var refreshHelper: RefreshHelper

    private val logDetailDialog by lazy {
        WithdrawLogDetailDialog()
    }

    private val withdrawLogAdapter by lazy {
        WithdrawLogAdapter().apply {
            withdrawLogListener = WithdrawLogListener(
                clickListener = { event, clickMoney ->
                    event.peekContent()?.let {
                        if (it.uwType == UWType.BETTING_STATION.type && clickMoney) {
                            reserveTime = it.withdrawDateAndTime.toString()
                            viewModel.getQueryByBettingStationId(it.channel)
                        } else {
                            viewModel.setWithdrawLogDetail(event)
                        }
                    }
                }
            )
        }
    }

    override fun onInitView(view: View) {
        binding.selectorOrderStatus.setItemData(withdrawStateList as MutableList<StatusSheetData>)
        binding.selectorMethodStatus.setItemData(withdrawTypeList as MutableList<StatusSheetData>)
        setupWithdrawLogList()
        setupSearch()
        initOnclick()
        initNoRecordView()
    }


    private fun initOnclick() =binding.run{
        dateRangeSelector.setOnClickSearchListener {
            viewModel.getUserWithdrawList(
                true, dateRangeSelector.startTime.toString(),
                dateRangeSelector.endTime.toString(),
                selectorOrderStatus.selectedTag,
                selectorMethodStatus.selectedTag
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

    private fun setupWithdrawLogList()=binding.run {
        rvlist.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = withdrawLogAdapter
            addItemDecoration(
                DividerItemDecorator(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.divider_gray
                    )
                )
            )
            refreshHelper = RefreshHelper.of(rvlist, this@WithdrawLogFragment, false, true)
            refreshHelper.setLoadMoreListener(object : RefreshHelper.LoadMore {
                override fun onLoadMore(pageIndex: Int, pageSize: Int) {
                    reload(pageIndex, pageSize)
                }
            })
        }
    }

    private fun reload(pageIndex: Int, pageSize: Int) {
        viewModel.getUserWithdrawList(
            pageIndex==1,
            binding.dateRangeSelector.startTime.toString(),
            binding.dateRangeSelector.endTime.toString(),
            binding.selectorOrderStatus.selectedTag,
            binding.selectorMethodStatus.selectedTag
        )
    }

    private fun setupSearch() {
        binding.dateRangeSelector.setOnClickSearchListener {
            viewModel.getUserWithdrawList(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.queryByBettingStationIdResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { it ->
                if (it.success) {
                    it.data.appointmentTime = reserveTime
                    JumpUtil.toInternalWeb(
                        requireContext(),
                        "https://maps.google.com/?q=@" + it.data.lat + "," + it.data.lon,
                        getString(R.string.outlets_address),
                        true,
                        true,
                        it.data
                    )

                }
            }
        }

        viewModel.userWithdrawListResult.observe(this.viewLifecycleOwner) {
            LogUtil.d("isFinalPage="+viewModel.isFinalPage.value)
            if (viewModel.isFinalPage.value==true) {
                refreshHelper.finishLoadMoreWithNoMoreData()
            } else {
                refreshHelper.finishLoadMore()
            }
            it?.let {
                withdrawLogAdapter.setList(it)
                setupNoRecordView(it.isNullOrEmpty())
            }
        }

        viewModel.withdrawLogDetail.observe(this.viewLifecycleOwner) {
            if (it.getContentIfNotHandled() == null) return@observe

            if (logDetailDialog.dialog?.isShowing != true) {
                logDetailDialog.show(parentFragmentManager)
            }
        }

        viewModel.getUserWithdrawList(true)
    }

    private fun setupNoRecordView(visible: Boolean) {
        if (visible) {
            binding.viewNoRecord.root.visibility = View.VISIBLE
        } else {
            binding.viewNoRecord.root.visibility = View.GONE
        }
    }


    private val withdrawStateList by lazy {
        this.resources.getStringArray(R.array.withdraw_state_array).map {
            when (it) {
                getString(R.string.log_state_processing) -> {
                    StatusSheetData(CheckStatus.PROCESSING.code.toString(), it)
                }
                getString(R.string.recharge_state_success) -> {
                    StatusSheetData(CheckStatus.PASS.code.toString(), it)
                }
                getString(R.string.recharge_state_failed) -> {
                    StatusSheetData(CheckStatus.UN_PASS.code.toString(), it)
                }
                getString(R.string.N653) -> {
                    StatusSheetData(CheckStatus.PENDING.code.toString(), it)
                }
                else -> {
                    StatusSheetData(viewModel.allTag, it).apply { isChecked = true }
                }
            }
        }
    }
    private val withdrawTypeList by lazy {
        this.resources.getStringArray(R.array.withdraw_type_array).map {
            when (it) {
                getString(R.string.withdraw_log_type_bank_trans) -> {
                    StatusSheetData(UWType.BANK_TRANSFER.type, it)
                }
                getString(R.string.withdraw_log_type_admin) -> {
                    StatusSheetData(UWType.ADMIN_SUB_MONEY.type, it)
                }
                getString(R.string.withdraw_log_crypto_transfer) -> {
                    StatusSheetData(UWType.CRYPTO.type, it)
                }
                getString(R.string.ewallet) -> {
                    StatusSheetData(UWType.E_WALLET.type, it)
                }
                getString(R.string.online_maya) -> {
                    StatusSheetData(UWType.PAY_MAYA.type, it)
                }
                getString(R.string.betting_station_reserve) -> {
                    StatusSheetData(UWType.BETTING_STATION.type, it)
                }
                getString(R.string.betting_station_withdraw) -> {
                    StatusSheetData(UWType.BETTING_STATION_ADMIN.type, it)
                }
                //提款
                else -> {
                    StatusSheetData(viewModel.allTag, it).apply { isChecked = true }
                }
            }
        }
    }
}