package org.cxct.sportlottery.ui.money.recharge

import org.cxct.sportlottery.databinding.ItemQuickMoneyBinding
import org.cxct.sportlottery.ui.common.adapter.BindingAdapter
import org.cxct.sportlottery.util.TextUtil


class QuickMoneyAdapter : BindingAdapter<String, ItemQuickMoneyBinding>() {
    private var selectPos: Int? = null
    override fun onBinding(position: Int, binding: ItemQuickMoneyBinding, item: String) {
        binding.tvName.apply {
            text = TextUtil.formatMoney(item)
            isSelected = (selectPos == position)
        }
    }

    fun selectItem(position: Int) {
        selectPos = position
        notifyDataSetChanged()
    }

}