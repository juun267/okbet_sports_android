package org.cxct.sportlottery.ui.maintab.menu.adapter

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemLeftInPlayBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.Item

class RecyclerInPlayAdapter: BindingAdapter<Item,ItemLeftInPlayBinding>() {

    override fun onBinding(position: Int, binding: ItemLeftInPlayBinding, item: Item) {
        binding.tvSportName.text=item.name
        binding.tvSportNum.text="${item.num}"
        binding.ivCover.load(GameType.getLeftGameTypeMenuIcon(item.code))
    }
}