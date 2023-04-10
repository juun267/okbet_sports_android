package org.cxct.sportlottery.ui.betList


import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.button_bet.view.*
import org.cxct.sportlottery.R


/**
 * @author Kevin
 * @create 2021/7/8
 * @description 包含登入按鈕、投注按鈕(兩種狀態)、賠率變動接受按鈕
 */
class BetButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {


    var isLogin: Boolean? = null
        set(value) {
            field = value
            field?.let {
                setupLogin(it)
            }
        }


    var isOddsChanged: Boolean? = null
        set(value) {
            field = value
            field?.let {
                setupOddsChanged(it)
            }
        }


    var isCanSendOut: Boolean? = null
        set(value) {
            field = value
            field?.let {
                setupBetClickable()
            }
        }

    //投注金額是否為限額範圍內
    var amountCanBet: Boolean? = null
        set(value) {
            field = value
            setupBetClickable()
        }

    //已下注盤口是否有被關閉
    var hasBetPlatClose: Boolean? = null
        set(value) {
            field = value
            setupBetClickable()
        }

    //盤口是否有被關閉
    var hasPlatClose: Boolean? = null
        set(value) {
            field = value
            setupRemoveButtonVisible()
        }

    //是否為串關
    var isParlay: Boolean = false
        set(value) {
            field = value
            setupRemoveButtonVisible()
        }

    //注單有輸入金額的投注數量
    var betCounts: Int = 0
        set(value) {
            field = value
            setUpBetContent()
        }

    var currentBetListCounts: Int = 0


    init {
        init()
    }


    private fun init() {
        inflate(context, R.layout.button_bet, this)
        setUpBetContent()
//        tv_currency_type.text = sConfigData?.systemCurrencySign
//        setupQuotaListener()
    }

    private fun setUpBetContent() {
        val betContent = context.getString(R.string.betting)
//            when {
//                betCounts == 0 -> {
//                    context.getString(R.string.betting)
//                }
//                isParlay -> {
//                    val multipleBetText = context.getString(R.string.confirm_multiple_bets, betCounts)
//
//                    when (betCounts) {
//                        1 -> multipleBetText.removeSuffix("s")
//                        else -> multipleBetText
//                    }
//                }
//                else -> {
//                }
//            }
        tv_bet.text = betContent
        setupBetClickable()
    }


    private fun setupQuotaListener() {
//        tv_quota.doAfterTextChanged {
//            isCanSendOut = (it.toString().replace(",", "").toDoubleOrNull() ?: 0.0) != 0.0
//        }
    }


    private fun setupLogin(isLogin: Boolean) {
        tv_login.visibility = if (isLogin) View.GONE else View.VISIBLE
//        tv_login.setText(
//            if (sConfigData?.creditSystem == FLAG_CREDIT_OPEN)
//                R.string.btn_login_by_credit_system
//            else R.string.btn_login
//        )
    }


    private fun setupOddsChanged(isOddsChanged: Boolean) {
        //20220616 賠率更變時，按鈕顯示文案修改 (不管有無輸入金額)
//        if (isOddsChanged) {
//            tv_accept_odds_change.visibility = View.VISIBLE
//            tv_bet.visibility = View.INVISIBLE
//        } else {
//            tv_accept_odds_change.visibility = View.GONE
//            tv_bet.visibility = View.VISIBLE
//        }
    }

    private fun setupRemoveButtonVisible() {
        //不管單項還是串關，當盤口關閉，會展示[移除已关闭的选项]
        val showRemoveButton = hasPlatClose == true
//        tv_remove_closed_selections.isVisible = showRemoveButton //暫時不支援移除關閉盤口的按鈕
    }

    private fun setupBetClickable() {
        val betClickable =
            !(hasBetPlatClose == true || amountCanBet == false || betCounts == 0 || (isParlay && currentBetListCounts == 1))
        cl_bet.apply {
            isSelected = betClickable
            isClickable = betClickable
        }
    }

}