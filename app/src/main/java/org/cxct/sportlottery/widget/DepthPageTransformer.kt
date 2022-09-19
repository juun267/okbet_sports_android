package org.cxct.sportlottery.widget

import android.view.View
import androidx.viewpager2.widget.ViewPager2


class DepthPageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        page.apply {
            when {
                position < -1 -> {
                    alpha = 1f - MIN_SCALE
                    scaleX = MIN_SCALE
                    scaleY = MIN_SCALE
                }
                position < 0 -> {
                    val absPos = Math.abs(position)
                    val scale = if (absPos > 1) 0F else 1f - absPos
                    page.scaleX = MIN_SCALE + (1f - MIN_SCALE) * scale
                    page.scaleY = MIN_SCALE + (1f - MIN_SCALE) * scale
                    page.alpha = MIN_SCALE + (1f - MIN_SCALE) * scale
                }
                position <= 1 -> {
                    scaleX = 1f
                    scaleY = 1f
                    page.alpha = 1f
                }
                else -> {
                    page.scaleX = 0f
                    page.scaleY = 0f
                    page.alpha = 0f
                }
            }
        }


    }

    companion object {
        private const val MIN_SCALE = 0.5f
    }
}