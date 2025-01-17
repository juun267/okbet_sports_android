package org.cxct.sportlottery.ui.maintab.menu.adapter

import androidx.core.view.isVisible
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemLeftHotMatchesBinding
import org.cxct.sportlottery.net.sport.data.RecommendLeague
import org.cxct.sportlottery.util.setLeagueLogo

class RecyclerLeftMatchesAdapter: BindingAdapter<RecommendLeague,ItemLeftHotMatchesBinding>() {

    override fun onBinding(position: Int, binding: ItemLeftHotMatchesBinding, item: RecommendLeague)= binding.run {
        tvLeagueName.text= item.name
        ivLeagueCover.setLeagueLogo(item.categoryIcon)
        tvLine.isVisible = position != itemCount-1
    }
}