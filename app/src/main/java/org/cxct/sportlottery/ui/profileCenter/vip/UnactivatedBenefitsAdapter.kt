package org.cxct.sportlottery.ui.profileCenter.vip

import android.graphics.Color
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.databinding.ItemUnactivatedBenefitsBinding
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable

class UnactivatedBenefitsAdapter: BindingAdapter<String, ItemUnactivatedBenefitsBinding>() {

    private val bg by lazy { ShapeDrawable().setSolidColor(Color.WHITE).setRadius(8.dp.toFloat()) }
    private val bg1 by lazy {
        val dp40 = 40.dp
        ShapeDrawable().setSolidColor(context.getColor(R.color.color_F1F4FC))
            .setWidth(dp40)
            .setHeight(dp40)
            .setRadius(dp40.toFloat())
    }

    init {
        setNewInstance(mutableListOf("1", "1", "1", "1","1","1","1","1","1","1"))
    }

    override fun onCreateDefViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingVH<ItemUnactivatedBenefitsBinding> {
        val holder = super.onCreateDefViewHolder(parent, viewType)
        holder.itemView.background = bg
        holder.vb.ivBenefits.background = bg1
        return holder
    }

    override fun onBinding(position: Int, binding: ItemUnactivatedBenefitsBinding, item: String) {
        binding.ivBenefits.setImageResource(R.drawable.ic_vip_benfit)
    }
}