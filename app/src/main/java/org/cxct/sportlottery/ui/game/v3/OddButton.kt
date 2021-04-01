package org.cxct.sportlottery.ui.game.v3

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.button_odd.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.OUType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState
import java.lang.Exception


class OddButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    interface OnOddStatusChangedListener {
        fun onOddStateChangedFinish()
    }

    var playType: PlayType? = null
        set(value) {
            field = value

            field?.let {
                setupPlayType(it)
            }
        }

    var ouType: OUType? = null
        set(value) {
            field = value

            field?.let {
                setupOUType(it)
            }
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

    var onOddStatusChangedListener: OnOddStatusChangedListener? = null

    private val mHandler by lazy {
        Handler()
    }

    private var runnable: Runnable? = null

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        inflate(context, R.layout.button_odd, this)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.OddButton)

        try {

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    private fun setupPlayType(playType: PlayType) {
        odd_hdp_top_text.visibility = if (playType == PlayType.HDP) {
            View.VISIBLE
        } else {
            View.GONE
        }

        odd_hdp_bottom_text.visibility = if (playType == PlayType.HDP) {
            View.VISIBLE
        } else {
            View.GONE
        }

        odd_ou_type.visibility = if (playType == PlayType.OU) {
            View.VISIBLE
        } else {
            View.GONE
        }

        odd_ou_top_text.visibility = if (playType == PlayType.OU) {
            View.VISIBLE
        } else {
            View.GONE
        }

        odd_ou_bottom_text.visibility = if (playType == PlayType.OU) {
            View.VISIBLE
        } else {
            View.GONE
        }

        odd_1x2_top_text.visibility = if (playType == PlayType.X12) {
            View.VISIBLE
        } else {
            View.GONE
        }

        odd_1x2_bottom_text.visibility = if (playType == PlayType.X12) {
            View.VISIBLE
        } else {
            View.GONE
        }

        odd_outright_text.visibility = if (playType == PlayType.OUTRIGHT) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun setupOUType(ouType: OUType) {
        odd_ou_type.text = when (ouType) {
            OUType.O_TYPE -> {
                resources.getString(R.string.odd_button_ou_o)
            }
            OUType.U_TYPE -> {
                resources.getString(R.string.odd_button_ou_u)
            }
        }
    }

    private fun setupBetStatus(betStatus: Int) {
        visibility = if (betStatus == BetStatus.DEACTIVATED.code) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }

        odd_lock.visibility =
            if (betStatus == BetStatus.LOCKED.code) {
                View.VISIBLE
            } else {
                View.GONE
            }

        isEnabled = (betStatus == BetStatus.ACTIVATED.code)
    }

    private fun setupOddState(oddState: Int) {
        if (!isEnabled) return

        when (oddState) {
            OddState.LARGER.state -> {
                odd_button.background =
                    ContextCompat.getDrawable(context, R.drawable.shape_button_odd_bg_green)
            }
            OddState.SMALLER.state -> {
                odd_button.background =
                    ContextCompat.getDrawable(context, R.drawable.shape_button_odd_bg_red)
            }
            else -> return
        }

        runnable = Runnable {
            odd_button.background =
                ContextCompat.getDrawable(context, R.drawable.shape_button_odd_bg)

            onOddStatusChangedListener?.onOddStateChangedFinish()
        }

        runnable?.let {
            mHandler.postDelayed(
                it, 3000
            )
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        runnable?.let {
            mHandler.removeCallbacks(it)
        }
    }
}