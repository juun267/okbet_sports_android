package org.cxct.sportlottery.ui.maintab.home.view

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemHomeVenuesBinding
import org.cxct.sportlottery.network.index.config.HomeGameBean

class RecyclerVenuesAdapter : BindingAdapter<HomeGameBean, ItemHomeVenuesBinding>()  {
    override fun onBinding(position: Int, binding: ItemHomeVenuesBinding, item: HomeGameBean) {
        binding.run {
            tvSportClose.text=item.gameName
        }
    }
}