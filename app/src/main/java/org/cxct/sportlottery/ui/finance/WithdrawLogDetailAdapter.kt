package org.cxct.sportlottery.ui.finance

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.withdraw.list.Row
import org.cxct.sportlottery.repository.sConfigData


class WithdrawLogDetailAdapter(data: List<Row>?) :
    BaseQuickAdapter<Row, BaseViewHolder>(R.layout.item_withdraw_log_detail,
        data?.toMutableList()) {

    override fun convert(helper: BaseViewHolder, item: Row) {
        helper.setText(R.id.wd_log_detail_trans_num_subtitle,
            "${context.getString(R.string.N618)}：")
        helper.setText(R.id.wd_log_detail_amount_subtitle,
            "${context.getString(R.string.text_account_history_amount)}：")
        helper.setText(R.id.wd_log_detail_trans_num, item.orderNo)
        item.actualMoney?.let { nonNullDisplayMoney ->
            helper.setText(R.id.wd_log_detail_amount,
                "${sConfigData?.systemCurrencySign} $nonNullDisplayMoney")
        }
        helper.getView<TextView>(R.id.wd_log_detail_status).let {
            //用于前端显示单单订单状态 1: 處理中 2:提款成功 3:提款失败 4：待投注站出款
            it.text = when (item.orderState) {
                1 -> context.getString(R.string.log_state_processing)
                2 -> context.getString(R.string.L019)
                3 -> context.getString(R.string.N626)
                4 -> context.getString(R.string.N627)
                else -> null
            }
            when (item.orderState) {
                2 -> it.setTextColor(context.getColor(R.color.color_1EB65B))
                3 -> it.setTextColor(context.getColor(R.color.color_E23434))
                else -> it.setTextColor(context.getColor(R.color.color_BBBBBB_333333))
            }
        }
    }

}

