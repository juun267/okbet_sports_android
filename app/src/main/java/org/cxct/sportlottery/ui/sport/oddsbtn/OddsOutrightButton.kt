package org.cxct.sportlottery.ui.sport.oddsbtn


import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ButtonOddOutrightBinding
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.util.BetPlayCateFunction.isCombination
import org.cxct.sportlottery.util.BetPlayCateFunction.isEndScoreType
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.QuickListManager
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds
import splitties.systemservices.layoutInflater


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

    private val upAnim by lazy { TranslateAnimation(0f, 0f, 0f, -4.dp.toFloat()) }
    private val downAnim by lazy { TranslateAnimation(0f, 0f, 0f, 4.dp.toFloat()) }

    var betStatus: Int? = null
        set(value) {
            field = value
            field?.let {
                setupBetStatus(it)
            }
        }

    var oddStatus: Int? = null
        set(value) {
            if (value != field) {
                setupOddState(value ?: OddState.SAME.state)
            }
            field = value
        }

    private var mOdd: Odd? = null

    private var mOddsType: OddsType = OddsType.EU

    private var mFillet = true

    private var hideItem = false

    private var mBackground: Drawable? = null
    
    val binding by lazy { ButtonOddOutrightBinding.inflate(layoutInflater,this,true) }

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.OddsButton)
        mFillet = typedArray.getBoolean(R.styleable.OddsButton_ob_fillet, true)
        hideItem = typedArray.getBoolean(R.styleable.OddsButton_ob_hide_item_flag, false)
        mBackground =
            typedArray.getDrawable(R.styleable.OddsButton_ob_background)
                ?: context.theme.getDrawable(R.drawable.selector_button_radius_6_odds)
    }

    fun setupOdd(
        odd: Odd?,
        oddsType: OddsType,
        isOddPercentage: Boolean? = false,
    ) {
        mOdd = odd
        mOddsType = oddsType

        val languae = LanguageManager.getSelectLanguage(context).key
        val extInfoStr = odd?.extInfoMap?.get(languae) ?: odd?.extInfo

        if (mOdd?.playCode?.isEndScoreType() == true) {
            binding.tvName.text = mOdd?.name
        } else {
            binding.tvName.text = if (extInfoStr.isNullOrEmpty()) {
                "${(odd?.nameMap?.get(languae) ?: odd?.name)}"
            } else {
                "$extInfoStr ${(odd?.nameMap?.get(languae) ?: odd?.name)}"
            }
        }

        binding.tvSpread.apply {
            text = odd?.spread
            visibility = if (odd?.spread.isNullOrEmpty() || odd?.playCode == PlayCate.DOUBLE_D_P.value || odd?.playCode == PlayCate.TRIPLE_D_P.value)
                View.GONE
            else
                View.VISIBLE
        }

        if (isOddPercentage == true) //反波膽顯示 %
            binding.tvOdds.text = TextUtil.formatForOddPercentage((getOdds(odd, oddsType) - 1))
        else
            binding.tvOdds.text = TextUtil.formatForOdd(getOdds(odd, oddsType))
//        updateOddsTextColor()

        val select = odd?.id?.let { QuickListManager.containOdd(it) } ?: false
        isSelected = select
        odd?.isSelected = select
        //[Martin]馬來盤＆印尼盤會有負數的賠率
        //betStatus = if (getOdds(odd, oddsType) <= 0.0 || odd == null) BetStatus.LOCKED.code else odd.status
        betStatus = if (odd == null) BetStatus.LOCKED.code else odd.status

    }

    //常駐顯示按鈕 依狀態隱藏鎖頭
    private fun setupBetStatus(betStatus: Int) {

        if (betStatus == BetStatus.LOCKED.code) {
            clearStatus()
            binding.tvOdds.text = ""
            binding.ivArrow.visible()
            binding.ivArrow.setImageResource(R.drawable.ic_lock)
        }

        binding.imgOddUnknown.apply {
            visibility =
                if (betStatus == BetStatus.DEACTIVATED.code) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        isEnabled = (betStatus == BetStatus.ACTIVATED.code)
        binding.buttonOddDetail.isVisible = betStatus != BetStatus.DEACTIVATED.code
    }

    private fun setupOddState(oddState: Int) {
        if (!isEnabled) return

        var animation: Animation? = null
        when (oddState) {
            OddState.LARGER.state -> {
                binding.tvOdds.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_1CD219
                    )
                )
                binding.ivArrow.apply {
                    setImageResource(R.drawable.icon_odds_up)
                    (layoutParams as LinearLayout.LayoutParams).apply {
                        gravity = Gravity.TOP
                        layoutParams = this
                    }
                    visibility = View.VISIBLE
                }
                animation = upAnim
                isActivated = false
            }
            OddState.SMALLER.state -> {
                binding.tvOdds.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_FF2E00
                    )
                )
                binding.ivArrow.apply {
                    setImageResource(R.drawable.icon_odds_down)
                    (layoutParams as LinearLayout.LayoutParams).apply {
                        gravity = Gravity.BOTTOM
                        layoutParams = this
                    }
                    visibility = View.VISIBLE
                }
                animation = downAnim
                isActivated = false
            }
            OddState.SAME.state -> {
                resetOddsValueState()
                isActivated = false
            }
        }

        if (binding.ivArrow.tag == animation) {
            return
        }

        playAnim(animation!!)
    }

    private fun playAnim(animation: Animation) {

        animation.interpolator = DecelerateInterpolator()
        animation.repeatCount = 3
        animation.repeatMode = Animation.RESTART
        animation.duration = 500
        animation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation) { }
            override fun onAnimationRepeat(animation: Animation) {
            }
            override fun onAnimationEnd(animation: Animation) {
                resetOddsValueState()
            }
        })
        binding.ivArrow.apply {
            tag = animation
            visible()
            startAnimation(animation)
        }
    }

    private fun resetOddsValueState() {
        clearStatus()
        binding.ivArrow.gone()
    }

    private fun clearStatus() {
        binding.tvOdds.setTextColor(ContextCompat.getColorStateList(context, R.color.selector_button_odd_bottom_text))
        binding.ivArrow.clearAnimation()
        binding.ivArrow.tag = null
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
