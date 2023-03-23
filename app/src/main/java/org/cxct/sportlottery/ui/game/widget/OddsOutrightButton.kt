package org.cxct.sportlottery.ui.game.widget


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.button_odd_outright.view.*
import kotlinx.android.synthetic.main.button_odd_outright.view.button_odd_detail
import kotlinx.android.synthetic.main.button_odd_outright.view.img_odd_lock
import kotlinx.android.synthetic.main.button_odd_outright.view.img_odd_unknown
import kotlinx.android.synthetic.main.button_odd_outright.view.iv_arrow
import kotlinx.android.synthetic.main.button_odd_outright.view.tv_name
import kotlinx.android.synthetic.main.button_odd_outright.view.tv_odds
import kotlinx.android.synthetic.main.button_odd_outright.view.tv_spread
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.extentions.flashAnimation
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.enum.OddsType
import org.cxct.sportlottery.util.BetPlayCateFunction.isCombination
import org.cxct.sportlottery.util.BetPlayCateFunction.isNOGALType
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LocalUtils.getString
import org.cxct.sportlottery.util.QuickListManager
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds


/**
 * @author Kevin
 * @create 2021/06/21
 * @description 賠率按鈕(預設圓角)
 * @edit:
 * 2021/07/05 擴展配適直角
 * 2021/07/27 合併其他odd button
 * 2021/07/29 新增特優賠率樣式
 * 2021/08/16 新增isSelect判斷
 * 2022/06/16 大廳賠率按鈕顯示邏輯搬移至OddsButton
 */
open class OddsOutrightButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var betStatus: Int? = null
        set(value) {
            field = value
            field?.let {
                setupBetStatus(it)
            }
        }

    var oddStatus: Int? = null
        set(value) {
            field = value
            field?.let {
                setupOddState(it)
            }
        }

    private var mOdd: Odd? = null

    private var mOddsType: OddsType = OddsType.EU

    private var mFillet = true

    private var hideItem = false

    private var mBackground: Drawable? = null

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.OddsButton)
        mFillet = typedArray.getBoolean(R.styleable.OddsButton_ob_fillet, true)
        hideItem = typedArray.getBoolean(R.styleable.OddsButton_ob_hide_item_flag, false)
        mBackground =
            typedArray.getDrawable(R.styleable.OddsButton_ob_background)
                ?: context.theme.getDrawable(R.drawable.selector_button_radius_4_odds)
        try {
            inflate(context, R.layout.button_odd_outright, this)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    fun setupOdd(
        odd: Odd?,
        oddsType: OddsType,
        gameType: String? = null,
        isOddPercentage: Boolean? = false,
    ) {
        mOdd = odd
        mOddsType = oddsType
        tv_name.apply {

            val languae = LanguageManager.getSelectLanguage(context).key
            val extInfoStr = odd?.extInfoMap?.get(languae) ?: odd?.extInfo
            text = if (extInfoStr.isNullOrEmpty()) {
                "${(odd?.nameMap?.get(languae) ?: odd?.name)}"
            } else {
                "$extInfoStr ${(odd?.nameMap?.get(languae) ?: odd?.name)}"
            }

            visibility = if ("disable".equals(gameType)) View.GONE else View.VISIBLE
        }


        tv_spread.apply {
            text = odd?.spread
            visibility = if (odd?.spread.isNullOrEmpty() || odd?.playCode == PlayCate.DOUBLE_D_P.value || odd?.playCode == PlayCate.TRIPLE_D_P.value)
                View.GONE
            else
                View.VISIBLE
        }

        if (isOddPercentage == true) //反波膽顯示 %
            tv_odds?.text = TextUtil.formatForOddPercentage((getOdds(odd, oddsType) - 1))
        else
            tv_odds?.text = TextUtil.formatForOdd(getOdds(odd, oddsType))
//        updateOddsTextColor()

        val select = QuickListManager.getQuickSelectedList()?.contains(odd?.id) ?: false
        isSelected = select
        odd?.isSelected = select
        //[Martin]馬來盤＆印尼盤會有負數的賠率
        //betStatus = if (getOdds(odd, oddsType) <= 0.0 || odd == null) BetStatus.LOCKED.code else odd.status
        betStatus = if (odd == null) BetStatus.LOCKED.code else odd.status

    }

    fun setupOdd4hall(
        playCateCode: String,
        odds: Odd?,
        oddList: List<Odd?>?,
        oddsType: OddsType,
        isDrawBtn: Boolean? = false,
        isOtherBtn: Boolean? = false,
    ) {
        mOdd = odds
        mOddsType = oddsType

        if (isDrawBtn == true) {
            when {
                (oddList?.size ?: 0 > 2) -> {
                    visibility = View.VISIBLE
                }
                (oddList?.size ?: 0 < 3) -> {
                    visibility = View.INVISIBLE
                }
            }
        }

        when {
            (oddList == null || oddList.all { odd -> odd == null }) -> {
                betStatus = BetStatus.DEACTIVATED.code
                return
            }
            ((oddList.size < 2 || odds?.odds ?: 0.0 <= 0.0) && isOtherBtn == false) -> {
                betStatus = BetStatus.LOCKED.code
                return
            }
            else -> {
                betStatus = odds?.status
            }
        }

        tv_name.apply {
            if (isDrawBtn == true) {
                visibility = View.VISIBLE

                text = when {
                    playCateCode.isNOGALType() -> getString(R.string.none)
                    playCateCode.isCombination() -> {
                        (odds?.nameMap?.get(
                            LanguageManager.getSelectLanguage(context).key
                        ) ?: odds?.name)?.split("-")?.firstOrNull() ?: ""
                    }
                    !playCateCode.isCombination() -> {
                        odds?.nameMap?.get(
                            LanguageManager.getSelectLanguage(context).key
                        ) ?: odds?.name
                    }
                    else -> ""
                }
            } else {
                visibility = when {
                    playCateCode.isOUType() || playCateCode.isOEType() || playCateCode.isBTSType() || playCateCode.isNOGALType() || playCateCode.isCSType() -> View.VISIBLE
                    else -> View.GONE
                }

                text = when {
                    playCateCode.isCSType() -> {
                        odds?.nameMap?.get(
                            LanguageManager.getSelectLanguage(context).key
                        ) ?: odds?.name
                    }
                    playCateCode.isOUType() -> {
                        //越南語大小顯示又要特殊處理(用O/U)
                        val language =
                            if (LanguageManager.getSelectLanguage(context).key == LanguageManager.Language.VI.key) LanguageManager.Language.EN.key else LanguageManager.getSelectLanguage(
                                context
                            ).key
                        (odds?.nameMap?.get(
                            language
                        ) ?: odds?.name)?.abridgeOddsName()
                    }
                    playCateCode.isOEType() || playCateCode.isBTSType() -> {
                        (odds?.nameMap?.get(
                            LanguageManager.getSelectLanguage(
                                context
                            ).key
                        ) ?: odds?.name)?.abridgeOddsName()
                    }
                    playCateCode.isNOGALType() -> {
                        when (LanguageManager.getSelectLanguage(this.context)) {
                            LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> {
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
            requestLayout()
        }

        tv_spread.apply {
            visibility = when (!odds?.spread.isNullOrEmpty()) {
                true -> View.VISIBLE
                false -> {
                    when {
                        playCateCode.isOUType() -> View.INVISIBLE
                        else -> View.GONE
                    }
                }
            }
            text = odds?.spread ?: ""
            requestLayout()
        }

        tv_odds.apply {
            text = TextUtil.formatForOdd(getOdds(odds, oddsType))
        }

//        updateOddsTextColor()
        val select = QuickListManager.getQuickSelectedList()?.contains(odds?.id) ?: false
        isSelected = select
        odds?.isSelected = select
    }

    //主頁精選oddsButton的判斷
    fun setupOddName4Home(name: String?, gameType: String? = null) {
        tv_name.apply {
            if (gameType?.contains(PlayCate.SINGLE.value) == true) {
                isVisible = true
                text = name
            } else isVisible = false
        }
    }


    fun setupOddForEPS(odd: Odd?, oddsType: OddsType) {
        tv_name.apply {
            text = odd?.extInfo?.toDoubleOrNull()?.let { TextUtil.formatForOdd(it) }
                ?: odd?.extInfo //低賠率會返回在extInfo
        }

        tv_spread.visibility = View.GONE

        tv_odds.apply {
            setTextColor(
                ContextCompat.getColorStateList(
                    context,
                    R.color.selector_button_odd_bottom_text_eps
                )
            )
            text = TextUtil.formatForOdd(getOdds(odd, oddsType))
        }
        val diff = getOdds(odd, oddsType)
        if (diff < 0.0) {
            tv_odds.setTextColor(
                ContextCompat.getColorStateList(
                    context,
                    R.color.selector_button_odd_bottom_text_red
                )
            )
            iv_arrow.setImageResource(R.drawable.ic_arrow_odd_down)

        } else if (diff > 0.0) {
            tv_odds.setTextColor(
                ContextCompat.getColorStateList(
                    context,
                    R.color.selector_button_odd_bottom_text_green
                )
            )
            iv_arrow.setImageResource(R.drawable.ic_arrow_odd_up)
        } else {
            tv_odds.setTextColor(
                ContextCompat.getColorStateList(
                    context,
                    R.color.selector_button_odd_bottom_text_eps
                )
            )
            iv_arrow.setImageDrawable(null)
        }

        isSelected = odd?.isSelected ?: false
        //[Martin]馬來盤＆印尼盤會有負數的賠率
        //betStatus = if (getOdds(odd, oddsType) <= 0.0 || odd == null) BetStatus.LOCKED.code else odd.status
        betStatus = if (odd == null) BetStatus.LOCKED.code else odd.status

    }

    //常駐顯示按鈕 依狀態隱藏鎖頭
    private fun setupBetStatus(betStatus: Int) {
        img_odd_lock.apply {
            visibility =
                if (betStatus == BetStatus.LOCKED.code) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        img_odd_unknown.apply {
            visibility =
                if (betStatus == BetStatus.DEACTIVATED.code) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        isEnabled = (betStatus == BetStatus.ACTIVATED.code)
        button_odd_detail.isVisible = isEnabled
    }

    private fun setupOddState(oddState: Int) {
        if (!isEnabled) return
        var status = false
        when (oddState) {
            OddState.LARGER.state -> {
                tv_odds.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_1EB65B
                    )
                )
                iv_arrow.apply {
                    setImageResource(R.drawable.ic_arrow_odd_up)
                    visibility = View.VISIBLE
                }
                status = true
                isActivated = false
            }
            OddState.SMALLER.state -> {
                tv_odds.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_E23434
                    )
                )
                iv_arrow.apply {
                    setImageResource(R.drawable.ic_arrow_odd_down)
                    visibility = View.VISIBLE
                }
                status = true
                isActivated = false
            }
            OddState.SAME.state -> {
                resetOddsValueState()
                isActivated = false
            }
        }
        val animator = ll_odd_outright.tag
        if (animator is Animator) {
            animator.cancel()
            if (status) {
                animator.start()
                return
            }
        }

        if (status) {
            ll_odd_outright.tag = ll_odd_outright.flashAnimation(1000,2,0.3f).apply {
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        resetOddsValueState()
                    }
                })
            }
        }
//        updateOddsTextColor()
    }

    private fun resetOddsValueState() {
        tv_odds.setTextColor(
            ContextCompat.getColorStateList(
                context,
                if (MultiLanguagesApplication.isNightMode) R.color.selector_button_odd_bottom_text_dark
                else R.color.selector_button_odd_bottom_text
            )
        )
        iv_arrow.apply {
            setImageDrawable(null)
            visibility = View.GONE
        }
    }

    /**
     * 透過當前賠率更新賠率文字顏色
     */
    private fun updateOddsTextColor() {
        //負盤
        val diff = getOdds(mOdd, mOddsType)
        if (diff < 0.0) {
            tv_odds.setTextColor(
                ContextCompat.getColorStateList(
                    context,
                    R.color.selector_button_odd_bottom_text_red
                )
            )
            iv_arrow.apply {
                setImageResource(R.drawable.selector_odds_arrow_down)
                visibility = View.VISIBLE
            }
        } else if (diff > 0.0) {//正盤
            tv_odds.setTextColor(
                ContextCompat.getColorStateList(
                    context,
                    R.color.selector_button_odd_bottom_text_green
                )
            )
            iv_arrow.apply {
                setImageResource(R.drawable.selector_odds_arrow_up)
                visibility = View.VISIBLE
            }
        } else {
            tv_odds.setTextColor(
                ContextCompat.getColorStateList(
                    context,
                    if (MultiLanguagesApplication.isNightMode) R.color.selector_button_odd_bottom_text_dark
                    else R.color.selector_button_odd_bottom_text
                )
            )
            iv_arrow.apply {
                setImageDrawable(null)
                visibility = GONE
            }
        }
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
    private fun getOrdinalNumbers(number: String): String {
        return when (number) {
            "1" -> "1st"
            "2" -> "2nd"
            "3" -> "3rd"
            else -> "${number}th"
        }
    }

}
