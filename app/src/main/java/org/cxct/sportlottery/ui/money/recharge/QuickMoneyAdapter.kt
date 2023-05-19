package org.cxct.sportlottery.ui.money.recharge

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemQuickMoneyBinding


class QuickMoneyAdapter : BindingAdapter<String, ItemQuickMoneyBinding>() {
    var selectPos: Int? = null
    override fun onBinding(position: Int, binding: ItemQuickMoneyBinding, item: String) {
        binding.tvName.apply {
            text = item
            isSelected = (selectPos == position)
        }
    }

    fun selectItem(position: Int) {
        selectPos = position
        notifyDataSetChanged()
    }

}