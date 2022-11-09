package org.cxct.sportlottery.ui.finance

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_log_recharge_detail.*
import kotlinx.android.synthetic.main.dialog_log_recharge_detail.view.*
import kotlinx.android.synthetic.main.view_item_recharge_log.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.finance.df.Status
import org.cxct.sportlottery.util.TextUtil
import kotlin.math.abs

/**
 * @app_destination 存款详情弹窗
 */
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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.rechargeLogDetail.observe(this.viewLifecycleOwner) { event ->
            event.peekContent().let {

                log_detail_type_subtitle.text = "${getString(R.string.tran_type)}："
                log_detail_amount_subtitle.text =
                    "${getString(R.string.text_account_history_amount)}："

                log_detail_trans_num.text = it.orderNo
                log_detail_time.text = it.rechDateAndTime ?: ""
                log_detail_type.text = it.rechTypeDisplay ?: ""
                log_detail_status.text = it.rechState ?: ""
                when (it.status) {
                    Status.SUCCESS.code -> {
                        log_detail_status.setTextColor(resources.getColor(R.color.color_1EB65B))
                    }
                    Status.FAILED.code -> {
                        log_detail_status.setTextColor(resources.getColor(R.color.color_E23434))
                    }
                    else -> {
                        log_detail_status.setTextColor(resources.getColor(R.color.color_BBBBBB_333333))
                    }
                }
                log_detail_amount.text = "${sConfigData?.systemCurrencySign} ${it.displayMoney}"
                log_detail_reason.text = it.reason ?: ""

                (it.rebateMoney ?: 0.0).let { nonNullDisplayFee ->
                    log_detail_rebate.text =
                        "${sConfigData?.systemCurrencySign} ${TextUtil.format(abs(nonNullDisplayFee))}"
                    log_detail_rebate_subtitle.text =
                        if (nonNullDisplayFee > 0.0) getString(R.string.log_detail_rebate_money)
                        else getString(R.string.log_detail_handle_fee)

                }

            }
        }
    }
}