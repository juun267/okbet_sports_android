package org.cxct.sportlottery.ui.redeem

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.RedeemCodeHistoryEntity
import org.cxct.sportlottery.util.TimeUtil

class RedeemAdapter :
    BaseQuickAdapter<RedeemCodeHistoryEntity, BaseViewHolder>(R.layout.item_redeem) {
    override fun convert(holder: BaseViewHolder, item: RedeemCodeHistoryEntity) {
        holder.setText(R.id.tvTime, item.date?.let { TimeUtil.timeFormat(it.toLong(),TimeUtil.YMD_HMS_FORMAT_CHANGE_LINE) })
        holder.setText(R.id.tvCode, item.redeemCode)
        item.rewards?.let { holder.setText(R.id.tvMoney, it) }
    }
}