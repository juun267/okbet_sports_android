package org.cxct.sportlottery.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import org.cxct.sportlottery.R

class ListLoadingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): ConstraintLayout(context, attrs, defStyleAttr) {
    init {
        addView(LayoutInflater.from(context).inflate(R.layout.view_list_loading, this, false))
    }
}