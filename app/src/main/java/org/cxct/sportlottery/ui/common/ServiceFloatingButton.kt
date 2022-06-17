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
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.util.JumpUtil

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
        movable_layout.setOnClickListener { serviceFloatingListener?.serviceClick() }
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

    fun setView4Maintenance(activity: AppCompatActivity) {
        //2022-02-10 維護頁面常駐客服按鈕
            visibility = View.VISIBLE
            setupClickEvent(activity)
    }

    private fun setupClickEvent(activity: AppCompatActivity) {
        serviceFloatingListener = ServiceFloatingListener {
            val serviceUrl = sConfigData?.customerServiceUrl
            val serviceUrl2 = sConfigData?.customerServiceUrl2
            when {
                !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    ServiceDialog().show(activity?.supportFragmentManager, null)
                }
                serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(activity, serviceUrl2)
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(activity, serviceUrl)
                }
            }
        }
    }
}