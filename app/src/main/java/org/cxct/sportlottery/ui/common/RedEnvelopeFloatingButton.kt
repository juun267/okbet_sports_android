package org.cxct.sportlottery.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import kotlinx.android.synthetic.main.motion_view_red_envelope_floating.view.*
import kotlinx.android.synthetic.main.motion_view_service_floating.view.*
import kotlinx.android.synthetic.main.motion_view_service_floating.view.movable_layout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.util.JumpUtil

class RedEnvelopeFloatingButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) :
    MotionLayout(context, attributeSet, defStyle) {


    init {
        addView(
            LayoutInflater.from(context)
                .inflate(R.layout.motion_view_red_envelope_floating, this, false)
        )
        initClickEvent()
    }

    var goneClose = true
    private fun initClickEvent() {
        iv_close.setOnClickListener {
            visibility = View.GONE
            goneClose = false
        }
    }


    fun setView(gone: Boolean) {
        if (gone) {
            if (goneClose) {
                visibility = View.VISIBLE
            }
        } else {
            visibility = View.GONE
            goneClose = true
        }

    }

    fun setCountdown(countdown: Long) {
        tv_countdown.text = countdown.toString()
    }

}