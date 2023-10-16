package org.cxct.sportlottery.ui.maintab.games.adapter

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemHomeFollowBinding
import org.cxct.sportlottery.ui.maintab.games.bean.FollowMenuBean

class RecyclerHomeFollowAdapter : BindingAdapter<FollowMenuBean, ItemHomeFollowBinding>() {

    override fun onBinding(position: Int, binding: ItemHomeFollowBinding, item: FollowMenuBean) {
        binding.ivCover.load(item.icon)
    }
}