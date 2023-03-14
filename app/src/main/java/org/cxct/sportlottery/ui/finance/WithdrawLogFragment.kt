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
import kotlinx.android.synthetic.main.activity_withdraw_log.*
import kotlinx.android.synthetic.main.activity_withdraw_log.view.*
import kotlinx.android.synthetic.main.component_date_range_selector.view.*
import kotlinx.android.synthetic.main.view_no_record.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.finance.df.CheckStatus
import org.cxct.sportlottery.ui.finance.df.UWType
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.JumpUtil

/**
 * @app_destination 提款记录
 */
class WithdrawLogFragment : BaseFragment<FinanceViewModel>(FinanceViewModel::class) {
    private var reserveTime: String = ""
    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            //TODO 位置改动 这个后续要删除掉 暂时隐藏
            private fun scrollToTopControl(firstVisibleItemPosition: Int) {
                iv_scroll_to_top.apply {
                    when {
                        firstVisibleItemPosition > 0 && alpha == 0f -> {
                           // visibility = View.VISIBLE
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
                    viewModel.getUserWithdrawList(
                        false,
                        date_range_selector.startTime.toString(),
                        date_range_selector.endTime.toString(),
                        selector_order_status.selectedTag,
                        selector_method_status.selectedTag
                    )
                    scrollToTopControl(firstVisibleItemPosition)
                }
           //     isSlidingToLast = dy>0 //dy表示水平方向的滑动 大于0表示向下 小于0表示向上
                if ( !recyclerView.canScrollVertically(1)){//1表示是否能向上滚动 false表示已经到底部 -1表示是否能向下滚动false表示已经到顶部
                    viewModel.userWithdrawListResult.observe(this@WithdrawLogFragment) {
                        if (it.isNullOrEmpty()){
                            tv_no_data.visibility = View.GONE
                        }else{
                            tv_no_data.visibility = View.VISIBLE
                        }
                    }
                }else{
                    tv_no_data.visibility = View.GONE
                }
            }

        }

    private val logDetailDialog by lazy {
        WithdrawLogDetailDialog()
    }

    private val withdrawLogAdapter by lazy {
        WithdrawLogAdapter().apply {
            withdrawLogListener = WithdrawLogListener(
                clickListener = { event ->
                    event.peekContent()?.let {
                        if (it.uwType == UWType.BETTING_STATION.type) {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_withdraw_log, container, false).apply {
            this.selector_order_status.setItemData(withdrawStateList as MutableList<StatusSheetData>)
            this.selector_method_status.setItemData(withdrawTypeList as MutableList<StatusSheetData>)
            setupListColumn(this)
            setupWithdrawLogList(this)
            setupSearch(this)
            initOnclick(this)
            initNoRecordView(this)
        }
    }

    private fun initOnclick(view: View) {
        //TODO 位置改动 这个方法要删除掉 暂时隐藏
        view.iv_scroll_to_top.setOnClickListener {
            view.rvlist.smoothScrollToPosition(0)
        }

        view.date_range_selector.setOnClickSearchListener {
            viewModel.getUserWithdrawList(
                true, date_range_selector.startTime.toString(),
                date_range_selector.endTime.toString(),
                selector_order_status.selectedTag,
                selector_method_status.selectedTag
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

    private fun setupListColumn(view: View) {
        view.rech_log_recharge_amount.text = getString(R.string.withdraw_amount)
    }

    private fun setupWithdrawLogList(view: View) {
        view.rvlist.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addOnScrollListener(recyclerViewOnScrollListener)
            this.adapter = withdrawLogAdapter
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
                        "https://maps.google.com/?q=@" + it.data.lon + "," + it.data.lat,
                        getString(R.string.outlets_address),
                        true,
                        true,
                        it.data
                    )

                }
            }
        }

        viewModel.userWithdrawListResult.observe(this.viewLifecycleOwner) {
            it?.let {
                withdrawLogAdapter.data = it
                setupNoRecordView(it.isNullOrEmpty())
            }
        }

        viewModel.withdrawLogDetail.observe(this.viewLifecycleOwner) {
            if (it.getContentIfNotHandled() == null) return@observe

            if (logDetailDialog.dialog?.isShowing != true) {
                logDetailDialog.show(
                    parentFragmentManager,
                    WithdrawLogFragment::class.java.simpleName
                )
            }
        }

        viewModel.isFinalPage.observe(this.viewLifecycleOwner) {
            withdrawLogAdapter.isFinalPage = it
        }

        viewModel.getUserWithdrawList(true)
    }

    private fun setupNoRecordView(visible: Boolean) {
        if (visible) {
            view_no_record.visibility = View.VISIBLE
        } else {
            view_no_record.visibility = View.GONE
        }
    }


    private val withdrawStateList by lazy {
        this.resources.getStringArray(R.array.withdraw_state_array).map {
            when (it) {
                getString(R.string.log_state_processing) -> {
                    StatusSheetData(CheckStatus.PROCESSING.code.toString(), it)
                }
                getString(R.string.L019) -> {
                    StatusSheetData(CheckStatus.PASS.code.toString(), it)
                }
                getString(R.string.N626) -> {
                    StatusSheetData(CheckStatus.UN_PASS.code.toString(), it)
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