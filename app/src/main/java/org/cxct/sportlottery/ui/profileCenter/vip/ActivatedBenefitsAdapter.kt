package org.cxct.sportlottery.ui.profileCenter.vip

import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.common.enums.UserVipType
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemActivatedBenefitsBinding
import org.cxct.sportlottery.net.user.UserRepository
import org.cxct.sportlottery.net.user.data.RewardDetail
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable

class ActivatedBenefitsAdapter(val onItemClick: (RewardDetail)->Unit): BindingAdapter<RewardDetail, ItemActivatedBenefitsBinding>() {

    private val bg by lazy { ShapeDrawable().setSolidColor(Color.WHITE).setRadius(8.dp.toFloat()) }
    private val bg0 by lazy { ShapeDrawable().setSolidColor(context.getColor(R.color.color_025BE8)).setRadius(8.dp.toFloat()) }
    private val bg1 by lazy {
        val dp40 = 40.dp
        ShapeDrawable().setSolidColor(context.getColor(R.color.color_F1F4FC))
            .setWidth(dp40)
            .setHeight(dp40)
            .setRadius(dp40.toFloat())
    }
    private val weeklySurplus by lazy {
        val radius = 8.dp.toFloat()
        ShapeDrawable().setSolidColor(context.getColor(R.color.color_FF8A00))
            .setRadius(radius,radius,0f,radius)
    }

    var setBirthday: Boolean=false
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    var disableStatus: Boolean=false

    override fun onCreateDefViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingVH<ItemActivatedBenefitsBinding> {
        val holder = super.onCreateDefViewHolder(parent, viewType)
        holder.vb.linContent.background = bg
        holder.vb.ivBenefits.background = bg1

        return holder
    }

    override fun onBinding(position: Int, binding: ItemActivatedBenefitsBinding, item: RewardDetail) = binding.run {
        tvWeeklySurplus.gone()
        when(item.rewardType){
            UserVipType.REWARD_TYPE_PROMOTE->{
                ivBenefits.setImageResource(R.drawable.ic_vip_bonus_promote)
                tvBenefitsName.text = context.getString(R.string.P363)
            }
            UserVipType.REWARD_TYPE_WEEKLY->{
                ivBenefits.setImageResource(R.drawable.ic_vip_bonus_weekly)
                tvBenefitsName.text = context.getString(R.string.P364)
                tvWeeklySurplus.visible()
                tvWeeklySurplus.background = weeklySurplus
                tvWeeklySurplus.text = "${context.getString(R.string.P416)}: ${TextUtil.formatMoney(item.remainingWeekRedenpAmount)}"
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
        if(disableStatus){
            tvAction.gone()
            return
        }else{
            tvAction.visible()
        }
        tvAction.setOnClickListener {
            onItemClick.invoke(item)
        }
        //1:未中奖,2:待审核,3:审核不通过,4:审核通过,5:已领取,6:已失效,7:未领取, null:無資格（專屬紅包則視為未申請）
        when(item.status){
            1,7-> {
                tvAction.setActionBtn(true)
                tvAction.text = context.getString(R.string.P373)
            }
            2->{
                tvAction.setActionBtn(false)
                tvAction.text = context.getString(R.string.P375)
            }
            5->{
                tvAction.setActionBtn(false)
                tvAction.text = context.getString(R.string.P374)
            }
            6->{
                tvAction.setActionBtn(false)
                tvAction.text = context.getString(R.string.P377)
            }
            else->{
                if (item.rewardType == UserVipType.REWARD_TYPE_PACKET){
                    tvAction.setActionBtn(true)
                    tvAction.text = context.getString(R.string.P407)
                }else{
                    tvAction.setActionBtn(false)
                    tvAction.text = context.getString(R.string.P376)
                }
            }
        }
        if (item.rewardType == UserVipType.REWARD_TYPE_BIRTHDAY&&setBirthday){
            tvAction.setActionBtn(true)
            tvAction.text = context.getString(R.string.P448)
        }
    }
    private fun TextView.setActionBtn(isBtn: Boolean){
        if (isBtn) {
            isEnabled = true
            background = bg0
            setTextColor(context.getColor(R.color.color_FFFFFF))
        } else {
            isEnabled = false
            background = null
            setTextColor(context.getColor(R.color.color_999999))
        }
    }
}