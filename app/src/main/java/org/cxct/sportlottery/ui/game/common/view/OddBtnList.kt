package org.cxct.sportlottery.ui.game.common.view

import android.content.Context
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

    val oddWidth = 64.dp
    val marging = 3.dp
    val oddBtnParams = LayoutParams(oddWidth, 38.dp).apply {
        bottomMargin = marging
        topMargin = marging
    }


    init {
        setPadding(0, 0, 6.dp, marging)
        orientation = VERTICAL
        oddBtnType = AppCompatTextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
            setTextColor(context.getColor(R.color.color_6C7BA8))
            setAutoSizeTextTypeWithDefaults(TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            setAutoSizeTextTypeUniformWithConfiguration(6, 10, 1, TypedValue.COMPLEX_UNIT_SP)
            maxLines = 3
            gravity = Gravity.CENTER
        }

        val odd_btn_row1_typeParam = LayoutParams(oddWidth, 30.dp)
        addView(oddBtnType, odd_btn_row1_typeParam)

        oddBtnHome = createOddBtn()
        oddBtnAway = createOddBtn()

        addView(oddBtnHome, LayoutParams(oddWidth, 38.dp).apply {
            topMargin = 6.dp
            bottomMargin = marging
        })

        addView(oddBtnAway, oddBtnParams)

    }

    fun getOtherOddsBtn(): OddsButton2 {
        if (oddBtnOther == null) {
            oddBtnOther = createOddBtn()
            addView(oddBtnOther, LayoutParams(66.dp, 126.dp).apply { topMargin = 5.dp })
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
        val oddsBtn = OddsButton2(context)
        return oddsBtn
    }

    fun setOddsInvisiable() {
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

    fun setBtnTypeVisiable(visiable: Boolean) {
        oddBtnType.isVisible = visiable
    }

    fun recyclerAll() {
        oddBtnHome.recyclerAll()
        oddBtnAway.recyclerAll()
        oddBtnDraw?.recyclerAll()
        oddBtnOther?.recyclerAll()
    }

}