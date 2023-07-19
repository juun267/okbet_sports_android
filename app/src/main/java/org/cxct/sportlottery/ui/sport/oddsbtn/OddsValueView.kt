package org.cxct.sportlottery.ui.sport.oddsbtn


import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.animation.Animation
import android.view.animation.Animation.RESTART
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp

class OddsValueView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {


    private val valuesText: TextView
    private val buoyIcon: ImageView
    private val upAnim by lazy { TranslateAnimation(0f, 0f, 0f, -4.dp.toFloat()) }
    private val downAnim by lazy { TranslateAnimation(0f, 0f, 0f, 4.dp.toFloat()) }

    init {
        gravity = Gravity.CENTER
        valuesText = AppCompatTextView(context).apply {
            typeface = AppFont.inter_bold
            includeFontPadding = false
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
        }

        addView(valuesText)

        buoyIcon = ImageView(context)
        16.dp.let { addView(buoyIcon, LayoutParams(it, it)) }
    }

    private inline fun getColor(color: Int) = ContextCompat.getColor(context, color)

    fun onRise(odds: String) {
        valuesText.text = odds
        valuesText.setTextColor(getColor(R.color.color_1CD219))
        playAnim(R.drawable.icon_odds_up, upAnim)
    }

    fun onFall(odds: String) {
        valuesText.text = odds
        valuesText.setTextColor(getColor(R.color.color_FF2E00))
        playAnim(R.drawable.icon_odds_down, downAnim)
    }

    fun setOdds(odds: String) {
        disableBuoy()
        valuesText.text = odds
    }

    private fun disableBuoy() {
        buoyIcon.gone()
        buoyIcon.clearAnimation()
        buoyIcon.tag = null
        valuesText.setTextColor(getColor(R.color.color_000000))
    }

    private fun playAnim(icon: Int, animation: Animation) {
        if (animation == buoyIcon.tag) {
            return
        }
        animation.interpolator = DecelerateInterpolator()
        animation.repeatCount = 3
        animation.repeatMode = RESTART
        animation.duration = 500
        animation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation) { }
            override fun onAnimationRepeat(animation: Animation) { }
            override fun onAnimationEnd(animation: Animation) {
                disableBuoy()
            }
        })

        buoyIcon.tag = animation
        buoyIcon.visible()
        buoyIcon.setImageResource(icon)
        buoyIcon.startAnimation(animation)
    }

}