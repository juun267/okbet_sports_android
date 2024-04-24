package org.cxct.sportlottery.ui.profileCenter.vip

import android.annotation.SuppressLint
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
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable

class VipCardAdapter: BindingAdapter<Int, ItemVipCardBinding>() {

    private val leftProgress by lazy { context.getDrawable(R.drawable.bg_vip_progress_left)!! }
    private val rightProgress by lazy { context.getDrawable(R.drawable.bg_vip_progress_right)!! }

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

    override fun onBinding(position: Int, binding: ItemVipCardBinding, item: Int) = binding.run {
        if (position % 3 == 0) {
            binding.tvCurrent.show()
            vipProgressView.setProgress2(5, 5.dp, leftProgress)
            (binding.card.layoutParams as MarginLayoutParams).leftMargin = 12.dp
        } else {
            binding.tvCurrent.hide()
            (binding.card.layoutParams as MarginLayoutParams).leftMargin = 10.dp
            vipProgressView.setProgress2(91, 24.dp, rightProgress)
        }

        tvDescribe.text = "Ang halaga ng paglago ay binabayaran sa 15:50 araw-araw Ang halaga ng paglago ay binabayaran sa 15:50 araw-araw"
        card.setBackgroundResource(UserVipType.getVipCard(position))

        tvLevel.text = "VIP $item"
        tvNextLevel.text = "VIP ${item + 1}"
        setProgress(50, 900, binding.tvPercent)
    }

    private fun setProgress(progress: Int, max: Int, progressText: TextView) {
        progressText.text = "$progress/"
            .setSpan(ColorSpan(progressText.context.getColor(R.color.color_0D2245)))
            .addSpan("$max", ColorSpan(progressText.context.getColor(R.color.color_6D7693)))

    }
}