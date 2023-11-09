package org.cxct.sportlottery.ui.sport.oddsbtn

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp

class PlayCateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    val esportTheme: Boolean = false
) : LinearLayout(context, attrs, defStyleAttr) {

    open val nameLayout: LinearLayout
    private val homeText: TextView
    private val awayText: TextView
    private val drawText: TextView
    var oddBtnHome: OddsButton2
    val oddBtnAway: OddsButton2
    private var oddBtnDraw: OddsButton2? = null
    private var oddBtnOther: OddsButton2? = null

    private val oddHeight = 48.dp
    private val oddLayout = LinearLayout(context)
    private val oddMargins = 4.dp

    init {
        orientation = VERTICAL
        layoutParams = ViewGroup.LayoutParams(200.dp, -1)
        12.dp.let { setPadding(it, 0, it, 0) }

        nameLayout = LinearLayout(context)
        val param = LayoutParams(0, -1, 1f)
        homeText = createNameTextView()
        nameLayout.addView(homeText, param)
        drawText = createNameTextView()
        nameLayout.addView(drawText, param)
        awayText = createNameTextView()
        nameLayout.addView(awayText, param)
        addView(nameLayout, LayoutParams(-1, 48.dp))

        oddBtnHome = createOddBtn()
        oddBtnAway = createOddBtn()
        oddLayout.gravity = Gravity.CENTER_VERTICAL
        oddLayout.addView(oddBtnHome, LayoutParams(-1, oddHeight, 1f).apply { rightMargin = oddMargins })
        oddLayout.addView(oddBtnAway, LayoutParams(-1, oddHeight, 1f).apply { leftMargin = oddMargins })
        addView(oddLayout,LayoutParams(-1, -1))
    }

    private fun createNameTextView(): TextView {
        val text = AppCompatTextView(context)
        text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
        text.setTextColor(getColor(R.color.color_6D7693))
        text.typeface = AppFont.helvetica
        text.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
        text.setLineSpacing(1f, 0.9f)
        return text
    }

    fun getOtherOddsBtn(): OddsButton2 {
        if (oddBtnOther == null) {
            oddBtnOther = createOddBtn()
            oddLayout.addView(oddBtnOther, LayoutParams(-1, oddHeight))
        }
        return oddBtnOther!!
    }

    fun getDrawOddsBtn(): OddsButton2 {
        if (oddBtnDraw == null) {
            oddBtnDraw = createOddBtn()
            val params = LayoutParams(-1, oddHeight, 1f)
            params.leftMargin = 0.dp
            params.rightMargin = 0.dp
            oddLayout.addView(oddBtnDraw, 1, params)
        }
        return oddBtnDraw!!
    }

    fun disableDrawBtn() {
        oddBtnDraw?.gone()
    }

    private fun createOddBtn(): OddsButton2 {
        return OddsButton2(context,esportTheme = esportTheme)
    }

    fun setOddsInvisible() {
        setViewInvisible(homeText, awayText, drawText, oddBtnHome, oddBtnAway)
        oddBtnDraw?.gone()
        oddBtnOther?.gone()
    }

    fun enableOtherOddsBtn() {
        setViewGone(homeText, awayText, drawText, oddBtnHome, oddBtnAway)
        oddBtnDraw?.gone()
        oddBtnOther?.visible()
    }

    fun enableAllOddsBtn(includeDrawBtn: Boolean) {
        setViewVisible(homeText, awayText, drawText, oddBtnHome, oddBtnAway)
        oddBtnOther?.gone()
        if (includeDrawBtn) {
            getDrawOddsBtn().visible()
        }
    }

    fun setOddsDeactivated() {
        homeText.text = ""
        awayText.text = ""
        drawText.text = "-"
        val deactivated = BetStatus.DEACTIVATED.code
        oddBtnHome.betStatus = deactivated
        oddBtnAway.betStatus = deactivated
        oddBtnDraw?.betStatus = deactivated
        oddBtnOther?.betStatus = deactivated
    }

    fun setBtnTypeVisible(visible: Boolean) {
        drawText.isVisible = visible
    }

    fun recyclerAll() {
        oddBtnHome.recyclerAll()
        oddBtnAway.recyclerAll()
        oddBtnDraw?.recyclerAll()
        oddBtnOther?.recyclerAll()
    }

    fun setPlayCateName(home: String, away: String, draw: CharSequence) {
        bindText(homeText, home)
        bindText(awayText, away)
        bindText(drawText, draw)
    }

    private fun bindText(textView: TextView, name: CharSequence) {
        textView.text = name
        textView.isVisible = name.isNotEmpty()
    }

}