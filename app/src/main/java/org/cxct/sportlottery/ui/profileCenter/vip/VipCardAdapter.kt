package org.cxct.sportlottery.ui.profileCenter.vip

import android.graphics.Color
import android.view.Gravity
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
import org.cxct.sportlottery.common.extentions.setViewGone
import org.cxct.sportlottery.common.extentions.setViewVisible
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.ItemVipCardBinding
import org.cxct.sportlottery.net.user.data.RewardInfo
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable

class VipCardAdapter: BindingAdapter<RewardInfo, ItemVipCardBinding>() {


    private lateinit var levelCode: String
    private var exp: Int = 0
    private var upgradeExp: Int = 0

    fun setUpdate(currentLevelCode: String, exp: Int, upgradeExp: Int, rewardInfoList: List<RewardInfo>) {
        levelCode = currentLevelCode
        this.exp = exp
        this.upgradeExp = upgradeExp
        setNewInstance(rewardInfoList.toMutableList())
    }

    override fun onCreateDefViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingVH<ItemVipCardBinding> {
        val holder = super.onCreateDefViewHolder(parent, viewType)
        holder.vb.vipProgressView.setTintColor(R.color.color_025BE8, R.color.color_e0ecfc)
        holder.vb.vipProgressView.setThumbColor(R.color.color_025BE8)
        val progressText = holder.vb.vipProgressView.getProgressTextView()
        progressText.gravity = Gravity.CENTER
        progressText.textSize = 10f
        progressText.setTextColor(Color.WHITE)
        val lp = progressText.layoutParams
        lp.width = 38.dp
        lp.height = 30.dp
        holder.vb.tvPercent.background = ShapeDrawable()
            .setRadius(30.dp.toFloat())
            .setSolidColor(parent.context.getColor(R.color.color_f3f3f3))
        return holder
    }

    override fun onBinding(position: Int, binding: ItemVipCardBinding, item: RewardInfo) = binding.run {

        val next = getItemOrNull(position + 1)
        tvLevel.text = item.levelName
        tvDescribe.text = "Ang halaga ng paglago ay binabayaran sa 15:50 araw-araw Ang halaga ng paglago ay binabayaran sa 15:50 araw-araw"
        card.setBackgroundResource(UserVipType.getVipCard(position))

        if (levelCode != item.levelCode || next == null) {
            setViewGone(tvCurrent, vipProgressView, tvPercent, tvNextLevel)
            (binding.card.layoutParams as MarginLayoutParams).leftMargin = 10.dp
            return@run
        }

        setViewVisible(tvCurrent, vipProgressView, tvPercent, tvNextLevel)
        (binding.card.layoutParams as MarginLayoutParams).leftMargin = 12.dp
        tvNextLevel.text = next.levelName
        setProgress(exp, upgradeExp, tvPercent)
        val upgradeProgress = if (exp > upgradeExp || exp < 0 || upgradeExp <= 0) 100 else (exp.toFloat() / upgradeExp * 100).toInt()
        if (upgradeProgress < 90) {
            vipProgressView.setProgress2(upgradeProgress, 5.dp, context.getDrawable(R.drawable.bg_vip_progress_left)!!)
        } else {
            vipProgressView.setProgress2(upgradeProgress, 5.dp, context.getDrawable(R.drawable.bg_vip_progress_right)!!)
        }

//        tvLevel.text = "VIP $item"
//        tvNextLevel.text = "VIP ${item + 1}"

    }

    private fun setProgress(progress: Int, max: Int, progressText: TextView) {
        progressText.text = "$progress/"
            .setSpan(ColorSpan(progressText.context.getColor(R.color.color_0D2245)))
            .addSpan("$max", ColorSpan(progressText.context.getColor(R.color.color_6D7693)))

    }
}