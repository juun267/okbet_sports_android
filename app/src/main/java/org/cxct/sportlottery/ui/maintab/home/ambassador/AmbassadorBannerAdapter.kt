package org.cxct.sportlottery.ui.maintab.home.ambassador

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemAmbassadorBannerBinding

class AmbassadorBannerAdapter : BindingAdapter<Int, ItemAmbassadorBannerBinding>() {

    override fun onBinding(position: Int, binding: ItemAmbassadorBannerBinding, item: Int) {
        binding.ivImage.setImageResource(item)
    }
}