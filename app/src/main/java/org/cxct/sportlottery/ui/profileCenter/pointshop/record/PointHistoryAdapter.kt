package org.cxct.sportlottery.ui.profileCenter.pointshop.record

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemPointHistoryBinding
import org.cxct.sportlottery.net.point.data.PointBill
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.view.setColors
import kotlin.math.absoluteValue

class PointHistoryAdapter: BindingAdapter<PointBill, ItemPointHistoryBinding>() {
    override fun onBinding(
        position: Int,
        binding: ItemPointHistoryBinding,
        item: PointBill,
    )=binding.run {
        tvName.text = item.reason
        var symbol = ""
        // 1:收入 2:支出
        if (item.type==1){
            tvAmount.setColors(R.color.color_1BC870)
            symbol= "+"
        }else{
            tvAmount.setColors(R.color.color_F06A75)
            symbol= "-"
        }
        tvAmount.text = "$symbol${TextUtil.formatMoney2(item.points.absoluteValue)}"
        tvTime.text = TimeUtil.stampToDateHMS(item.addTime)
    }

}