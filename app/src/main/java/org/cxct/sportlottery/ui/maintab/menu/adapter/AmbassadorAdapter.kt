package org.cxct.sportlottery.ui.maintab.menu.adapter

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemAmbassadorBinding

class AmbassadorAdapter : BindingAdapter<String, ItemAmbassadorBinding>() {
    var selectPos:Int? = null
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    override fun onBinding(position: Int, binding: ItemAmbassadorBinding, item: String) {
        binding.tvName.text = item
        binding.tvName.isSelected = (position==
                selectPos)
    }
}