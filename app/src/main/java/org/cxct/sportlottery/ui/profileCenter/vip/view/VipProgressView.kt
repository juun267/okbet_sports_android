package org.cxct.sportlottery.ui.profileCenter.vip.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.view.marginLeft
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.getColor
import org.cxct.sportlottery.databinding.ViewVipProgressBinding
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import splitties.systemservices.layoutInflater
import java.math.BigDecimal

class VipProgressView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val binding = ViewVipProgressBinding.inflate(layoutInflater,this)
    private val leftProgress by lazy { context.getDrawable(R.drawable.bg_vip_progress_left)!! }
    private val rightProgress by lazy { context.getDrawable(R.drawable.bg_vip_progress_right)!! }

    init {
        orientation = VERTICAL
    }

    fun setProgress(progress:Double) {

        binding.tvProgress.text = "${getProgressText(progress)}%"
        post {
            val leftMargin = binding.progressBar.marginLeft - binding.ivThumb.width/2 + binding.progressBar.measuredWidth * progress.toInt() / 100
            binding.tvProgress.apply {
                val lp = layoutParams as LayoutParams
                lp.setMargins(leftMargin - 5.dp, 0, 0, 0)
                layoutParams = lp
            }

            binding.ivThumb.apply {
                val lp = binding.ivThumb.layoutParams as LayoutParams
                lp.leftMargin = leftMargin
                binding.ivThumb.layoutParams = lp
            }

            binding.progressBar.setProgress(progress.toInt(),true)
        }
    }

    fun setProgress2(progress:Double) {

        val offset = if (progress < 90) 3.dp else 28.dp
        binding.tvProgress.text = "${getProgressText(progress)}%"
        binding.tvProgress.background = if (progress < 90) leftProgress else rightProgress
        post {
            val leftMargin = binding.progressBar.marginLeft - binding.ivThumb.width/2 + binding.progressBar.measuredWidth * progress.toInt() / 100
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

            binding.progressBar.setProgress(progress.toInt(),true)
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
    fun setYellowStyle()=binding.run{
        setTintColor(R.color.color_FFB828, R.color.color_eed39f)
        setThumbColor(R.color.color_FFB828)
        tvProgress.setTextColor(Color.WHITE)
        tvProgress.gravity = Gravity.CENTER_HORIZONTAL
        progressBar.apply {
            val lp = layoutParams as LayoutParams
            lp.setMargins(30.dp, 0, 30.dp, 0)
            layoutParams = lp
        }
    }
    fun setBlueStyle()=binding.run{
        setTintColor(R.color.color_025BE8, R.color.color_e0ecfc)
        setThumbColor(R.color.color_025BE8)
        tvProgress.gravity = Gravity.CENTER
        tvProgress.textSize = 10f
        tvProgress.setTextColor(Color.WHITE)
        val lp = tvProgress.layoutParams
        lp.width = 42.dp
        lp.height = 30.dp
    }
    //如果是整数就不显示小数点
   private fun getProgressText(progress: Double): String{
       return if (progress.toInt().toDouble()==progress){
           progress.toInt().toString()
       }else{
           progress.toString()
       }
   }
}