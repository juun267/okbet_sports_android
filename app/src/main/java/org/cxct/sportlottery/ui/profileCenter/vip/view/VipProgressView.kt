package org.cxct.sportlottery.ui.profileCenter.vip.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import org.cxct.sportlottery.common.extentions.getColor
import org.cxct.sportlottery.databinding.ViewVipProgressBinding
import splitties.systemservices.layoutInflater

class VipProgressView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val binding = ViewVipProgressBinding.inflate(layoutInflater,this,true)
    init {
        orientation = VERTICAL
    }
    open fun setProgress(progress:Int){
        binding.tvProgress.apply {
            val leftMargin = binding.progressBar.marginLeft+binding.progressBar.width*progress/100-width/2
            val lp = layoutParams as LayoutParams
            lp.setMargins(leftMargin, 0, 0, 0)
            layoutParams = lp
        }
        binding.tvProgress.text = "$progress%"
        binding.ivThumb.apply {
            val leftMargin =  binding.progressBar.marginLeft+binding.progressBar.width*progress/100-width/2
            val lp = layoutParams as LayoutParams
            lp.setMargins(leftMargin, marginTop, 0, 0)
            layoutParams = lp
        }
        binding.progressBar.setProgress(progress,true)
    }
    open fun setTintColor(@ColorRes colorProgress: Int,@ColorRes backgroundProgress: Int){
        binding.progressBar.progressTintList = ColorStateList.valueOf(getColor(colorProgress))
        binding.progressBar.progressBackgroundTintList = ColorStateList.valueOf(getColor(backgroundProgress))
    }

}