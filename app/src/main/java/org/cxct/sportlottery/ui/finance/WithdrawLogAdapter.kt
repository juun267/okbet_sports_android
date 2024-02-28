package org.cxct.sportlottery.ui.finance

import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemWithdrawLogBinding
import org.cxct.sportlottery.network.withdraw.list.Row
import org.cxct.sportlottery.ui.finance.df.OrderState
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.TextUtil

class WithdrawLogAdapter : BindingAdapter<Row,ItemWithdrawLogBinding>() {


    var withdrawLogListener: WithdrawLogListener? = null

    override fun onBinding(position: Int, binding: ItemWithdrawLogBinding, item: Row) {
        binding.apply {
            rechLogDate.text = item.withdrawDate
            rechLogTime.text = item.withdrawTime
            tvOrderNumber.text = item.orderNo
            rechLogAmount.text = item.displayMoney
            tvReceiveAmount.text = TextUtil.formatMoney(item.actualMoney ?: 0.0)
            //用于前端显示单单订单状态 1: 處理中 2:提款成功 3:提款失败 4：待投注站出款
            rechLogState.apply {
                when (item.orderState) {
                    OrderState.PROCESSING.code -> {
                        text = LocalUtils.getString(R.string.log_state_processing)
                        setTextColor(ContextCompat.getColor(context,
                            R.color.color_414655))
                    }
                    OrderState.SUCCESS.code -> {
                        text = LocalUtils.getString(R.string.recharge_state_success)
                        setTextColor(ContextCompat.getColor(context,
                            R.color.color_1EB65B))
                    }
                    OrderState.FAILED.code -> {
                        text = LocalUtils.getString(R.string.recharge_state_failed)
                        setTextColor(ContextCompat.getColor(context,
                            R.color.color_E23434))
                    }
                    OrderState.PENGING.code -> {
                        text = LocalUtils.getString(R.string.N653)
                        setTextColor(ContextCompat.getColor(context,
                            R.color.color_414655))
                    }
                }
            }
        }

        binding.root.setOnClickListener {
            withdrawLogListener?.onClick(Event(item), false)
        }
        binding.linAmount.setOnClickListener {
            withdrawLogListener?.onClick(Event(item), true)
        }
    }
}

class WithdrawLogListener(val clickListener: (row: Event<Row>, clickMoney: Boolean) -> Unit) {
    fun onClick(row: Event<Row>, clickMoney: Boolean = false) = clickListener(row, clickMoney)
}