package org.cxct.sportlottery.ui.profileCenter.vip

import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import com.drake.spannable.addSpan
import com.drake.spannable.setSpan
import com.drake.spannable.span.ColorSpan
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.common.enums.UserVipType
import org.cxct.sportlottery.common.extentions.hide
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.databinding.ItemVipCardBinding
import org.cxct.sportlottery.net.user.data.RewardInfo
import org.cxct.sportlottery.net.user.data.UserVip
import org.cxct.sportlottery.util.DisplayUtil.dp

class VipCardAdapter: BindingAdapter<RewardInfo, ItemVipCardBinding>() {


    lateinit var userVip: UserVip

    override fun onCreateDefViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingVH<ItemVipCardBinding> {
        val holder = super.onCreateDefViewHolder(parent, viewType)
        holder.vb.vipProgressView.setBlueStyle()
        return holder
    }

    override fun onBinding(position: Int, binding: ItemVipCardBinding, item: RewardInfo) = binding.run {
        val isCurrentLevel = userVip.levelCode==item.levelCode
        if (isCurrentLevel) {
            binding.tvCurrent.show()
            (binding.card.layoutParams as MarginLayoutParams).leftMargin = 12.dp
            vipProgressView.setProgress2(userVip.getExpPercent())
            setProgress(userVip.exp.toString(), item.upgradeExp.toString(), binding.tvPercent)
        }else{
            binding.tvCurrent.hide()
            (binding.card.layoutParams as MarginLayoutParams).leftMargin = 10.dp
            val currentLevelIndex = userVip.rewardInfo.indexOfFirst { it.levelCode == item.levelCode }
            val userLevelIndex = userVip.rewardInfo.indexOfFirst { it.levelCode == userVip.levelCode }
            if (userLevelIndex>currentLevelIndex){
                vipProgressView.setProgress2(100.0)
                setProgress(item.upgradeExp.toString(), item.upgradeExp.toString(), binding.tvPercent)
            }else{
                vipProgressView.setProgress2(0.0)
                setProgress("0", item.upgradeExp.toString(), binding.tvPercent)
            }
        }
        if (position == (userVip.rewardInfo.size-1)){
            setProgress(if (isCurrentLevel) userVip.exp.toString() else "0", context.getString(R.string.max), binding.tvPercent)
        }
        tvDescribe.text = context.getString(R.string.P443)
        card.setBackgroundResource(UserVipType.getVipCard(position))

        tvLevel.text = item.levelName
        tvNextLevel.text = getItemOrNull(position+1)?.levelName?:context.getString(R.string.max)
    }

    private fun setProgress(progress: String, max: String, progressText: TextView) {
        val total = max.toIntS(-1)
        if (total > 0) {
            progressText.text = "$progress/"
                .setSpan(ColorSpan(progressText.context.getColor(R.color.color_0D2245)))
                .addSpan("$total pts", ColorSpan(progressText.context.getColor(R.color.color_6D7693)))
        } else {
            progressText.text = "$progress/"
                .setSpan(ColorSpan(progressText.context.getColor(R.color.color_0D2245)))
                .addSpan("$max", ColorSpan(progressText.context.getColor(R.color.color_6D7693)))
        }
    }

}