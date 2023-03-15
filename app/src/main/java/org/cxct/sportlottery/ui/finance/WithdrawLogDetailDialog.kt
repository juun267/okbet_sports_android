package org.cxct.sportlottery.ui.finance

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_withdraw_log_detail.*
import kotlinx.android.synthetic.main.dialog_withdraw_log_detail.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
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
            event.peekContent().let {

                wd_log_detail_amount_subtitle.text =
                    "${getString(R.string.text_account_history_amount)}："

                wd_log_detail_trans_num.text = it.orderNo ?: ""
                wd_log_detail_time.text = it.withdrawDateAndTime ?: ""
                wd_log_detail_status.text = it.withdrawState ?: ""
                when (it.withdrawState) {
                    "通过" -> wd_log_detail_status.setTextColor(resources.getColor(R.color.color_1EB65B))
                    "未通过" -> wd_log_detail_status.setTextColor(resources.getColor(R.color.color_E23434))
                    else -> wd_log_detail_status.setTextColor(resources.getColor(R.color.color_BBBBBB_333333))
                }
                wd_log_detail_review_time.text = it.operatorDateAndTime ?: ""
                wd_log_detail_reason.text = it.reason ?: ""
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
            }
        }
    }
}