package org.cxct.sportlottery.ui.sport.oddsbtn

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.flashAnimation
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.isCombination
import org.cxct.sportlottery.util.BetPlayCateFunction.isNOGALType
import org.cxct.sportlottery.util.BetPlayCateFunction.isW_METHOD_1ST
import org.cxct.sportlottery.util.DisplayUtil.dp

class OddsButton2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {

        private val oddsNameCaches = mutableListOf<TextView>()
        private val oddsValueCaches = mutableListOf<OddsValueView>()
        private val oddsLockedCaches = mutableListOf<ImageView>()
        private val oddsUnknownCaches = mutableListOf<TextView>()
        private val params1 = LayoutParams(-2, -2).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }
        private val params2 = LayoutParams(-2, -2)


        fun clearOddsViewCaches() {
            oddsNameCaches.clear()
            oddsValueCaches.clear()
            oddsLockedCaches.clear()
            oddsUnknownCaches.clear()
        }
    }

    private var oddsName: TextView? = null
    private var oddsValue: OddsValueView? = null
    private var oddsLocked: ImageView? = null
    private var oddsUnknown: TextView? = null

    private val language: String by lazy { LanguageManager.getSelectLanguage(context).key }
    private var mOdd: Odd? = null
    private var mOddsType: OddsType = OddsType.EU

    private var nameText: String? = null
    private var spreadText: String? = null
    private var oddsValueText: String = ""

    var betStatus: Int? = null
        set(value) {
            value?.let {
                setBetStatus(it)
            }
            field = value
        }

    var oddStatus: Int? = null
        set(value) {
            if (value != null) {
                setupOddState(value)
                field = value
            }
        }

    init {
        foreground = ContextCompat.getDrawable(context, R.drawable.fg_ripple)
        setBackgroundResource(R.drawable.selector_button_radius_4_odds)
        orientation = VERTICAL
        gravity = Gravity.CENTER
        betStatus = BetStatus.DEACTIVATED.code
    }

    private fun setBetStatus(status: Int) {

        isEnabled = status == BetStatus.ACTIVATED.code

        if (status == BetStatus.LOCKED.code) {
            getOddsLockedView()
            return
        }

        if (status == BetStatus.DEACTIVATED.code) {
            getOddsUnknownView()
            return
        }

        recyclerLockedView()
        recyclerUnknownView()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        // 状态发生改变时处于选中状态,取消选中状态
        if (!enabled && isSelected) {
            isSelected = false
        }
    }

    fun recyclerAll() {
        recyclerLockedView()
        recyclerUnknownView()
        recyclerNameView()
        recyclerValuesView()
    }

    private fun setupOddState(oddState: Int) {
        if (!isEnabled) return

        val oddView = getOddsValueView()


        when (oddState) {
            OddState.LARGER.state -> {
                oddView.onRise(oddsValueText)
            }
            OddState.SMALLER.state -> {
                oddView.onFall(oddsValueText)
            }
            else -> {
                oddView.setOdds(oddsValueText)
            }
        }

    }

    private fun recyclerNameView() {
        oddsName?.let {
            removeView(it)
            oddsNameCaches.add(it)
            oddsName = null
        }
    }

    private fun recyclerValuesView() {
        oddsValue?.let {
            removeView(it)
            oddsValueCaches.add(it)
            oddsValue = null
        }
    }

    private fun recyclerLockedView() {
        oddsLocked?.let {
            removeView(it)
            oddsLockedCaches.add(it)
            oddsLocked = null
        }
    }

    private fun recyclerUnknownView() {
        oddsUnknown?.let {
            removeView(it)
            oddsUnknownCaches.add(it)
            oddsUnknown = null
        }
    }

    private fun getOddsNameView(): TextView {
        recyclerLockedView()
        recyclerUnknownView()
        if (oddsName != null) {
            return oddsName!!
        }
        if (oddsNameCaches.size > 0 ) {
            oddsName = addOddView(oddsNameCaches.removeAt(0), params1, 0)
            return oddsName!!
        }

        oddsName = AppCompatTextView(context).apply {
            setTextColor(ContextCompat.getColorStateList(context, R.color.selector_button_odd_top_text))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f)
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            getPaint().apply {
                setStyle(Paint.Style.FILL_AND_STROKE)
                setStrokeWidth(0.37f)
            }
        }
        addOddView(oddsName!!, params1, 0)
        return oddsName!!
    }

    private fun getOddsValueView(): OddsValueView {
        recyclerLockedView()
        recyclerUnknownView()
        if (oddsValue != null) {
            return oddsValue!!
        }
        if (oddsValueCaches.size > 0 ) {
            oddsValue = addOddView(oddsValueCaches.removeAt(0), params1)
            return oddsValue!!
        }
        oddsValue = OddsValueView(context)
        addOddView(oddsValue!!, params1)
        return oddsValue!!
    }

    private fun getOddsLockedView(): ImageView {
        recyclerNameView()
        recyclerValuesView()
        recyclerUnknownView()
        if (oddsLocked != null) {
            return oddsLocked!!
        }
        if (oddsLockedCaches.size > 0 ) {
            oddsLocked = addOddView(oddsLockedCaches.removeAt(0), params2)
            return oddsLocked!!
        }
        oddsLocked = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_lock)
            setPadding(14.dp)
        }
        addOddView(oddsLocked!!, params2)
        return oddsLocked!!
    }

    private fun getOddsUnknownView(): TextView {
        recyclerNameView()
        recyclerValuesView()
        recyclerLockedView()
        if (oddsUnknown != null) {
            return oddsUnknown!!
        }
        if (oddsUnknownCaches.size > 0 ) {
            oddsUnknown = addOddView(oddsUnknownCaches.removeAt(0), params2)
            return oddsUnknown!!
        }

        oddsUnknown = AppCompatTextView(context).apply {
            setTextColor(context.getColor(R.color.color_6C7BA8))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
            setText(R.string.unknown_data)
            gravity = Gravity.CENTER
        }

        addOddView(oddsUnknown!!, params2)
        return oddsUnknown!!
    }

    private fun <T: View> addOddView(view: T, params: LayoutParams, index: Int = -1): T {
        addView(view, index, params)
        view.isSelected = isSelected
        return view
    }

    private fun reBindText() {
        if (nameText.isNullOrEmpty() && spreadText.isNullOrEmpty()) {
            recyclerNameView()
            return
        }

        val nameTextView = getOddsNameView()
        nameTextView.isSelected = isSelected
        nameTextView.text = when {
            nameText.isNullOrEmpty() -> {
                spreadText
            }
            spreadText.isNullOrEmpty() -> {
                nameText
            }
            else -> {
                "$nameText  $spreadText"
            }
        }
    }


    fun setupOdd4hall(playCateCode: String, odds: Odd?, status: Int?, oddsType: OddsType, isDrawBtn: Boolean = false) {
        betStatus = status
        if (betStatus == BetStatus.DEACTIVATED.code || betStatus == BetStatus.LOCKED.code) {
            return
        }

        //判断是否反波胆玩法，显示上要单独处理
        val isOddPercentage = playCateCode.startsWith(PlayCate.LCS.value)
        mOdd = odds
        mOddsType = oddsType

        if(isDrawBtn) {
            nameText = when {
                playCateCode.isNOGALType() -> resources.getString(R.string.none)
                playCateCode.isCombination() -> {
                    (odds?.nameMap?.get(language) ?: odds?.name)?.split("-")?.firstOrNull() ?: ""
                }
                else -> {
                    odds?.nameMap?.get(language) ?: odds?.name
                }
            }
        } else {

            nameText = when {
                playCateCode.isCSType() -> {
                    odds?.nameMap?.get(language) ?: odds?.name
                }

                playCateCode.isOUType() -> {
                    //越南語大小顯示又要特殊處理(用O/U)
                    val language = if (LanguageManager.Language.VI.key.equals(language)) {
                        LanguageManager.Language.EN.key
                    } else {
                        language
                    }
                    (odds?.nameMap?.get(language) ?: odds?.name)?.abridgeOddsName()
                }

                playCateCode.isOEType() || playCateCode.isBTSType() || playCateCode.isW_METHOD_1ST() -> {
                    (odds?.nameMap?.get(language) ?: odds?.name)?.abridgeOddsName()
                }

                playCateCode.isNOGALType() -> {
                    when (language) {
                        LanguageManager.Language.ZH.key, LanguageManager.Language.ZHT.key -> {
                            "第" + odds?.nextScore.toString()
                        }
                        else -> {
                            getOrdinalNumbers(odds?.nextScore.toString())
                        }
                    }
                }
                else -> ""
            }
        }

        spreadText = odds?.spread
        if (isOddPercentage) {//反波膽顯示 %
            oddsValueText = TextUtil.formatForOddPercentage((getOdds(odds, oddsType) - 1))
        } else{
            oddsValueText = TextUtil.formatForOdd(getOdds(odds, oddsType))
        }

        isSelected = odds?.id?.let { QuickListManager.containOdd(it) } ?: false

        reBindText()
        if (odds == null) {
            return
        }

        if (odds.oddState in OddState.SAME.state..OddState.SMALLER.state) {
            oddStatus = odds.oddState
        }


    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        setEnabled(visibility == View.VISIBLE)
    }

    /**
     * 玩法判斷
     * */
    private fun String.isCSType(): Boolean {
        return this.contains(PlayCate.CS.value) && !this.isCombination()
    }

    private fun String.isOUType(): Boolean {
        return this.contains(PlayCate.OU.value) && !this.isCombination()
    }

    private fun String.isOEType(): Boolean {
        return (this.contains(PlayCate.OE.value) || this.contains(PlayCate.Q_OE.value)) && !this.isCombination()
    }

    private fun String.isBTSType(): Boolean {
        return this.contains(PlayCate.BTS.value) && !this.isCombination()
    }

    /**
     * 後端回傳文字需保留完整文字, 文字顯示縮減由前端自行處理
     */
    private fun String.abridgeOddsName(): String {
        return this.replace("Over", "O").replace("Under", "U")
    }

    /**
     * 足球：下個進球玩法會使用到
     */
    private fun getOrdinalNumbers(number:String):String {
        return when (number) {
            "1" -> "1st"
            "2" -> "2nd"
            "3" -> "3rd"
            else -> "${number}th"
        }
    }

}