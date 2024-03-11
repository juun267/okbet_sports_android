package org.cxct.sportlottery.ui.sport.oddsbtn


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.flashAnimation
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ButtonOddHotHomeBinding
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.util.BetPlayCateFunction.isCombination
import org.cxct.sportlottery.util.BetPlayCateFunction.isNOGALType
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LocalUtils.getString
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
class OddsHomeHotButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var betStatus: Int? = null
        set(value) {
            field = value
            if(euTypeAndOddOne()){
                field = BetStatus.LOCKED.code
            }
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

    private var hideItem = false

    private var mBackground: Drawable? = null
    private var oddOrientation = LinearLayout.HORIZONTAL
    private val binding by lazy { ButtonOddHotHomeBinding.inflate(layoutInflater,this,true) }

    init {
        init(attrs)
    }


    //为了在赔率不显示队名，按钮内传入队名，过滤
    private var matchInfo: MatchInfo? = null
    private var hideName = true

    private fun init(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.OddsButton)
        hideItem = typedArray.getBoolean(R.styleable.OddsButton_ob_hide_item_flag, false)
        mBackground =
            typedArray.getDrawable(R.styleable.OddsButton_ob_background)
                ?: context.theme.getDrawable(R.drawable.selector_button_home_hot_odds)
        oddOrientation =
            typedArray.getInt(R.styleable.OddsButton_ob_orientation, LinearLayout.HORIZONTAL)
        try {
            binding.apply {
                buttonOddDetail.background = mBackground
                buttonOddDetail.orientation = oddOrientation
                if (oddOrientation == LinearLayout.HORIZONTAL) {
                    buttonOddDetail.setPadding(8.dp, 0, 8.dp, 0)
                    linName.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                } else {
                    buttonOddDetail.setPadding(0, 8.dp, 0, 8.dp)
                    linName.gravity = Gravity.CENTER
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    fun isLocked(): Boolean = BetStatus.LOCKED.code == betStatus

    fun isDeactivated() = BetStatus.DEACTIVATED.code == betStatus

    fun deactivatedOdds() {
        betStatus = BetStatus.DEACTIVATED.code
    }

    fun lockOdds() {
        betStatus = BetStatus.LOCKED.code
    }
    fun setupOdd4hall(
        playCateCode: String,
        odds: Odd?,
        oddList: List<Odd?>?,
        oddsType: OddsType,
        isDrawBtn: Boolean? = false,
        isOtherBtn: Boolean? = false,
        hideName: Boolean = false,
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

        binding.tvName.apply {

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
                visibility = View.VISIBLE
//                    when {
//                    playCateCode.isOUType() || playCateCode.isOEType() || playCateCode.isBTSType() || playCateCode.isNOGALType() || playCateCode.isCSType() || playCateCode.isSingleType() -> View.VISIBLE
//                    else -> View.GONE
//                }
                text = when {
                   hideName -> {
                        //独赢可能出现没有和的情况
                        var index = oddList.indexOf(odds)
                        when (index) {
                            0 -> context.getString(R.string.odds_button_name_home)
                            1 -> if (oddList.size > 2) context.getString(R.string.draw_name) else context.getString(R.string.odds_button_name_away)
                            2 -> context.getString(R.string.odds_button_name_away)
                            else -> ""
                        }
                    }
                    else -> {
                        odds?.nameMap?.get(
                            LanguageManager.getSelectLanguage(context).key
                        ) ?: odds?.name
                    }

//                    playCateCode.isOUType() -> {
//                        //越南語大小顯示又要特殊處理(用O/U)
//                        val language =
//                            if (LanguageManager.getSelectLanguage(context).key == LanguageManager.Language.VI.key) LanguageManager.Language.EN.key else LanguageManager.getSelectLanguage(
//                                context
//                            ).key
//                        (odds?.nameMap?.get(
//                            language
//                        ) ?: odds?.name)?.abridgeOddsName()
//                    }
//                    playCateCode.isOEType() || playCateCode.isBTSType() -> {
//                        (odds?.nameMap?.get(
//                            LanguageManager.getSelectLanguage(
//                                context
//                            ).key
//                        ) ?: odds?.name)?.abridgeOddsName()
//                    }
//                    playCateCode.isNOGALType() -> {
//                        when (LanguageManager.getSelectLanguage(this.context)) {
//                            LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> {
//                                "第" + odds?.nextScore.toString()
//                            }
//                            else -> {
//                                getOrdinalNumbers(odds?.nextScore.toString())
//                            }
//                        }
//                    }
//                    else -> ""
                }
            }
//            if (hideName) {
//                tvName.isVisible = false
//            }
            requestLayout()
        }

        binding.tvSpread.apply {
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
//        tvSpread.visibility = View.GONE
        binding.tvOdds.apply {
            text = TextUtil.formatForOdd(getOdds(odds, oddsType))
        }

//        updateOddsTextColor()

        isSelected = odds?.isSelected ?: false
    }

    //主頁精選oddsButton的判斷
    fun setupOddName4Home(name: String?, gameType: String? = null) {
        binding.tvName.apply {
            if (gameType?.contains(PlayCate.SINGLE.value) == true) {
                isVisible = true
                text = name
            } else isVisible = false
        }
    }


    //常駐顯示按鈕 依狀態隱藏鎖頭
    private fun setupBetStatus(betStatus: Int) {
//        buttonOddDetail.setBackgroundResource(R.drawable.bg_gray_border_8)
        binding.imgOddLock.apply {
            visibility =
                if (betStatus == BetStatus.LOCKED.code) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
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
        binding.buttonOddDetail.isVisible = isEnabled
    }

    private fun setupOddState(oddState: Int) {
        if (!isEnabled) return
        var status = false
        when (oddState) {
            OddState.LARGER.state -> {
                binding.tvOdds.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_1EB65B
                    )
                )
//                iv_arrow.apply {
//                    setImageResource(R.drawable.ic_match_green_up)
//                    visibility = View.VISIBLE
//                }
                binding.ivMarkTop.visible()
                binding.ivMarkBottom.gone()
                binding.buttonOddDetail.setBackgroundResource(R.drawable.bg_home_hot_green)
                status = true
                isActivated = false
            }
            OddState.SMALLER.state -> {
                binding.tvOdds.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_E23434
                    )
                )
//                iv_arrow.apply {
//                    setImageResource(R.drawable.ic_match_red_down)
//                    visibility = View.VISIBLE
//                }
                binding.ivMarkBottom.visible()
                binding.ivMarkTop.gone()
                binding.buttonOddDetail.setBackgroundResource(R.drawable.bg_home_hot_red)
                status = true
                isActivated = false
            }
            OddState.SAME.state -> {
                resetOddsValueState(binding.tvOdds)
                isActivated = false
            }
        }

        val animator = binding.linOdd.tag
        if (animator is Animator) {
            animator.cancel()
            if (status) {
                animator.start()
                return
            }
        }

        if (status) {
            binding.linOdd.tag = binding.linOdd.flashAnimation(1000,2,0.9f).apply {
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        resetOddsValueState(binding.tvOdds)
                    }
                })
            }
        }
//        updateOddsTextColor()
    }

    private fun resetOddsValueState(textView: TextView) {
//        iv_arrow.apply {
//            setImageDrawable(null)
//            visibility = View.GONE
//        }
        binding.ivMarkTop.gone()
        binding.ivMarkBottom.gone()
        binding.buttonOddDetail.setBackgroundResource(R.drawable.selector_button_home_hot_odds)
        textView.setTextColor(
            ContextCompat.getColorStateList(
                context,
                if (MultiLanguagesApplication.isNightMode) R.color.selector_button_odd_bottom_text_dark
                else R.color.selector_button_odd_bottom_text_hot
            )
        )
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

    private fun String.isSingleType(): Boolean {
        return this.contains(PlayCate.SINGLE.value) && !this.isCombination()
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
    /**
     * 当前欧洲盘，并且欧洲盘赔率=1，显示锁盘
     */
    private fun euTypeAndOddOne(): Boolean{
        return mOddsType==OddsType.EU&&mOdd?.odds==1.0
    }
}

