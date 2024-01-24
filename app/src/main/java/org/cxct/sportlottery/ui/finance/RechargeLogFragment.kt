package org.cxct.sportlottery.ui.finance

import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.databinding.FragmentRechargeLogBinding
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.finance.df.RechType
import org.cxct.sportlottery.ui.finance.df.Status
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.RefreshHelper
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.view.DividerItemDecorator

/**
 * @app_destination 存款记录
 */
class RechargeLogFragment : BindingFragment<FinanceViewModel,FragmentRechargeLogBinding>(), OnItemClickListener {

    private lateinit var refreshHelper: RefreshHelper

    private val logDetailDialog by lazy {
        RechargeLogDetailDialog()
    }

    private val rechargeAdapter by lazy { RechargeLogAdapter() }

    override fun onInitView(view: View) {
        binding.ivScrollToTop.setOnClickListener { binding.rvlist.smoothScrollToPosition(0) }
        setupListColumn()
        setupRechargeLogList()
        setupSearch()
        initObserver()
        reload()
    }

    private fun reload(pageIndex: Int = 1, pageSize: Int = refreshHelper.pageSize) {
        viewModel.getUserRechargeList(pageIndex,
            pageSize,
            binding.dateRangeSelector.startTime.toString(),
            binding.dateRangeSelector.endTime.toString(),
            binding.selectorOrderStatus.selectedTag,
            binding.selectorMethodStatus.selectedTag)
    }

    private fun setupListColumn() {
        binding.rechLogRechargeAmount.text = getString(R.string.recharge_log_recharge_amount)
    }

    private fun setupRechargeLogList()=binding.run {
        rvlist.setLinearLayoutManager()
        rvlist.adapter = rechargeAdapter
        rechargeAdapter.setOnItemClickListener(this@RechargeLogFragment)
        rechargeAdapter.setEmptyView(LayoutInflater.from(view.context).inflate(R.layout.view_no_record, null))
        rvlist.addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(view.context, R.drawable.recycleview_decoration)))
        refreshHelper = RefreshHelper.of(rvlist, this@RechargeLogFragment, false, true)
        refreshHelper.setLoadMoreListener(object : RefreshHelper.LoadMore {
            override fun onLoadMore(pageIndex: Int, pageSize: Int) {
                reload(pageIndex, pageSize)
            }
        })
    }

    private fun resetListStatus() {
        rechargeAdapter.setList(null)
        refreshHelper.reset()
    }

    private fun startReload() {
        resetListStatus()
        reload()
    }

    private fun setupSearch() {
        binding.dateRangeSelector.clickDelay {
            startReload()
        }
    }

    private fun initObserver() {
        binding.selectorMethodStatus.setItemData(rechargeChannelList as MutableList<StatusSheetData>)
        binding.selectorOrderStatus.setItemData(rechargeStateList as MutableList<StatusSheetData>)
        binding.selectorMethodStatus.setOnItemSelectedListener { startReload() }
        binding.selectorOrderStatus.setOnItemSelectedListener { startReload() }

        viewModel.rechargeLogDataList.observe(viewLifecycleOwner) {
            if (it.first == null || it.first!!.size < refreshHelper.pageSize) {
                refreshHelper.finishLoadMoreWithNoMoreData()
            } else {
                refreshHelper.finishLoadMore()
            }

            if (it.first == null && !it.second) {
                if (it.third.isEmptyStr()) {
                    ToastUtil.showToast(context, R.string.J871)
                } else {
                    ToastUtil.showToast(context, it.third)
                }
                return@observe
            }

            if (!it.first.isNullOrEmpty()) {
                rechargeAdapter.addData(it.first!!)
            }
        }

        viewModel.rechargeLogDetail.observe(this.viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                if (logDetailDialog.dialog?.isShowing != true) {
                    logDetailDialog.show(parentFragmentManager,
                        RechargeLogFragment::class.java.simpleName
                    )
                }
            }
        }
    }

    private val rechargeChannelList by lazy { this.resources.getStringArray(R.array.recharge_channel_array).map {
        when (it) {
            getString(R.string.recharge_channel_online) -> {
                StatusSheetData(RechType.ONLINE_PAYMENT.type, it)
            }
            getString(R.string.recharge_channel_bank) -> {
                StatusSheetData(RechType.BANK_TRANSFER.type, it)
            }
            getString(R.string.recharge_channel_alipay) -> {
                StatusSheetData(RechType.ALIPAY.type, it)
            }
            getString(R.string.recharge_channel_weixin) -> {
                StatusSheetData(RechType.WEIXIN.type, it)
            }
            getString(R.string.recharge_channel_cft) -> {
                StatusSheetData(RechType.CFT.type, it)
            }
            getString(R.string.recharge_channel_admin) -> {
                StatusSheetData(RechType.ADMIN_ADD_MONEY.type, it)
            }
            getString(R.string.recharge_channel_crypto) -> {
                StatusSheetData(RechType.CRYPTO.type, it)
            }
            getString(R.string.recharge_channel_gcash) -> {
                StatusSheetData(RechType.GCASH.type, it)
            }
            getString(R.string.recharge_channel_grabpay) -> {
                StatusSheetData(RechType.GRABPAY.type, it)
            }
            getString(R.string.recharge_channel_paymaya) -> {
                StatusSheetData(RechType.PAYMAYA.type, it)
            }
            getString(R.string.betting_station_deposit) -> {
                StatusSheetData(RechType.BETTING_STATION.type, it)
            }
            getString(R.string.P183) -> {
                StatusSheetData(RechType.BETTING_STATION_AGENT.type, it)
            }
            getString(R.string.text_account_history_activity) -> {
                StatusSheetData(RechType.ACTIVITY.type, it)
            }
            getString(R.string.P216) -> {
                StatusSheetData(RechType.REDEMTIONCODE.type, it)
            }
            //全部渠道类型
            else -> {
                StatusSheetData(viewModel.allTag, it).apply { isChecked = true }
            }
        }
    }
    }

    private val rechargeStateList by lazy {
        this.resources.getStringArray(R.array.recharge_state_array).map {
            when (it) {
                getString(R.string.log_state_processing) -> {
                    StatusSheetData(Status.PROCESSING.code.toString(), it)
                }
                getString(R.string.recharge_state_success) -> {
                    StatusSheetData(Status.SUCCESS.code.toString(), it)

                }
                getString(R.string.recharge_state_failed) -> {
                    StatusSheetData(Status.FAILED.code.toString(), it)
                }
                else -> {
                    StatusSheetData(viewModel.allTag, it).apply { isChecked = true }
                }
            }
        }
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        viewModel.setLogDetail(Event(rechargeAdapter.getItem(position)))
    }

}

