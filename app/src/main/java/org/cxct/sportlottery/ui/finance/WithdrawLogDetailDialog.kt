package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.dialog_withdraw_log_detail.*
import kotlinx.android.synthetic.main.dialog_withdraw_log_detail.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.util.TextUtil
import kotlin.math.abs

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.withdrawLogDetail.observe(this.viewLifecycleOwner) { event ->
            event.peekContent().let {
                wd_log_detail_trans_num.text = it.orderNo ?: ""
                wd_log_detail_time.text = it.withdrawDateAndTime ?: ""
                wd_log_detail_status.text = it.withdrawState ?: ""
                wd_log_detail_review_time.text = it.operatorDateAndTime ?: ""
                wd_log_detail_reason.text = it.reason ?: ""
3
                it.displayMoney?.let { nonNullDisplayMoney ->
                    wd_log_detail_amount.text = "$nonNullDisplayMoney ${sConfigData?.systemCurrency}"
                }

                it.withdrawDeductMoney?.let { nonNullDeductMoney ->
                    wd_log_detail_commission.text = "$nonNullDeductMoney ${sConfigData?.systemCurrency}"
                }

                (it.fee ?: 0.0).let { fee ->
                    wd_log_detail_handle_fee.text = "${TextUtil.format(abs(fee))} ${sConfigData?.systemCurrency}"
                    wd_log_detail_handle_fee_subtitle.text =
                        if ((fee) > 0.0) getString(R.string.log_detail_rebate_money)
                        else getString(R.string.log_detail_handle_fee)
                }
            }
        }
    }
}