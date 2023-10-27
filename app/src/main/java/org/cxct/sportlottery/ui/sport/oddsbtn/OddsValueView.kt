package org.cxct.sportlottery.ui.sport.oddsbtn


import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.RESTART
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
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
import splitties.views.gravityCenter

class OddsValueView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RelativeLayout(context, attrs, defStyleAttr) {


    private val valuesText: TextView


    init {
        valuesText = AppCompatTextView(context).apply {
            typeface = AppFont.inter_bold
            includeFontPadding = false
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
        }

        addView(valuesText,LayoutParams(-2,-2).apply { addRule(CENTER_IN_PARENT,TRUE) })
    }

    private inline fun getColor(color: Int) = ContextCompat.getColor(context, color)

    fun onRise(odds: String) {
        valuesText.text = odds
        valuesText.setTextColor(getColor(R.color.color_1CD219))
    }

    fun onFall(odds: String) {
        valuesText.text = odds
        valuesText.setTextColor(getColor(R.color.color_FF2E00))
    }

    fun setOdds(odds: String) {
        disableBuoy()
        valuesText.text = odds
    }

    private fun disableBuoy() {
        valuesText.setTextColor(ContextCompat.getColorStateList(context, R.color.selector_button_odd_top_text))
    }

}