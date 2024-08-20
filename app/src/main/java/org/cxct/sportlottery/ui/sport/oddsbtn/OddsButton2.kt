package org.cxct.sportlottery.ui.sport.oddsbtn

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.common.enums.OddsType
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
    val esportTheme: Boolean = false
) : RelativeLayout(context, attrs, defStyleAttr) {

    companion object {

        private val oddsNameCaches = mutableListOf<TextView>()
        private val oddsValueCaches = mutableListOf<OddsValueView>()
        private val oddsLockedCaches = mutableListOf<ImageView>()
        private val oddsUnknownCaches = mutableListOf<TextView>()
        private val params1 = LinearLayout.LayoutParams(-2, -2).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }
        private val params2 = LinearLayout.LayoutParams(-2, -2)


        fun clearOddsViewCaches() {
            oddsNameCaches.clear()
            oddsValueCaches.clear()
            oddsLockedCaches.clear()
            oddsUnknownCaches.clear()
        }
    }
    private val rootLin: LinearLayout

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

    private val buoyIcon: ImageView

    var betStatus: Int? = null
        set(value) {
            if (value != null) {
                field = value
                if(euTypeAndOddOne()){
                    field = BetStatus.LOCKED.code
                }
                field?.let {
                    setBetStatus(it)
                }
            }
        }

    init {
        foreground = ContextCompat.getDrawable(context, R.drawable.fg_ripple)
        setBackgroundResource(R.drawable.selector_button_radius_6_odds)
        rootLin= LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        addView(rootLin,LinearLayout.LayoutParams(-1,-1))
        buoyIcon = ImageView(context)
        addView(buoyIcon, LayoutParams(12.dp, -2).apply {addRule(ALIGN_PARENT_RIGHT,TRUE)  })
        betStatus = BetStatus.DEACTIVATED.code
    }

    private fun getBuoyAnimation(fromAlpha: Float, toAlpha: Float): ValueAnimator {
        val alphaAnim = ObjectAnimator.ofFloat(buoyIcon, "alpha", fromAlpha, toAlpha)
        alphaAnim.repeatCount = 2
        alphaAnim.duration = 1000
        alphaAnim.repeatMode = ValueAnimator.RESTART
        alphaAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                setupOddState(OddState.SAME.state)
            }
        })
        alphaAnim.start()
        return alphaAnim
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

    private fun resetBuoyIcon() {
        buoyIcon.clearAnimation()
        runningAnim?.let { it.cancel() }
        runningAnim = null
        buoyIcon.tag = null
        mOdd?.oddState = OddState.SAME.state
        (buoyIcon.parent as ViewGroup?)?.let {
            it.removeView(buoyIcon)
        }
    }

    private fun setupOddState(oddState: Int) {
        if (!isEnabled) return

        val oddView = getOddsValueView()


        when (oddState) {
            OddState.LARGER.state -> {
                oddView.onRise(oddsValueText)
                (buoyIcon.layoutParams as RelativeLayout.LayoutParams).apply {
                    removeRule(ALIGN_PARENT_BOTTOM)
                    addRule(ALIGN_PARENT_TOP,TRUE)
                    buoyIcon.layoutParams = this
                }
                playAnim(R.drawable.icon_odds_up, 0f, 1f)
            }
            OddState.SMALLER.state -> {
                oddView.onFall(oddsValueText)
                (buoyIcon.layoutParams as RelativeLayout.LayoutParams).apply {
                    removeRule(ALIGN_PARENT_TOP)
                    addRule(ALIGN_PARENT_BOTTOM,TRUE)
                    buoyIcon.layoutParams = this
                }
                playAnim(R.drawable.icon_odds_down, 0f, 1f)
            }
            else -> {
                oddView.setOdds(oddsValueText)
                resetBuoyIcon()
            }
        }

    }

    private fun recyclerNameView() {
        oddsName?.let {
            rootLin.removeView(it)
            oddsNameCaches.add(it)
            oddsName = null
        }
    }

    private fun recyclerValuesView() {
        oddsValue?.let {
            rootLin.removeView(it)
            oddsValueCaches.add(it)
            oddsValue = null
        }
    }

    private fun recyclerLockedView() {
        oddsLocked?.let {
            rootLin.removeView(it)
            oddsLockedCaches.add(it)
            oddsLocked = null
        }
    }

    private fun recyclerUnknownView() {
        oddsUnknown?.let {
            rootLin.removeView(it)
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
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            typeface = AppFont.helvetica
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
        resetBuoyIcon()

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
        resetBuoyIcon()

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

    private fun <T: View> addOddView(view: T, params: LinearLayout.LayoutParams, index: Int = -1): T {
        rootLin.addView(view, index, params)
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

    private inline fun String.isSingleType(): Boolean {
        return this.contains(PlayCate.SINGLE.value) && !this.isCombination()
    }

    fun setupOdd4hall(playCateCode: String, odds: Odd?, status: Int?, oddsType: OddsType, isDrawBtn: Boolean = false) {
        mOdd = odds
        mOddsType = oddsType
        betStatus = status
        if (betStatus == BetStatus.DEACTIVATED.code || betStatus == BetStatus.LOCKED.code) {
            return
        }
        //判断是否反波胆玩法，显示上要单独处理
        val isOddPercentage = playCateCode.startsWith(PlayCate.LCS.value)

        if(isDrawBtn) {
            nameText = when {
                playCateCode.isSingleType() -> ""  // 独赢玩法
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
            setupOddState(odds.oddState)
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        isEnabled = visibility == View.VISIBLE
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

    private var runningAnim: ValueAnimator? = null
    private fun playAnim(icon: Int, fromAlpha: Float, toAlpha: Float) {

        if (icon == buoyIcon.tag) {
            return
        }

        if (buoyIcon.parent == null) {
            addView(buoyIcon)
        }

        buoyIcon.tag = icon
        buoyIcon.setImageResource(icon)
        runningAnim = getBuoyAnimation(fromAlpha, toAlpha)
    }

    /**
     * 当前欧洲盘，并且欧洲盘赔率=1，显示锁盘
     */
    private fun euTypeAndOddOne(): Boolean{
        return mOddsType==OddsType.EU && mOdd?.odds==1.0
    }
}