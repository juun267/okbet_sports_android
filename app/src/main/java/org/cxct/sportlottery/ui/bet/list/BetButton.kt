package org.cxct.sportlottery.ui.bet.list


import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import kotlinx.android.synthetic.main.button_bet.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.FLAG_CREDIT_OPEN
import org.cxct.sportlottery.repository.sConfigData


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

    //盤口是否有被關閉
    var hasBetPlatClose: Boolean? = null
        set(value) {
            field = value
            setupBetClickable()
        }

    //是否為串關
    var isParlay: Boolean = false

    //注單有輸入金額的投注數量
    var betCounts: Int = 0
        set(value) {
            field = value
            setUpBetContent()
        }

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
        val betContent = if (isParlay) {
            context.getString(R.string.confirm_multiple_bets, betCounts)
        } else {
            context.getString(R.string.confirm_single_bets, betCounts)
        }
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

    private fun setupBetClickable() {
        val betClickable = !(hasBetPlatClose == true || amountCanBet == false || betCounts == 0)
        cl_bet.apply {
            isSelected = betClickable
            isClickable = betClickable
        }
    }

}