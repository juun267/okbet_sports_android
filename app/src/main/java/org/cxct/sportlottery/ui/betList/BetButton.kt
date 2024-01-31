package org.cxct.sportlottery.ui.betList


import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ButtonBetBinding
import splitties.systemservices.layoutInflater
import timber.log.Timber


/**
 * @author Kevin
 * @create 2021/7/8
 * @description 包含登入按鈕、投注按鈕(兩種狀態)、賠率變動接受按鈕
 */
class BetButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val binding by lazy { ButtonBetBinding.inflate(layoutInflater,this,true) }
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
        setUpBetContent()
    }

    private fun setUpBetContent() {
        val betContent = context.getString(R.string.betting)
        binding.tvBet.text = betContent
        setupBetClickable()
    }


    private fun setupLogin(isLogin: Boolean) {
        binding.tvLogin.visibility = if (isLogin) View.GONE else View.VISIBLE
    }


    private fun setupBetClickable() {
        val opt1 = hasBetPlatClose == true
//        val opt2 = amountCanBet == false
        val opt3 = betCounts == 0
        val opt4 = isParlay && currentBetListCounts == 1
        val betClickable = !(opt1 || opt3 || opt4)
        Timber.d("setupBetClickable: betClickable:${betClickable} opt1:${opt1} op2:${true} opt3:${opt3} opt4:${opt4}")
        binding.clBet.apply {
            isSelected = betClickable
            isClickable = betClickable
        }
    }

    fun setBtnText(text:String){
        binding.tvBet.text = text
    }

    fun setOddsButtonChangeStyle() {
        binding.clBet.background = AppCompatResources.getDrawable(
            context, R.drawable.selector_button_radius_5_betting_change
        )
    }

    fun resetButtonStyle(){
        binding.clBet.background = AppCompatResources.getDrawable(
            context,R.drawable.selector_button_radius_5_bet
        )
    }

}