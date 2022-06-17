package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.dialog_log_recharge_detail.*
import kotlinx.android.synthetic.main.dialog_log_recharge_detail.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.util.TextUtil
import kotlin.math.abs

class RechargeLogDetailDialog : BaseDialog<FinanceViewModel>(FinanceViewModel::class) {
    init {
        setStyle(R.style.CustomDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_log_recharge_detail, container, false).apply {
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

        viewModel.rechargeLogDetail.observe(this.viewLifecycleOwner) { event ->
            event.peekContent().let {
                log_detail_trans_num.text = it.orderNo
                log_detail_time.text = it.rechDateAndTime ?: ""
                log_detail_type.text = it.rechTypeDisplay ?: ""
                log_detail_status.text = it.rechState ?: ""
                log_detail_amount.text = "${sConfigData?.systemCurrencySign} ${it.displayMoney}"
                log_detail_reason.text = it.reason ?: ""

                (it.rebateMoney ?: 0.0).let { nonNullDisplayFee ->
                    log_detail_rebate.text ="${sConfigData?.systemCurrencySign} ${TextUtil.format(abs(nonNullDisplayFee))}"
                    log_detail_rebate_subtitle.text =
                        if (nonNullDisplayFee > 0.0) getString(R.string.log_detail_rebate_money)
                        else getString(R.string.log_detail_handle_fee)

                }

            }
        }
    }
}