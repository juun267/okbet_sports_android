package org.cxct.sportlottery.ui.profileCenter.vip

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.enums.UserVipType
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.databinding.ItemUnactivatedBenefitsBinding
import org.cxct.sportlottery.net.user.data.RewardDetail
import org.cxct.sportlottery.util.TextUtil

class UnactivatedBenefitsAdapter: BindingAdapter<RewardDetail, ItemUnactivatedBenefitsBinding>() {


    override fun onBinding(position: Int, binding: ItemUnactivatedBenefitsBinding, item: RewardDetail)=binding.run {
        if (item.otherType==1){
            ivBenefits.setImageResource(R.drawable.ic_vip_bonus_support)
            tvBenefitsName.text = context.getString(R.string.P402)
            tvAmount.text = "24x7"
            return@run
        }
        if (item.otherType==2){
            ivBenefits.setImageResource(R.drawable.ic_vip_bonus_disbursement)
            tvBenefitsName.text = context.getString(R.string.P404)
            tvAmount.text = context.getString(R.string.P411)
            return@run
        }
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
        tvAmount.text = TextUtil.formatMoney(item.value,0)
    }
}