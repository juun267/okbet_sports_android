package org.cxct.sportlottery.ui.money.recharge

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemQuickMoneyBinding
import org.cxct.sportlottery.util.TextUtil


class QuickMoneyAdapter : BindingAdapter<Int, ItemQuickMoneyBinding>() {
    private var selectPos: Int? = null
    override fun onBinding(position: Int, binding: ItemQuickMoneyBinding, item: Int) {
        binding.tvName.apply {
            text = TextUtil.formatBetQuota(item)
            isSelected = (selectPos == position)
        }
    }

    fun selectItem(position: Int) {
        selectPos = position
        notifyDataSetChanged()
    }

}