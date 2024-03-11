package org.cxct.sportlottery.ui.sport.oddsbtn


import android.animation.Animator
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.flashAnimation
import org.cxct.sportlottery.databinding.ButtonOddDetailScoBinding
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.detail.adapter.TypeOneListAdapter
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.isCombination
import org.cxct.sportlottery.util.BetPlayCateFunction.isEndScoreType
import org.cxct.sportlottery.util.LocalUtils.getString
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
class OddsButtonDetailSCO @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
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
    
    val binding by lazy { ButtonOddDetailScoBinding.inflate(layoutInflater,this, true) }

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
                ?: context.theme.getDrawable(R.drawable.selector_button_radius_6_odds)
        binding.apply {
            buttonOddDetail.background = mBackground
        }
    }

    fun setupOdd(
        odd: Odd?,
        oddsType: OddsType,
        gameType: String? = null,
        isOddPercentage: Boolean? = false,
        matchInfo: MatchInfo?,
        adapterName: String?=null
    ) {
        mOdd = odd
        mOddsType = oddsType
        this.matchInfo = matchInfo
        hideName = (TextUtils.equals(matchInfo?.homeName, odd?.name)
                || TextUtils.equals(matchInfo?.awayName, odd?.name)
                || TextUtils.equals(getString(R.string.draw), odd?.name))&&adapterName!=TypeOneListAdapter::class.java.name

        binding.tvName.apply {
//            val extInfoStr =
//                odd?.extInfoMap?.get(LanguageManager.getSelectLanguage(context).key) ?: odd?.extInfo
            text =
//                if (extInfoStr.isNullOrEmpty())
                "${(odd?.nameMap?.get(LanguageManager.getSelectLanguage(context).key) ?: odd?.name)}"
//                else
//                    "$extInfoStr ${(odd?.nameMap?.get(LanguageManager.getSelectLanguage(context).key) ?: odd?.name)}"
            requestLayout()

            visibility =
                if (odd?.name.isNullOrEmpty() || gameType == "disable" || hideName) View.GONE else View.VISIBLE
        }

        binding.tvSpread.apply {
            text = odd?.spread
            requestLayout()
            visibility =
                if (odd?.spread.isNullOrEmpty() || odd?.name == odd?.spread || odd?.playCode == PlayCate.DOUBLE_D_P.value || odd?.playCode == PlayCate.TRIPLE_D_P.value) View.GONE else View.VISIBLE
        }

        if(isOddPercentage == true) //反波膽顯示 %
            binding.tvOdds?.text = TextUtil.formatForOddPercentage((getOdds(odd, oddsType) - 1))
        else
            binding.tvOdds?.text = TextUtil.formatForOdd(getOdds(odd, oddsType))

//        updateOddsTextColor()


//        Timber.d("更新单个条目 isSelected:${isSelected} oddName:${odd?.name}")
        isSelected = odd?.isSelected ?: false
        //[Martin]馬來盤＆印尼盤會有負數的賠率
        //betStatus = if (getOdds(odd, oddsType) <= 0.0 || odd == null) BetStatus.LOCKED.code else odd.status
        betStatus = if (odd == null) BetStatus.LOCKED.code else odd.status

        binding.linName.isVisible = !(hideName && !binding.tvSpread.isVisible)
        //篮球末尾比分，只显示最后空格后面的比分
        if (mOdd?.playCode?.isEndScoreType() == true) {
            binding.tvOdds.text = binding.tvName.text.toString().split(" ")?.last()
            binding.linName.isVisible = false
        }
    }

    //常駐顯示按鈕 依狀態隱藏鎖頭
    private fun setupBetStatus(betStatus: Int) {
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
                        R.color.color_1CD219
                    )
                )
                binding.ivArrow.apply {
                    setImageResource(R.drawable.icon_odds_up)
                    (layoutParams as FrameLayout.LayoutParams).gravity = Gravity.TOP or Gravity.RIGHT
                    visibility = View.VISIBLE

                }
                status = true
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
                    (layoutParams as FrameLayout.LayoutParams).gravity = Gravity.BOTTOM or Gravity.RIGHT
                    visibility = View.VISIBLE
                }
                status = true
                isActivated = false
            }
            OddState.SAME.state -> {
                binding.tvOdds.setTextColor(
                    ContextCompat.getColorStateList(
                        context,
                        if (MultiLanguagesApplication.isNightMode) R.color.selector_button_odd_bottom_text_dark
                        else R.color.selector_button_odd_bottom_text
                    )
                )
                binding.ivArrow.apply {
                    setImageDrawable(null)
                    visibility = View.GONE
                }
                isActivated = false
            }
        }
        val animator = binding.llOddDetail.tag
        if (animator is Animator) {
            animator.cancel()
            if (status) {
                animator.start()
                return
            }
        }

        if (status) {
            binding.llOddDetail.tag = binding.llOddDetail.flashAnimation(1000,2,0.3f)
        }
//        updateOddsTextColor()
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
    private fun euTypeAndOddOne(): Boolean{
        return mOddsType==OddsType.EU&&mOdd?.odds==1.0
    }
}

