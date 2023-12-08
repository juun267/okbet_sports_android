package org.cxct.sportlottery.ui.promotion

import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemRewardHistoryBinding
import org.cxct.sportlottery.net.user.data.RewardRecord
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil


class RewardHistoryAdapter: BindingAdapter<RewardRecord, ItemRewardHistoryBinding>() {

    override fun onBinding(position: Int, binding: ItemRewardHistoryBinding, item: RewardRecord) = binding.run {
        tvTime.text = TimeUtil.timeFormat(item.addTime,TimeUtil.YMD_HMS_FORMAT_CHANGE_LINE)
        tvAmount.text = "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(item.rewardAmount)}"
        when(item.recordStatus){//1待审核，2已发放，3已拒绝
            1-> {
                tvStatus.text = context.getString(R.string.N633)
                tvStatus.setTextColor(ContextCompat.getColor(context,R.color.color_1CD219))
            }
            2-> {
                tvStatus.text = context.getString(R.string.J942)
                tvStatus.setTextColor(ContextCompat.getColor(context,R.color.color_BEC7DC))
            }
            3-> {
                tvStatus.text = context.getString(R.string.N417)
                tvStatus.setTextColor(ContextCompat.getColor(context,R.color.color_ff0000))
            }
            else->{
                tvStatus.text = ""
            }
        }
    }
}