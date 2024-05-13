package org.cxct.sportlottery.ui.profileCenter.vip

import android.graphics.Color
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.common.enums.UserVipType
import org.cxct.sportlottery.databinding.ItemUnactivatedBenefitsBinding
import org.cxct.sportlottery.net.user.data.RewardDetail
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable

class UnactivatedBenefitsAdapter: BindingAdapter<RewardDetail, ItemUnactivatedBenefitsBinding>() {

    private val bg by lazy { ShapeDrawable().setSolidColor(Color.WHITE).setRadius(8.dp.toFloat()) }
    private val bg1 by lazy {
        val dp40 = 40.dp
        ShapeDrawable().setSolidColor(context.getColor(R.color.color_F1F4FC))
            .setWidth(dp40)
            .setHeight(dp40)
            .setRadius(dp40.toFloat())
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

    override fun onBinding(position: Int, binding: ItemUnactivatedBenefitsBinding, item: RewardDetail)=binding.run {
        when(item.rewardType){
            UserVipType.REWARD_TYPE_PROMOTE->{
                 ivBenefits.setImageResource(R.drawable.ic_vip_bonus_promote)
                 tvBenefitsName.text = context.getString(R.string.P363)
            }
            UserVipType.REWARD_TYPE_WEEKLY->{
                ivBenefits.setImageResource(R.drawable.ic_vip_bonus_weekly)
                tvBenefitsName.text = context.getString(R.string.P364)
            }
            UserVipType.REWARD_TYPE_BIRTHDAY->{
                ivBenefits.setImageResource(R.drawable.ic_vip_bonus_birthday)
                tvBenefitsName.text = context.getString(R.string.P365)
            }
            UserVipType.REWARD_TYPE_PACKET->{
                ivBenefits.setImageResource(R.drawable.ic_vip_bonus_packet)
                tvBenefitsName.text = context.getString(R.string.P366)
            }
        }
        tvAmount.text = TextUtil.formatMoney(item.value)
    }
}