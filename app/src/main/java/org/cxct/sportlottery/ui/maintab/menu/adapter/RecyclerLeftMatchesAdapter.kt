package org.cxct.sportlottery.ui.maintab.menu.adapter

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemLeftHotMatchesBinding
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.util.setLeagueLogo

class RecyclerLeftMatchesAdapter: BindingAdapter<Recommend,ItemLeftHotMatchesBinding>() {

    override fun onBinding(position: Int, binding: ItemLeftHotMatchesBinding, item: Recommend) {
        binding.tvLeagueName.text= item.leagueName
        binding.ivLeagueCover.setLeagueLogo(item.categoryIcon)
    }
}