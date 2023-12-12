package org.cxct.sportlottery.ui.money.recharge

import androidx.core.view.isVisible
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.ItemQuickMoneyBinding
import org.cxct.sportlottery.util.TextUtil


class QuickMoneyAdapter : BindingAdapter<Int, ItemQuickMoneyBinding>() {
    private var selectPos: Int? = null
    private var percent:Int = 0
    override fun onBinding(position: Int, binding: ItemQuickMoneyBinding, item: Int) {
        binding.tvName.apply {
            text = TextUtil.formatBetQuota(item)
            isSelected = (selectPos == position)
        }
        binding.tvTips.apply {
            text = "${percent}%"
            isVisible = percent > 0
        }
    }

    fun selectItem(position: Int) {
        selectPos = position
        notifyDataSetChanged()
    }
    fun setPercent(percent: Int){
        this.percent = percent
        notifyDataSetChanged()
    }

}