package org.cxct.sportlottery.ui.game.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.button_odd_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.BetStatus


class OddButtonV4 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    interface OnOddStatusChangedListener {
        fun onOddStateChangedFinish()
    }

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


    init {
        inflate(context, R.layout.button_odd_v4, this)
    }

    private fun setupBetStatus(betStatus: Int) {
        visibility = if (betStatus == BetStatus.DEACTIVATED.code) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }

        odd_lock_v4.visibility =
            if (betStatus == BetStatus.LOCKED.code) {
                View.VISIBLE
            } else {
                View.GONE
            }

        isEnabled = (betStatus == BetStatus.ACTIVATED.code)
    }

    private fun setupOddState(oddState: Int) {}
}