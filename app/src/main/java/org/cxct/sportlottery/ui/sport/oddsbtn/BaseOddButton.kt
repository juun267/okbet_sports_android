package org.cxct.sportlottery.ui.sport.oddsbtn

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

abstract class BaseOddButton  @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), AbsOddView {



    override fun resetStatu() {

    }

    override fun onDeactivated() {

    }

    override fun onLock() {

    }

    override fun onRise() {

    }

    override fun onDecrease() {

    }

    override fun onSelected() {

    }

    override fun onUnselected() {

    }



}