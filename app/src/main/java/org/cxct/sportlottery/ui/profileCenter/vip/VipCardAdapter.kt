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
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.ItemVipCardBinding
import org.cxct.sportlottery.net.user.data.RewardInfo
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable

class VipCardAdapter: BindingAdapter<RewardInfo, ItemVipCardBinding>() {

    private val leftProgress by lazy { context.getDrawable(R.drawable.bg_vip_progress_left)!! }
    private val rightProgress by lazy { context.getDrawable(R.drawable.bg_vip_progress_right)!! }
    var userExp: Long=0

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
        val isCurrentLevel = UserInfoRepository.userInfo.value?.levelCode==item.levelCode
        if (isCurrentLevel) {
            binding.tvCurrent.show()
            (binding.card.layoutParams as MarginLayoutParams).leftMargin = 12.dp
//            vipProgressView.setProgress2((userExp*100/item.upgradeExp).toInt(), 5.dp, leftProgress)
            setProgress(userExp, item.upgradeExp, binding.tvPercent)
        } else {
            binding.tvCurrent.hide()
            (binding.card.layoutParams as MarginLayoutParams).leftMargin = 10.dp
            vipProgressView.setProgress2(0, 24.dp, rightProgress)
            setProgress(0, item.upgradeExp, binding.tvPercent)
        }

        tvDescribe.text = context.getString(R.string.P443)
        card.setBackgroundResource(UserVipType.getVipCard(position))

        tvLevel.text = item.levelName
        tvNextLevel.text = getItemOrNull(position+1)?.levelName?:context.getString(R.string.P439)
    }

    private fun setProgress(progress: Long, max: Long, progressText: TextView) {
        progressText.text = "$progress/"
            .setSpan(ColorSpan(progressText.context.getColor(R.color.color_0D2245)))
            .addSpan("$max", ColorSpan(progressText.context.getColor(R.color.color_6D7693)))

    }
}