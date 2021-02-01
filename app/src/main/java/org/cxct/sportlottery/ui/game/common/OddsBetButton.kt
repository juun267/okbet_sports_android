package org.cxct.sportlottery.ui.game.common

import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.play_category_bet_btn.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.game.odds.MatchOddAdapter
import java.lang.Exception

const val O_TYPE = 0
const val U_TYPE = 1

class OddsBetButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val CHANGING_COLOR_DURATION: Long = 3000
    }

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        inflate(context, R.layout.play_category_bet_btn, this)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.OddsBetButton)

        try {
            setupOUType(typedArray.getInteger(R.styleable.OddsBetButton_ouType, -1))

            setStatus(false, typedArray.getInteger(R.styleable.OddsBetButton_status, BetStatus.ACTIVATED.code))

            bet_top_text.text = typedArray.getString(R.styleable.OddsBetButton_topText)

            bet_bottom_text.text = typedArray.getString(R.styleable.OddsBetButton_bottomText)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    private fun setupOUType(type: Int) {
        when (type) {
            O_TYPE -> {
                bet_type.visibility = View.VISIBLE
                bet_type.text = "O"
            }
            U_TYPE -> {
                bet_type.visibility = View.VISIBLE
                bet_type.text = "U"
            }
            else -> {
                bet_type.visibility = View.INVISIBLE
            }
        }
    }

    //0:活跃可用，可投注、1：临时锁定，不允许投注、2：不可用，不可见也不可投注
    fun setStatus(isOddsNull: Boolean, status: Int) {
        var itemState = status
        if (isOddsNull) itemState = BetStatus.DEACTIVATED.code

        when (itemState) {
            BetStatus.ACTIVATED.code -> {
                bet_layout.visibility = View.VISIBLE
                bet_lock.visibility = View.GONE
                bet_layout.isEnabled = true
            }
            BetStatus.LOCKED.code -> {
                bet_layout.visibility = View.VISIBLE
                bet_lock.visibility = View.VISIBLE
                bet_layout.isEnabled = false
            }
            BetStatus.DEACTIVATED.code -> {
                bet_layout.visibility = View.GONE
                bet_lock.visibility = View.GONE
                bet_layout.isEnabled = false
            }
        }
    }

    //新值較大,亮綠色 ; 新值較小,亮紅色
    fun setHighlight(status: Int) {

        when (status) {
            OddState.SAME.state -> return

            OddState.LARGER.state -> bet_layout.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green))

            OddState.SMALLER.state -> bet_layout.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red))
        }

        Handler().postDelayed(
            {
                bet_layout.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
            },
            CHANGING_COLOR_DURATION
        )
    }
}