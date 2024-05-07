package org.cxct.sportlottery.ui.profileCenter.vip

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.FrameLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.databinding.ItemActivatedBenefitsBinding
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import splitties.views.dsl.core.horizontalMargin

class ActivatedBenefitsAdapter(cxt: Context): BindingAdapter<String, ItemActivatedBenefitsBinding>() {

    private val bg by lazy { ShapeDrawable().setSolidColor(Color.WHITE).setRadius(8.dp.toFloat()) }
    private val bg0 by lazy { ShapeDrawable().setSolidColor(context.getColor(R.color.color_025BE8)).setRadius(8.dp.toFloat()) }
    private val bg1 by lazy {
        val dp40 = 40.dp
        ShapeDrawable().setSolidColor(context.getColor(R.color.color_F1F4FC))
            .setWidth(dp40)
            .setHeight(dp40)
            .setRadius(dp40.toFloat())
    }

    init {
        setNewInstance(mutableListOf("1", "2", "3", "4","5","6","7","8","9","A"))
        val emptyView = enableDefaultEmptyView(cxt, R.drawable.img_no_benefits)
        emptyView.layoutParams = FrameLayout.LayoutParams(-1, 244.dp).apply { horizontalMargin = 12.dp }
        emptyView.background = ShapeDrawable().setSolidColor(Color.WHITE).setRadius(8.dp.toFloat())
        emptyView.setEmptyText("There are no benefits for the current level. Please continue betting to improve the level.")
    }

    override fun onCreateDefViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingVH<ItemActivatedBenefitsBinding> {
        val holder = super.onCreateDefViewHolder(parent, viewType)
        holder.itemView.background = bg
        holder.vb.ivBenefits.background = bg1

        return holder
    }

    override fun onBinding(position: Int, binding: ItemActivatedBenefitsBinding, item: String) = binding.run {
        ivBenefits.setImageResource(R.drawable.ic_vip_benfit)
        if (position % 2 == 0) {
            tvAction.background = bg0
            tvAction.setTextColor(Color.WHITE)
        } else {
            tvAction.background = null
            tvAction.setTextColor(context.getColor(R.color.color_999999))
        }
    }
}