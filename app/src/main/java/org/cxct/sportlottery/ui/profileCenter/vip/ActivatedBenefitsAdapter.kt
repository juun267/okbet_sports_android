package org.cxct.sportlottery.ui.profileCenter.vip

import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.enums.UserVipType
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemActivatedBenefitsBinding
import org.cxct.sportlottery.net.user.data.RewardDetail
import org.cxct.sportlottery.net.user.data.RewardInfo
import org.cxct.sportlottery.net.user.data.UserVip
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable

class ActivatedBenefitsAdapter(val onItemClick: (RewardInfo, RewardDetail)->Unit): BindingAdapter<RewardDetail, ItemActivatedBenefitsBinding>() {

    private val bg0 by lazy { ShapeDrawable().setSolidColor(context.getColor(R.color.color_025BE8)).setRadius(8.dp.toFloat()) }
    private val weeklySurplus by lazy {
        val radius = 8.dp.toFloat()
        ShapeDrawable().setSolidColor(context.getColor(R.color.color_FF8A00))
            .setRadius(radius,radius,0f,radius)
    }

    lateinit var userVip: UserVip
    lateinit var rewardInfo: RewardInfo

    override fun onBinding(position: Int, binding: ItemActivatedBenefitsBinding, item: RewardDetail) = binding.run {
        tvWeeklySurplus.gone()
        if (item.otherType==1){
            ivBenefits.setImageResource(R.drawable.ic_vip_bonus_support)
            tvBenefitsName.text = context.getString(R.string.P402)
            tvAmount.text = "24x7"
            tvAction.gone()
            return@run
        }
        if (item.otherType==2){
            ivBenefits.setImageResource(R.drawable.ic_vip_bonus_disbursement)
            tvBenefitsName.text = context.getString(R.string.P404)
            tvAmount.text = context.getString(R.string.P411)
            tvAction.gone()
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
                if (item.remainingWeekRedenpAmount==null){
                    tvWeeklySurplus.gone()
                }else{
                    tvWeeklySurplus.visible()
                    tvWeeklySurplus.background = weeklySurplus
                    tvWeeklySurplus.text = if (item.remainingWeekRedenpAmount==0.0)
                        context.getString(R.string.P415)
                    else
                        "${context.getString(R.string.P416)}: ${TextUtil.formatMoney(item.remainingWeekRedenpAmount,0)}"
                }
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
        val currentLevelPos = userVip.rewardInfo.indexOfFirst { it.levelCode == rewardInfo.levelCode }
        val userLevelPos = userVip.rewardInfo.indexOfFirst { it.levelCode == userVip.levelCode }
        val hideStatus = when(item.rewardType){
            UserVipType.REWARD_TYPE_PACKET-> currentLevelPos>userLevelPos && item.status==null
            UserVipType.REWARD_TYPE_BIRTHDAY,UserVipType.REWARD_TYPE_WEEKLY-> currentLevelPos!=userLevelPos &&item.status ==null
            else-> item.status==null
        }

        if(hideStatus){
            tvAction.gone()
            return
        }else{
            tvAction.visible()
        }
        tvAction.setOnClickListener {
            onItemClick.invoke(rewardInfo,item)
        }
        //1:未中奖,2:待审核,3:审核不通过,4:审核通过,5:已领取,6:已失效,7:未领取, null:無資格（專屬紅包則視為未申請）
        when(item.status){
            1,7-> {
                tvAction.setActionBtn(true)
                tvAction.text = context.getString(R.string.P407)
            }
            2->{
                tvAction.setActionBtn(false)
                tvAction.text = context.getString(R.string.P375)
            }
            4,5->{
                tvAction.setActionBtn(false)
                tvAction.text = context.getString(R.string.P374)
            }
            3,6->{
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
        if (item.rewardType == UserVipType.REWARD_TYPE_BIRTHDAY&&userVip.birthday.isNullOrEmpty()){
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