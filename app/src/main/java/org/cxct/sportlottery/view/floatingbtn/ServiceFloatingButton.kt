package org.cxct.sportlottery.view.floatingbtn

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.doOnResume
import org.cxct.sportlottery.common.extentions.post
import org.cxct.sportlottery.ui.common.WebActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.ui.splash.LaunchActivity
import org.cxct.sportlottery.ui.splash.SplashActivity
import org.cxct.sportlottery.ui.thirdGame.ThirdGameActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.setServiceClick

object ServiceFloatingButton {

    private var keepLeft = true
    private var yPoint = 0f

    fun targetActivity(activity: AppCompatActivity) {
        if (!needShowService(activity)) {
            return
        }

        post {
            if (activity.isDestroyed) {
                return@post
            }

            val serviceBtn = AppCompatImageView(activity)
            serviceBtn.setImageResource(R.drawable.ic_service_blue)

            val wh = 56.dp
            val layoutParams = FrameLayout.LayoutParams(wh, wh)
            if (yPoint == 0f) {
                layoutParams.gravity = Gravity.BOTTOM
                layoutParams.bottomMargin = 80.dp
                activity.addContentView(serviceBtn, layoutParams)
            } else {
                activity.addContentView(serviceBtn, layoutParams)
                serviceBtn.y = yPoint
                if (keepLeft) {
                    serviceBtn.x = 0f
                } else {
                    serviceBtn.x = ((serviceBtn.parent as View).width - wh).toFloat()
                }
            }

            serviceBtn.setOnTouchListener(SuckEdgeTouch(onSide = { keepLeft = it },onMove = { yPoint = it}))
            serviceBtn.setServiceClick(activity.supportFragmentManager)

            activity.doOnResume(interval = 0) {
                serviceBtn.y = yPoint
                if (keepLeft) {
                    serviceBtn.x = 0f
                } else {
                    serviceBtn.x = ((serviceBtn.parent as View).width - wh).toFloat()
                }
            }
        }
    }

    private fun needShowService(activity: AppCompatActivity): Boolean {
        return activity !is SplashActivity
                && activity !is MaintenanceActivity
                && activity !is LaunchActivity
                && activity !is ThirdGameActivity
                && activity !is WebActivity<*,*>
    }
}
