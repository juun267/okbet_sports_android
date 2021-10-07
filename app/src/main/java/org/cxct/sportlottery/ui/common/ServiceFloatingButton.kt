package org.cxct.sportlottery.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.motion.widget.MotionLayout
import kotlinx.android.synthetic.main.motion_view_service_floating.view.*
import org.cxct.sportlottery.R

class ServiceFloatingButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) :
    MotionLayout(context, attributeSet, defStyle) {

    var serviceFloatingListener: ServiceFloatingListener? = null

    init {
        addView(LayoutInflater.from(context).inflate(R.layout.motion_view_service_floating, this, false))
        initClickEvent()
    }

    private fun initClickEvent(){
        iv_service.setOnClickListener { serviceFloatingListener?.serviceClick() }
    }

    class ServiceFloatingListener(private val buttonClick: () -> Unit) {
        fun serviceClick() = buttonClick.invoke()
    }
}