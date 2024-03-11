package org.cxct.sportlottery.ui.sport.oddsbtn


import android.animation.Animator
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.flashAnimation
import org.cxct.sportlottery.databinding.ButtonOddDetailBinding
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.detail.adapter.TypeOneListAdapter
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.isEndScoreType
import org.cxct.sportlottery.util.DisplayUtil.dp
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
class OddsButtonDetail @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var betStatus: Int? = null
        set(value) {
            field = value
            if (euTypeAndOddOne()){
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
    val binding by lazy { ButtonOddDetailBinding.inflate(layoutInflater,this) }

    init {
        6.dp.let { setPadding(it, it, it, it) }
    }


    //为了在赔率不显示队名，按钮内传入队名，过滤
    private var matchInfo: MatchInfo? = null
    private var hideName = true

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
                || TextUtils.equals(context.getString(R.string.draw), odd?.name))&&adapterName!=TypeOneListAdapter::class.java.name

        binding.tvName.apply {
            val extInfoStr =
                odd?.extInfoMap?.get(LanguageManager.getSelectLanguage(context).key) ?: odd?.extInfo
            text =
                if (extInfoStr.isNullOrEmpty())
                    "${(odd?.nameMap?.get(LanguageManager.getSelectLanguage(context).key) ?: odd?.name)}"
                else
                    "$extInfoStr ${(odd?.nameMap?.get(LanguageManager.getSelectLanguage(context).key) ?: odd?.name)}"
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
        (binding.flOddDetail.layoutParams as LinearLayout.LayoutParams).apply {
            width = if(binding.linName.isVisible) -2 else -1
            binding.flOddDetail.layoutParams = this
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
        val animator = binding.flOddDetail.tag
        if (animator is Animator) {
            animator.cancel()
            if (status) {
                animator.start()
                return
            }
        }

        if (status) {
            binding.flOddDetail.tag = binding.flOddDetail.flashAnimation(1000,2,0.3f)
        }
//        updateOddsTextColor()
    }

    /**
     * 当前欧洲盘，并且欧洲盘赔率=1，显示锁盘
     */
    private fun euTypeAndOddOne(): Boolean{
        return mOddsType==OddsType.EU&&mOdd?.odds==1.0
    }
}

