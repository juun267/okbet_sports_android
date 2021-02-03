package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.dialog_withdraw_log_detail.*
import kotlinx.android.synthetic.main.dialog_withdraw_log_detail.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog

class WithdrawLogDetailDialog : BaseDialog<FinanceViewModel>(FinanceViewModel::class) {
    init {
        setStyle(R.style.CustomDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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

        viewModel.withdrawLogDetail.observe(this.viewLifecycleOwner, Observer {
            wd_log_detail_trans_num.text = it.orderNo ?: ""
            wd_log_detail_time.text = it.withdrawDateAndTime ?: ""
            wd_log_detail_status.text = it.withdrawState ?: ""
            wd_log_detail_review_time.text = it.operatorDateAndTime ?: ""

            it.displayMoney?.let { nonNullDisplayMoney ->
                wd_log_detail_amount.text = getString(R.string.finance_rmb, nonNullDisplayMoney)
            }

            it.displayFee?.let { nonNullDisplayFee ->
                wd_log_detail_handle_fee.text = getString(R.string.finance_rmb, nonNullDisplayFee)
            }
        })
    }
}