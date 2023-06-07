package org.cxct.sportlottery.ui.sport.oddsbtn

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import splitties.views.dsl.core.add
import splitties.views.dsl.core.button

class PlayCateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private val homeText: TextView
    private val awayText: TextView
    private val drawText: TextView
    val oddBtnType: TextView
    var oddBtnHome: OddsButton2
    val oddBtnAway: OddsButton2
    private var oddBtnDraw: OddsButton2? = null
    private var oddBtnOther: OddsButton2? = null

    private val oddWidth = 66.dp
    private val oddHeight = 44.dp
    private val margin = 5.dp
    private val typeNameHeight = 30.dp
    private val oddBtnParams = LayoutParams(oddWidth, oddHeight).apply {
        topMargin = margin
    }

    init {
        12.dp.let { setPadding(it, 0, it, 0) }

        val nameLayout = LinearLayout(context)
        val param = LayoutParams(0, 16.dp, 1f)
        homeText = createNameTextView()
        nameLayout.add(homeText, param)
        drawText = createNameTextView()
        nameLayout.add(drawText, param)
        awayText = createNameTextView()
        nameLayout.add(awayText, param)

        addView(nameLayout)

        oddBtnType = AppCompatTextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
            setTextColor(context.getColor(R.color.color_00181E))
            TextViewCompat.setAutoSizeTextTypeWithDefaults(this, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(this, 6, 10, 1, TypedValue.COMPLEX_UNIT_SP)
            maxLines = 3
            gravity = Gravity.CENTER
        }

        addView(oddBtnType, LayoutParams(oddWidth, typeNameHeight))

        oddBtnHome = createOddBtn()
        oddBtnAway = createOddBtn()

        addView(oddBtnHome, oddBtnParams)
        addView(oddBtnAway, oddBtnParams)
        layoutParams = ViewGroup.LayoutParams(200.dp, -1)
    }


    private fun createNameTextView(): TextView {
        val text = AppCompatTextView(context)
        text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
        text.setTextColor(getColor(R.color.color_6D7693))
        return  text
    }

    fun getOtherOddsBtn(): OddsButton2 {
        if (oddBtnOther == null) {
            oddBtnOther = createOddBtn()
            addView(oddBtnOther,
                LayoutParams(oddWidth, oddHeight * 3 + margin * 2).apply { topMargin = margin })
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