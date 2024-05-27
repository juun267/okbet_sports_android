package org.cxct.sportlottery.ui.finance

import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemWithdrawLogDetailBinding
import org.cxct.sportlottery.network.withdraw.list.Row
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.finance.df.OrderState
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.TextUtil


class WithdrawLogDetailAdapter: BindingAdapter<Row, ItemWithdrawLogDetailBinding>() {

    override fun onBinding(position: Int, binding: ItemWithdrawLogDetailBinding, item: Row): Unit = binding.run {

        wdLogDetailTransNumSubtitle.text = "${context.getString(R.string.N618)}${position + 1}："
        wdLogDetailAmountSubtitle.text =
            "${context.getString(R.string.text_account_history_amount)}："
        wdLogDetailTransNum.text = item.orderNo
        wdLogDetailAmount.text =
            "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(item.actualMoney ?: 0, 0)}"
        wdLogDetailStatus.apply {
            //用于前端显示单单订单状态 1: 處理中 2:提款成功 3:提款失败 4：待投注站出款
            when (item.orderState) {
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
    }

}

