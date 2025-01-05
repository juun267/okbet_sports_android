package org.cxct.sportlottery.ui.profileCenter.pointshop.order

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemGoodsThumbBinding
import org.cxct.sportlottery.repository.sConfigData

class GoodsThumbAdapter: BindingAdapter<String, ItemGoodsThumbBinding>() {

    var selectPos = -1
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: BindingVH<ItemGoodsThumbBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.vb.ivThumb.load("${sConfigData?.resServerHost}${getItem(position)}")
        holder.vb.ivThumb.alpha = if (selectPos == position) 1.0f else 0.5f
    }

    override fun onBinding(position: Int, binding: ItemGoodsThumbBinding, item: String) {

    }

}