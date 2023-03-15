package org.cxct.sportlottery.ui.finance

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_withdraw_log_detail.*
import kotlinx.android.synthetic.main.dialog_withdraw_log_detail.view.*
import kotlinx.android.synthetic.main.item_withdraw_log.view.*
import kotlinx.android.synthetic.main.view_item_recharge_log.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.finance.df.OrderState
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.TextUtil
import kotlin.math.abs

/**
 * @app_destination 提款详情弹窗
 */
class WithdrawLogDetailDialog : BaseDialog<FinanceViewModel>(FinanceViewModel::class) {
    init {
        setStyle(R.style.CustomDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_withdraw_log_detail, container, false).apply {
            setupConfirmButton(this)
        }
    }

    private fun setupConfirmButton(view: View) {
        view.log_detail_confirm.setOnClickListener {
            dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.withdrawLogDetail.observe(this.viewLifecycleOwner) { event ->
            event.peekContent().let { it ->
                wd_log_detail_trans_num_subtitle.text = "${getString(R.string.J630)}："
                wd_log_detail_amount_subtitle.text =
                    "${getString(R.string.text_account_history_amount)}："
                wd_log_detail_trans_num.text = it.orderNo ?: ""
                wd_log_detail_time.text = it.withdrawDateAndTime ?: ""
                //用于前端显示单单订单状态 1: 處理中 2:提款成功 3:提款失败 4：待投注站出款
                wd_log_detail_status.apply {
                    when (it.orderState) {
                        OrderState.PROCESSING.code -> {
                            text = LocalUtils.getString(R.string.log_state_processing)
                            setTextColor(ContextCompat.getColor(context, R.color.color_414655))
                        }
                        OrderState.SUCCESS.code -> {
                            text = LocalUtils.getString(R.string.recharge_state_success)
                            setTextColor(ContextCompat.getColor(context, R.color.color_1EB65B))
                        }
                        OrderState.FAILED.code -> {
                            text = LocalUtils.getString(R.string.recharge_state_failed)
                            setTextColor(ContextCompat.getColor(context, R.color.color_E23434))
                        }
                        OrderState.PENGING.code -> {
                            text = LocalUtils.getString(R.string.N653)
                            setTextColor(ContextCompat.getColor(context, R.color.color_414655))
                        }
                    }
                }

//                wd_log_detail_review_time.text = it.operatorDateAndTime ?: ""
//                wd_log_detail_reason.text = it.reason ?: ""
                it.displayMoney?.let { nonNullDisplayMoney ->
                    wd_log_detail_amount.text =
                        "${sConfigData?.systemCurrencySign} $nonNullDisplayMoney"
                }

                it.withdrawDeductMoney?.let { nonNullDeductMoney ->
                    wd_log_detail_commission.text =
                        "${sConfigData?.systemCurrencySign} $nonNullDeductMoney"
                }

                (it.fee ?: 0.0).let { fee ->
                    wd_log_detail_handle_fee.text =
                        "${sConfigData?.systemCurrencySign} ${TextUtil.format(abs(fee))}"
                    wd_log_detail_handle_fee_subtitle.text =
                        if ((fee) > 0.0) getString(R.string.log_detail_rebate_money)
                        else getString(R.string.log_detail_handle_fee)
                }
                it.children.let {
                    if (it.isNullOrEmpty()) {
                        nsv_content.layoutParams.apply {
                            height = LinearLayout.LayoutParams.WRAP_CONTENT
                        }
                    } else {
                        nsv_content.layoutParams.apply {
                            height = 310.dp
                            nsv_content.layoutParams = this
                        }
                        rv_child.layoutManager =
                            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                        rv_child.adapter = WithdrawLogDetailAdapter(it)
                    }
                }
            }
        }
    }
}