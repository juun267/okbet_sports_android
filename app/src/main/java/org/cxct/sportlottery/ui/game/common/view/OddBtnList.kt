package org.cxct.sportlottery.ui.game.common.view

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.extentions.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.DisplayUtil.pxToDp
import timber.log.Timber

class OddBtnList @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    val oddBtnType: TextView
    var oddBtnHome: OddsButton2
    val oddBtnAway: OddsButton2
    private var oddBtnDraw: OddsButton2? = null
    private var oddBtnOther: OddsButton2? = null

    private val oddWidth = context.dp2px(66)
    private val margin = context.dp2px(4.5f)
    private val oddBtnParams = LayoutParams(oddWidth, 44.dp).apply {
        topMargin = margin
    }


    init {
        setPadding(0, 0, 6.dp, 0)
        orientation = VERTICAL
        oddBtnType = AppCompatTextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
            setTextColor(context.getColor(R.color.color_00181E))
            setAutoSizeTextTypeWithDefaults(TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            setAutoSizeTextTypeUniformWithConfiguration(6, 10, 1, TypedValue.COMPLEX_UNIT_SP)
            maxLines = 3
            gravity = Gravity.CENTER
        }
//        Timber.d("OddBtnList：oddWidth: $oddWidth")
//        Timber.d("OddBtnList：10.dp: ${10.dp}")
//        Timber.d("OddBtnList：context.dp2px(10): ${context.dp2px(10)}")
        Timber.d("OddBtnList：Resources.getSystem().displayMetrics.density: ${Resources.getSystem().displayMetrics.density}")
        Timber.d("OddBtnList：resources.displayMetrics.density: ${resources.displayMetrics.density}")
        Timber.d("OddBtnList：resources.displayMetrics.widthPixels: ${resources.displayMetrics.widthPixels}")
        addView(oddBtnType, LayoutParams(oddWidth, 29.dp))

        oddBtnHome = createOddBtn()
        addView(oddBtnHome, LayoutParams(oddWidth, 43.dp).apply {
            topMargin = context.dp2px(4.5f)
        })


        oddBtnAway = createOddBtn()
        addView(oddBtnAway, LayoutParams(oddWidth, 43.dp).apply {
            topMargin = context.dp2px(4.5f)
        })

    }

    fun getOtherOddsBtn(): OddsButton2 {
        if (oddBtnOther == null) {
            oddBtnOther = createOddBtn()
            addView(oddBtnOther, LayoutParams(66.dp, 142.dp).apply { topMargin = 5.dp })
        }
        return oddBtnOther!!
    }

    fun getDrawOddsBtn(): OddsButton2 {
        if (oddBtnDraw == null) {
            oddBtnDraw = createOddBtn()
            addView(oddBtnDraw, oddBtnParams)
        }
        return oddBtnDraw!!
    }

    fun disableDrawBtn() {
        oddBtnDraw?.inVisible()
    }

    private fun createOddBtn(): OddsButton2 {
        return OddsButton2(context)
    }

    fun setOddsInvisible() {
        setViewInvisible(oddBtnType, oddBtnHome, oddBtnAway)
        oddBtnDraw?.inVisible()
        oddBtnOther?.gone()
    }

    fun enableOtherOddsBtn() {
        setViewGone(oddBtnType, oddBtnHome, oddBtnAway)
        oddBtnDraw?.gone()
        oddBtnOther?.visible()
    }

    fun enableAllOddsBtn(includeDrawBtn: Boolean) {
        setViewVisible(oddBtnType, oddBtnHome, oddBtnAway)
        oddBtnOther?.gone()
        if (includeDrawBtn) {
            getDrawOddsBtn().visible()
        }

    }

    fun setOddsDeactivated() {
        oddBtnType.text = "-"
        val deactivated = BetStatus.DEACTIVATED.code
        oddBtnHome.betStatus = deactivated
        oddBtnAway.betStatus = deactivated
        oddBtnDraw?.betStatus = deactivated
        oddBtnOther?.betStatus = deactivated
    }

    fun setBtnTypeVisible(visible: Boolean) {
        oddBtnType.isVisible = visible
    }

    fun recyclerAll() {
        oddBtnHome.recyclerAll()
        oddBtnAway.recyclerAll()
        oddBtnDraw?.recyclerAll()
        oddBtnOther?.recyclerAll()
    }

}