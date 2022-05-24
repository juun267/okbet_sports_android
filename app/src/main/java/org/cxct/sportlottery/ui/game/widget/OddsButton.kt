package org.cxct.sportlottery.ui.game.widget


import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager
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
 */
class OddsButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
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
            inflate(context, R.layout.button_odd_detail, this).apply {
                button_odd_detail.background = mBackground
            }
        } catch (e: Exception) {
            typedArray.recycle()
        }
    }

    fun setupOdd(odd: Odd?, oddsType: OddsType, gameType: String? = null) {
        tv_name.apply {
            val extInfoStr =
                odd?.extInfoMap?.get(LanguageManager.getSelectLanguage(context).key) ?: odd?.extInfo
            text =
                if (extInfoStr.isNullOrEmpty())
                    "${(odd?.nameMap?.get(LanguageManager.getSelectLanguage(context).key) ?: odd?.name)}"
                else
                    "$extInfoStr ${(odd?.nameMap?.get(LanguageManager.getSelectLanguage(context).key) ?: odd?.name)}"

            visibility =
                if (odd?.name.isNullOrEmpty() || gameType == "disable") View.GONE else View.VISIBLE
        }

        tv_spread.apply {
            text = odd?.spread
            visibility =
                if (odd?.spread.isNullOrEmpty() || odd?.playCode == PlayCate.DOUBLE_D_P.value || odd?.playCode == PlayCate.TRIPLE_D_P.value) View.GONE else View.VISIBLE
        }

        tv_odds?.text = TextUtil.formatForOdd(getOdds(odd, oddsType))

        if (getOdds(odd, oddsType) < 0.0) {
            tv_odds.setTextColor(
                ContextCompat.getColorStateList(
                    context,
                    R.color.selector_button_odd_bottom_text_red
                )
            )
        } else {
            tv_odds.setTextColor(
                ContextCompat.getColorStateList(
                    context,
                    if (MultiLanguagesApplication.isNightMode) R.color.selector_button_odd_bottom_text_dark
                    else R.color.selector_button_odd_bottom_text
                )
            )
        }

        isSelected = odd?.isSelected ?: false
        //[Martin]馬來盤＆印尼盤會有負數的賠率
        //betStatus = if (getOdds(odd, oddsType) <= 0.0 || odd == null) BetStatus.LOCKED.code else odd.status
        betStatus = if (odd == null) BetStatus.LOCKED.code else odd.status

    }

    //主頁精選oddsButton的判斷
    fun setupOddName4Home(name: String?, gameType: String? = null) {
        tv_name.apply {
            if (gameType?.contains("1X2") == true) {
                isVisible = true
                text = name
            } else isVisible = false
        }
    }


    fun setupOddForEPS(odd: Odd?, oddsType: OddsType) {
        tv_name.apply {
            text = odd?.extInfo?.toDoubleOrNull()?.let { TextUtil.formatForOdd(it) }
                ?: odd?.extInfo //低賠率會返回在extInfo
            paint?.flags = Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG //設置中間線
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

        if (getOdds(odd, oddsType) < 0.0) {
            tv_odds.setTextColor(
                ContextCompat.getColorStateList(
                    context,
                    R.color.selector_button_odd_bottom_text_red
                )
            )
        } else {
            tv_odds.setTextColor(
                ContextCompat.getColorStateList(
                    context,
                    R.color.selector_button_odd_bottom_text_eps
                )
            )
        }

        isSelected = odd?.isSelected ?: false
        //[Martin]馬來盤＆印尼盤會有負數的賠率
        //betStatus = if (getOdds(odd, oddsType) <= 0.0 || odd == null) BetStatus.LOCKED.code else odd.status
        betStatus = if (odd == null) BetStatus.LOCKED.code else odd.status

    }

    //常駐顯示按鈕 依狀態隱藏鎖頭
    private fun setupBetStatus(betStatus: Int) {
        img_odd_lock.apply {
            background = ContextCompat.getDrawable(
                context,
                if (mFillet) R.drawable.bg_radius_4_button_odds_lock else R.drawable.bg_radius_0_button_odds_lock
            )

            visibility =
                if (betStatus == BetStatus.LOCKED.code) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        img_odd_unknown.apply {
            background = ContextCompat.getDrawable(
                context,
                if (mFillet) R.drawable.bg_radius_4_button_odds_lock else R.drawable.bg_radius_0_button_odds_lock
            )

            visibility =
                if (betStatus == BetStatus.DEACTIVATED.code) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        isEnabled = (betStatus == BetStatus.ACTIVATED.code)
    }

    private fun setupOddState(oddState: Int) {
        if (!isEnabled) return

        when (oddState) {
            OddState.LARGER.state -> {
                button_odd_detail.background =
                    ContextCompat.getDrawable(
                        context,
                        if (mFillet) R.drawable.bg_radius_4_button_unselected_green
                        else R.drawable.bg_radius_0_button_green
                    )

                isActivated = true
            }
            OddState.SMALLER.state -> {
                button_odd_detail.background =
                    ContextCompat.getDrawable(
                        context,
                        if (mFillet) R.drawable.bg_radius_4_button_unselected_red
                        else R.drawable.bg_radius_0_button_red
                    )
                resources.getColor(R.color.color_E44438_e44438)
                isActivated = true
            }
            else -> {
                button_odd_detail.background =
                    ContextCompat.getDrawable(
                        context,
                        if (mFillet) {
                            if (MultiLanguagesApplication.isNightMode) R.drawable.selector_button_radius_4_odds_dark
                            else R.drawable.selector_button_radius_4_odds
                        } else R.drawable.selector_button_radius_0_odds
                    )

                isActivated = false

                tv_odds.setTextColor(
                    ContextCompat.getColorStateList(
                        context,
                        if (MultiLanguagesApplication.isNightMode) {
                            R.color.selector_button_odd_bottom_text_dark
                        } else R.color.selector_button_odd_bottom_text
                    )
                )
                if (MultiLanguagesApplication.isNightMode) {
                    tv_odds.isActivated = true
                }
            }
        }
    }
}