package org.cxct.sportlottery.ui.finance

import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.withdraw.list.Row
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.finance.df.OrderState
import org.cxct.sportlottery.util.LocalUtils


class WithdrawLogDetailAdapter(data: List<Row>?) :
    BaseQuickAdapter<Row, BaseViewHolder>(R.layout.item_withdraw_log_detail,
        data?.toMutableList()) {

    override fun convert(helper: BaseViewHolder, item: Row) {
        helper.setText(R.id.wd_log_detail_trans_num_subtitle,
            "${context.getString(R.string.N618)}${helper.layoutPosition + 1}：")
        helper.setText(R.id.wd_log_detail_amount_subtitle,
            "${context.getString(R.string.text_account_history_amount)}：")
        helper.setText(R.id.wd_log_detail_trans_num, item.orderNo)
        item.actualMoney?.let { nonNullDisplayMoney ->
            helper.setText(R.id.wd_log_detail_amount,
                "${sConfigData?.systemCurrencySign} $nonNullDisplayMoney")
        }
        helper.getView<TextView>(R.id.wd_log_detail_status).apply {
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
