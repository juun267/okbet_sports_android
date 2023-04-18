package org.cxct.sportlottery.ui.sport.oddsbtn

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
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

        private val textStyle by lazy { ResourcesCompat.getFont(MultiLanguagesApplication.appContext, R.font.din_pro_medium) }
        private val oddsNameCaches = mutableListOf<TextView>()
        private val oddsValueCaches = mutableListOf<TextView>()
        private val oddsLockedCaches = mutableListOf<ImageView>()
        private val oddsUnknownCaches = mutableListOf<TextView>()
        private val params1 = LayoutParams(-2, -2).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }
        private val params2 = LayoutParams(-1, -1)


        fun clearOddsViewCaches() {
            oddsNameCaches.clear()
            oddsValueCaches.clear()
            oddsLockedCaches.clear()
            oddsUnknownCaches.clear()
        }
    }

    private var oddsName: TextView? = null
    private var oddsValue: TextView? = null
    private var oddsLocked: ImageView? = null
    private var oddsUnknown: TextView? = null

    private val language: String by lazy { LanguageManager.getSelectLanguage(context).key }
    private var mOdd: Odd? = null
    private var mOddsType: OddsType = OddsType.EU

    private var nameText: String? = null
    private var spreadText: String? = null

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
        val tv_odds = getOddsValueView()
        var leftDrawable = 0
        var textColor = 0
        var status = false

        when (oddState) {
            OddState.LARGER.state -> {
                textColor = ContextCompat.getColor(context, R.color.color_1EB65B)
                leftDrawable = R.drawable.ic_arrow_odd_up
                isActivated = false
                status = true
            }
            OddState.SMALLER.state -> {
                textColor = ContextCompat.getColor(context, R.color.color_E23434)
                leftDrawable = R.drawable.ic_arrow_odd_down
                isActivated = false
                status = true
            }
            OddState.SAME.state -> {
                isActivated = false
            }
        }

        tv_odds.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, 0, 0 , 0)
        if (textColor != 0) {
            tv_odds.setTextColor(textColor)
        } else {
            ContextCompat.getColorStateList(
                context,
                if (MultiLanguagesApplication.isNightMode) R.color.selector_button_odd_bottom_text_dark
                else R.color.selector_button_odd_bottom_text
            ).let {
                tv_odds.setTextColor(it)
            }
        }

        val animator = tv_odds.tag
        if (animator is Animator) {
            animator.cancel()
            if (status) {
                animator.start()
                return
            }
        }

        if (status) {
            tv_odds.tag = tv_odds.flashAnimation(1000,2,0.3f).apply {
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        resetOddsValueState(tv_odds)
                    }
                })
            }

        }
    }

    private fun clearAnimator(textView: TextView) {
        val animator = textView.tag
        if (animator is Animator) {
            animator.cancel()
        }
    }

    private fun resetOddsValueState(textView: TextView) {
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0 , 0)
        textView.setTextColor(
            ContextCompat.getColorStateList(
                context,
                if (MultiLanguagesApplication.isNightMode) R.color.selector_button_odd_bottom_text_dark
                else R.color.selector_button_odd_bottom_text
            )
        )
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

    private fun setOddsValueText(value: String) {
        getOddsValueView().apply {
            text = " $value "
        }
    }

    private fun getOddsValueView(): TextView {
        recyclerLockedView()
        recyclerUnknownView()
        if (oddsValue != null) {
            return oddsValue!!
        }
        if (oddsValueCaches.size > 0 ) {
            oddsValue = addOddView(oddsValueCaches.removeAt(0), params1)
            resetOddsValueState(oddsValue!!)
            clearAnimator(oddsValue!!)
            return oddsValue!!
        }
        oddsValue = AppCompatTextView(context).apply {
            typeface = textStyle
            includeFontPadding = false
            gravity = Gravity.CENTER
            setTextColor(context.getColor(R.color.selector_button_odd_top_text))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.strokeWidth = 0.75f
        }

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

    fun setupOdd(odd: Odd?, oddsType: OddsType, gameType: String? = null, isOddPercentage:Boolean? = false) {
        mOdd = odd
        mOddsType = oddsType
        isSelected = odd?.isSelected ?: false
        if (odd == null) {
            betStatus = BetStatus.LOCKED.code
            return
        }

        if (odd.status == BetStatus.DEACTIVATED.code) {
            betStatus = BetStatus.DEACTIVATED.code
            return
        }

        if(isOddPercentage == true) { //反波膽顯示 %
            setOddsValueText(TextUtil.formatForOddPercentage((getOdds(odd, oddsType) - 1)))
        } else {
            setOddsValueText(TextUtil.formatForOdd(getOdds(odd, oddsType)))
        }

        spreadText = if (odd?.spread.isNullOrEmpty() ||  PlayCate.DOUBLE_D_P.value.equals(odd?.playCode) || PlayCate.TRIPLE_D_P.value.equals(odd?.playCode)) {
            null
        } else {
            odd?.spread
        }

        nameText = if (odd?.name.isNullOrEmpty() || "disable".equals(gameType)) {
            null
        } else {
            val extInfoStr = odd?.extInfoMap?.get(language) ?: odd?.extInfo
            if (extInfoStr.isNullOrEmpty()) {
                "${(odd?.nameMap?.get(language) ?: odd?.name)}"
            } else {
                "$extInfoStr ${(odd?.nameMap?.get(language) ?: odd?.name)}"
            }
        }

        reBindText()
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
            setOddsValueText(TextUtil.formatForOddPercentage((getOdds(odds, oddsType) - 1)))
        } else{
            setOddsValueText(TextUtil.formatForOdd(getOdds(odds, oddsType)))
        }

        reBindText()
        isSelected = QuickListManager.getQuickSelectedList()?.contains(odds?.id) ?: false

        if (odds == null || oddStatus == odds.oddState) {
            return
        }

        if (odds.oddState in OddState.SAME.state..OddState.SMALLER.state) {
            oddStatus = odds.oddState
        }
    }

    //主頁精選oddsButton的判斷
    fun setupOddName4Home(name: String?, gameType: String? = null) {
        oddsName?.let {
            if (name.isNullOrEmpty() || gameType?.contains(PlayCate.SINGLE.value) == false) {
                it.text = spreadText
                return
            }

            it.text = "$name $spreadText"
        }
    }

    fun setupOddForEPS(odd: Odd?, oddsType: OddsType) {
        spreadText = ""
        nameText = odd?.extInfo?.toDoubleOrNull()?.let { TextUtil.formatForOdd(it) }?: odd?.extInfo //低賠率會返回在extInfo
        reBindText()
        getOddsNameView().paint?.flags = Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG //設置中間線

        val oddsTextView = getOddsValueView()
        var textColor: ColorStateList
        var leftDrawable = 0
        oddsTextView.text = TextUtil.formatForOdd(getOdds(odd, oddsType))
        val diff = getOdds(odd, oddsType)
        if (diff < 0.0) {
            textColor = ContextCompat.getColorStateList(context, R.color.selector_button_odd_bottom_text_red)!!
            leftDrawable = R.drawable.ic_arrow_odd_down

        } else if (diff > 0.0) {
            textColor = ContextCompat.getColorStateList(context, R.color.selector_button_odd_bottom_text_green)!!
            leftDrawable = R.drawable.ic_arrow_odd_up
        } else {
            textColor = ContextCompat.getColorStateList(context, R.color.selector_button_odd_bottom_text_eps)!!
        }
        oddsTextView.setTextColor(textColor)
        oddsName?.setTextColor(textColor)

//        var spannableStringBuilder = SpannableStringBuilder()
//        spannableStringBuilder.append(ImageSpan(context, leftDrawable))
        oddsTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(leftDrawable, 0 ,0, 0)
        isSelected = odd?.isSelected ?: false
        //[Martin]馬來盤＆印尼盤會有負數的賠率
        //betStatus = if (getOdds(odd, oddsType) <= 0.0 || odd == null) BetStatus.LOCKED.code else odd.status
        betStatus = if (odd == null) BetStatus.LOCKED.code else odd.status
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