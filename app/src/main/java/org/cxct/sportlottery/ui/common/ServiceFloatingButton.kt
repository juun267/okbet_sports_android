package org.cxct.sportlottery.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import kotlinx.android.synthetic.main.motion_view_service_floating.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.setServiceClick

class ServiceFloatingButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) :
    MotionLayout(context, attributeSet, defStyle) {

    init {
        LayoutInflater.from(context).inflate(R.layout.motion_view_service_floating, this, true)
    }


    class ServiceFloatingListener(private val buttonClick: () -> Unit) {
        fun serviceClick() = buttonClick.invoke()
    }

    fun setView(activity: AppCompatActivity) {
        //2022-01-29 改為 config 控制開關 by Bill
        if (sConfigData?.customerFloating == "1"&&(!sConfigData?.customerServiceUrl.isNullOrBlank()||!sConfigData?.customerServiceUrl2.isNullOrBlank())) {
            visibility = View.VISIBLE
            setupClickEvent(activity)
        } else {
            visibility = View.GONE
        }
    }

    fun setViewAlwaysDisplay(activity: AppCompatActivity) {
        visibility = View.VISIBLE
        setupClickEvent(activity)
    }

    private fun setupClickEvent(activity: AppCompatActivity) {
        movable_layout.setServiceClick(activity.supportFragmentManager)
    }
}