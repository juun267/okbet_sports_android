package org.cxct.sportlottery.ui.maintab.games.adapter

import org.cxct.sportlottery.databinding.ItemHotGameViewBinding
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.common.adapter.BindingAdapter

class RecyclerHotGameAdapter: BindingAdapter<String, ItemHotGameViewBinding>() {

    override fun onBinding(position: Int, binding: ItemHotGameViewBinding, item: String) {
        binding.tvMatchTitle.text=""
    }

}