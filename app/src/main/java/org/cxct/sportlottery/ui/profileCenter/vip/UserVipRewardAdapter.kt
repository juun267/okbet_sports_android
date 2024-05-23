package org.cxct.sportlottery.ui.profileCenter.vip

import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.enums.UserVipType
import org.cxct.sportlottery.databinding.ItemUserVipRewardBinding
import org.cxct.sportlottery.net.user.data.RewardDetail

class UserVipRewardAdapter: BindingAdapter<RewardDetail, ItemUserVipRewardBinding>() {
    override fun onBinding(position: Int, binding: ItemUserVipRewardBinding, item: RewardDetail)=binding.run {
        when(item.rewardType){
            UserVipType.REWARD_TYPE_PROMOTE->{
                ivBonus.setImageResource(R.drawable.ic_vip_bonus_promote)
                tvName.text = context.getString(R.string.P363)
            }
            UserVipType.REWARD_TYPE_BIRTHDAY->{
                ivBonus.setImageResource(R.drawable.ic_vip_bonus_birthday)
                tvName.text = context.getString(R.string.P365)
            }
            UserVipType.REWARD_TYPE_WEEKLY->{
                ivBonus.setImageResource(R.drawable.ic_vip_bonus_weekly)
                tvName.text = context.getString(R.string.P364)
            }
            UserVipType.REWARD_TYPE_PACKET->{
                ivBonus.setImageResource(R.drawable.ic_vip_bonus_packet)
                tvName.text = context.getString(R.string.P366)
            }
        }
        ivLock.isVisible = !item.enable
    }
}