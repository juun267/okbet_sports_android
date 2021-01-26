package org.cxct.sportlottery.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.cxct.sportlottery.R

class OrderStatusSelectorComponent @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : FrameLayout(context, attrs, defStyle) {
    init {
        addView(LayoutInflater.from(context).inflate(R.layout.component_order_status_selector, null))
    }
}