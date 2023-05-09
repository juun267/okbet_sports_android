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
import kotlinx.android.synthetic.main.view_no_record.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui2.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.finance.df.RechType
import org.cxct.sportlottery.ui.finance.df.Status
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.DividerItemDecorator

/**
 * @app_destination 存款记录
 */
class RechargeLogFragment : BaseFragment<FinanceViewModel>(FinanceViewModel::class) {
    private var isSlidingToLast:Boolean = false
    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        private fun scrollToTopControl(firstVisibleItemPosition: Int) {
            iv_scroll_to_top.apply {
                when {
                    firstVisibleItemPosition > 0 && alpha == 0f -> {
                     //   visibility = View.VISIBLE
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
                viewModel.getUserRechargeList(false, date_range_selector.startTime.toString(),
                                              date_range_selector.endTime.toString(),
                                              selector_order_status.selectedTag,
                                              selector_method_status.selectedTag)

                scrollToTopControl(firstVisibleItemPosition)
            }
         //   isSlidingToLast = dy>0 //dy表示水平方向的滑动 大于0表示向下 小于0表示向上
            if ( !recyclerView.canScrollVertically(1)){//1表示是否能向上滚动 false表示已经到底部 -1表示是否能向下滚动false表示已经到顶部
                viewModel.userRechargeListResult.observe(this@RechargeLogFragment) {
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
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_recharge_log, container, false).apply {

            this.iv_scroll_to_top.setOnClickListener {
                rvlist.smoothScrollToPosition(0)
            }

            initNoRecordView(this)
            setupListColumn(this)
            setupRechargeLogList(this)
            setupSearch(this)
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
        view.rech_log_recharge_amount.text = getString(R.string.recharge_log_recharge_amount)
    }

    private fun setupRechargeLogList(view: View) {
        view.rvlist.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addOnScrollListener(recyclerViewOnScrollListener)
            this.adapter = rechargeLogAdapter
            addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.recycleview_decoration)))
        }
    }

    private fun setupSearch(view: View) {
        view.date_range_selector.setOnClickSearchListener {
            avoidFastDoubleClick()
            viewModel.getUserRechargeList(true,
                date_range_selector.startTime.toString(),
                date_range_selector.endTime.toString(),
                selector_order_status.selectedTag,
                selector_method_status.selectedTag)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selector_method_status.setItemData(rechargeChannelList as MutableList<StatusSheetData>)
        selector_order_status.setItemData(rechargeStateList as MutableList<StatusSheetData>)


        viewModel.userRechargeListResult.observe(this.viewLifecycleOwner) {
            it?.apply {
                rechargeLogAdapter.data = it
                setupNoRecordView(it.isNullOrEmpty())
            }
        }

        viewModel.rechargeLogDetail.observe(this.viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                if (logDetailDialog.dialog?.isShowing != true) {
                    logDetailDialog.show(
                        parentFragmentManager,
                        RechargeLogFragment::class.java.simpleName
                    )
                }
            }
        }

        viewModel.isFinalPage.observe(this.viewLifecycleOwner) {
            rechargeLogAdapter.isFinalPage = it
        }

        viewModel.getUserRechargeList(true)
    }

    private fun setupNoRecordView(visible: Boolean) {
        if (visible) {
            view_no_record.visibility = View.VISIBLE
        } else {
            view_no_record.visibility = View.GONE
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

}

