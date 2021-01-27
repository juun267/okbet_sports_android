package org.cxct.sportlottery.ui.game.common

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.play_category_bet_btn.view.*
import org.cxct.sportlottery.R
import java.lang.Exception

const val O_TYPE = 0
const val U_TYPE = 1

class OddsBetButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        inflate(context, R.layout.play_category_bet_btn, this)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.OddsBetButton)

        try {
            setupOUType(typedArray.getInteger(R.styleable.OddsBetButton_ouType, -1))

            setupStatus(false, typedArray.getInteger(R.styleable.OddsBetButton_status,0))

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
    fun setupStatus(isOddsNull: Boolean, status: Int) {
        var itemState = status
        if (isOddsNull) itemState = 2

        when (itemState) {
            0 -> {
                bet_lock.visibility = View.GONE
                bet_layout.isEnabled = true
            }
            1 -> {
                bet_lock.visibility = View.VISIBLE
                bet_layout.isEnabled = false
            }
            2 -> {
                bet_layout.visibility = View.GONE
                bet_layout.isEnabled = false
            }
        }
    }
}