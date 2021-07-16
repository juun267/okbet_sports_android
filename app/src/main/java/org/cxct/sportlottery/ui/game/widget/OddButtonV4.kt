package org.cxct.sportlottery.ui.game.widget

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import org.cxct.sportlottery.R


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

    private fun setupBetStatus(betStatus: Int) {}

    private fun setupOddState(oddState: Int) {}
}