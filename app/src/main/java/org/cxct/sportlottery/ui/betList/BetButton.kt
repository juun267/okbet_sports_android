package org.cxct.sportlottery.ui.betList


import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.button_bet.view.*
import org.cxct.sportlottery.R
import timber.log.Timber


/**
 * @author Kevin
 * @create 2021/7/8
 * @description 包含登入按鈕、投注按鈕(兩種狀態)、賠率變動接受按鈕
 */
class BetButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {


    init {
        init()
    }


    var isLogin: Boolean? = null
        set(value) {
            field = value
            field?.let {
                setupLogin(it)
            }
        }


    var isOddsChanged: Boolean? = null


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

    //是否為串關
    var isParlay: Boolean = false

    //注單有輸入金額的投注數量
    var betCounts: Int = 0
        set(value) {
            field = value
            setUpBetContent()
        }

    var currentBetListCounts: Int = 0


    private fun init() {
        inflate(context, R.layout.button_bet, this)
        setUpBetContent()
    }

    private fun setUpBetContent() {
        val betContent = context.getString(R.string.betting)
        tv_bet.text = betContent
        setupBetClickable()
    }


    private fun setupLogin(isLogin: Boolean) {
        tv_login.visibility = if (isLogin) View.GONE else View.VISIBLE
    }


    private fun setupBetClickable() {
        val opt1 = hasBetPlatClose == true
        val opt2 = amountCanBet == false
        val opt3 = betCounts == 0
        val opt4 = isParlay && currentBetListCounts == 1
        val betClickable = !(opt1 || opt2 || opt3 || opt4)
        Timber.d("setupBetClickable: betClickable:${betClickable} opt1:${opt1} op2:${opt2} opt3:${opt3} opt4:${opt4}")
        cl_bet.apply {
            isSelected = betClickable
            isClickable = betClickable
        }
    }

}