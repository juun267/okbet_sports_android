package org.cxct.sportlottery.ui.bet.list


import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
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

    //盤口是否有被關閉
    var hasBetPlatClose: Boolean? = null
        set(value) {
            field = value
            setupBetClickable()
        }


    init {
        init()
    }


    private fun init() {
        inflate(context, R.layout.button_bet, this)
        setupQuotaListener()
    }


    private fun setupQuotaListener() {
        tv_quota.doAfterTextChanged {
            isCanSendOut = (it.toString().replace(",", "").toDoubleOrNull()?:0.0) != 0.0
        }
    }


    private fun setupLogin(isLogin: Boolean) {
        tv_login.visibility = if (isLogin) View.GONE else View.VISIBLE
    }


    private fun setupOddsChanged(isOddsChanged: Boolean) {
        tv_accept_odds_change.visibility =
            if (isOddsChanged && (tv_quota.text.toString().replace(",", "").toDoubleOrNull()?:0.0) != 0.0) View.VISIBLE else View.GONE
    }


    private fun setupSendOutClickable(isCanSendOut: Boolean) {
        cl_bet.apply {
            isSelected = isCanSendOut
            isClickable = isCanSendOut
        }

        tv_accept_odds_change.apply {
            isSelected = isCanSendOut
            isClickable = isCanSendOut
        }
    }

    private fun setupBetClickable() {
        val betClickable = !(hasBetPlatClose == true || isCanSendOut == false || amountCanBet == false)
        cl_bet.apply {
            isSelected = betClickable
            isClickable = betClickable
        }

        tv_accept_odds_change.apply {
            isSelected = betClickable
            isClickable = betClickable
        }
    }


}