package org.cxct.sportlottery.ui.game.common

import android.content.Context
import android.util.AttributeSet
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

            setupLockState(typedArray.getBoolean(R.styleable.OddsBetButton_isLock, false))

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

    private fun setupLockState(isLock: Boolean) {
        when (isLock) {
            true -> {
                bet_lock.visibility = View.VISIBLE
                bet_layout.isEnabled = false
            }
            false -> {
                bet_lock.visibility = View.GONE
                bet_layout.isEnabled = true
            }
        }
    }
}