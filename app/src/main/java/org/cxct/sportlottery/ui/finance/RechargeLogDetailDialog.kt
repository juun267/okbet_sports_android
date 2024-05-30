package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogLogRechargeDetailBinding
import org.cxct.sportlottery.network.money.list.Row
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.finance.df.Status
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.copyText
import kotlin.math.abs

/**
 * @app_destination 存款详情弹窗
 */
class RechargeLogDetailDialog : BaseDialog<FinanceViewModel,DialogLogRechargeDetailBinding>() {

    companion object{
        fun newInstance(row: Row)= RechargeLogDetailDialog().apply{
            arguments = Bundle().apply {
                putParcelable("row",row)
            }
        }
    }
    private val row by lazy { arguments?.getParcelable("row") as Row? }

    init {
        marginHorizontal = 40.dp
    }

    override fun onInitView(): Unit =binding.run {
        logDetailConfirm.setOnClickListener {
            dismiss()
        }
        tvCopy.setOnClickListener {
            row?.orderNo?.let {
                requireContext().copyText(it)
                ToastUtil.showToastInCenter(activity, getString(R.string.text_money_copy_success))
            }
        }
        row?.let {
                logDetailTypeSubtitle.text = "${getString(R.string.tran_type)}："
                logDetailAmountSubtitle.text =
                    "${getString(R.string.text_account_history_amount)}："

                logDetailTransNum.text = it.orderNo
                logDetailTime.text = it.rechDateAndTime ?: ""
                logDetailType.text = it.rechTypeDisplay ?: ""
                logDetailStatus.text = it.rechState ?: ""
                when (it.status) {
                    Status.SUCCESS.code -> {
                        logDetailStatus.setTextColor(resources.getColor(R.color.color_1EB65B))
                    }
                    Status.FAILED.code -> {
                        logDetailStatus.setTextColor(resources.getColor(R.color.color_E23434))
                    }
                    else -> {
                        logDetailStatus.setTextColor(resources.getColor(R.color.color_BBBBBB_333333))
                    }
                }
                logDetailAmount.text = "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(it.rechMoney,0)}"
                logDetailReason.text = it.reason ?: ""

                (it.rebateMoney ?: 0.0).let { nonNullDisplayFee ->
                    logDetailRebate.text =
                        "${sConfigData?.systemCurrencySign} ${TextUtil.format(abs(nonNullDisplayFee))}"
                    logDetailRebateSubtitle.text =
                        if (nonNullDisplayFee > 0.0) getString(R.string.log_detail_rebate_money)
                        else getString(R.string.log_detail_handle_fee)

                }
        }
    }
}