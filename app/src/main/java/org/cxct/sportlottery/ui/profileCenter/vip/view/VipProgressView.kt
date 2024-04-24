package org.cxct.sportlottery.ui.profileCenter.vip.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.view.marginLeft
import org.cxct.sportlottery.common.extentions.getColor
import org.cxct.sportlottery.databinding.ViewVipProgressBinding
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import splitties.systemservices.layoutInflater

class VipProgressView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val binding = ViewVipProgressBinding.inflate(layoutInflater,this)
    init {
        orientation = VERTICAL
    }

    fun setProgress(progress:Int) {

        binding.tvProgress.text = "$progress%"
        post {
            val leftMargin = binding.progressBar.marginLeft + binding.progressBar.measuredWidth * progress / 100
            binding.tvProgress.apply {
                val lp = layoutParams as LayoutParams
                lp.setMargins(leftMargin - width / 2, 0, 0, 0)
                layoutParams = lp
            }

            binding.ivThumb.apply {
                val lp = binding.ivThumb.layoutParams as LayoutParams
                lp.leftMargin = leftMargin
                binding.ivThumb.layoutParams = lp
            }

            binding.progressBar.setProgress(progress,true)
        }
    }

    fun setProgress2(progress:Int, offset: Int, bg: Drawable) {

        binding.tvProgress.text = "$progress%"
        binding.tvProgress.background = bg
        post {
            val leftMargin = binding.progressBar.marginLeft + binding.progressBar.measuredWidth * progress / 100
            binding.tvProgress.apply {
                val lp = layoutParams as LayoutParams
                lp.setMargins(leftMargin - offset, 0, 0, 0)
                layoutParams = lp
            }

            binding.ivThumb.apply {
                val lp = binding.ivThumb.layoutParams as LayoutParams
                lp.leftMargin = leftMargin
                binding.ivThumb.layoutParams = lp
            }

            binding.progressBar.setProgress(progress,true)
        }
    }

    open fun setTintColor(@ColorRes colorProgress: Int,@ColorRes colorBackground: Int){
        binding.progressBar.progressTintList = ColorStateList.valueOf(getColor(colorProgress))
        binding.progressBar.progressBackgroundTintList = ColorStateList.valueOf(getColor(colorBackground))
    }

    fun setThumbColor(@ColorRes color: Int) {
        val wh = 10.dp
        binding.ivThumb.background = ShapeDrawable()
            .setSolidColor(getColor(color))
            .setWidth(wh)
            .setHeight(wh)
            .setRadius(wh.toFloat())
    }

    fun getProgressTextView() = binding.tvProgress

}