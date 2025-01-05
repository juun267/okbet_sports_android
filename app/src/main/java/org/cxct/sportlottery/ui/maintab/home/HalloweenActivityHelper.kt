package org.cxct.sportlottery.ui.maintab.home

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.ConfigRepository
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.loginedRun
import org.cxct.sportlottery.view.floatingbtn.SuckEdgeTouch

class HalloweenActivityHelper(private val container: FrameLayout) {

    private var halloweenView: View? = null
    private var isClosed = false // 手动关闭

    fun bindLifeCycle(lifecycleOwner: LifecycleOwner) {
        ConfigRepository.onNewConfig(lifecycleOwner) {
            enableHalloweenActivity()
            if (it?.configData?.christmasActivity == true) {
                enableHalloweenActivity()
            } else {
                disableHalloweenActivity()
            }
        }
    }

    private fun enableHalloweenActivity() {
        if (isClosed || halloweenView != null) {
            return
        }

        val context = container.context
        val frameLayout = FrameLayout(context)
        halloweenView = frameLayout
        val dp168 = 90.dp
        val lp0 = FrameLayout.LayoutParams(dp168, dp168)
        lp0.gravity = Gravity.END or Gravity.BOTTOM
        lp0.bottomMargin = 260.dp
        container.addView(frameLayout, lp0)

        val img = View(context)
        img.setBackgroundResource(R.drawable.img_halloween_activity)
        val dp80 = 80.dp
        val lp1 = FrameLayout.LayoutParams(dp80, dp80)
        lp1.gravity = Gravity.BOTTOM
        frameLayout.addView(img, lp1)

        val dp24 = 24.dp
        val dp4 = 4.dp
        val ivClose = ImageView(context)
        ivClose.setImageResource(R.drawable.ic_halloween_close)
        ivClose.setPadding(dp4, dp4, dp4, dp4)
        val lp2 = FrameLayout.LayoutParams(dp24, dp24)
        lp2.gravity = Gravity.END
        frameLayout.addView(ivClose, lp2)
        frameLayout.setOnTouchListener(SuckEdgeTouch())
        ivClose.setOnClickListener { disableHalloweenActivity() }
        frameLayout.setOnClickListener { loginedRun(context, true) { jumpToHalloweenActivity() } }
    }

    private fun disableHalloweenActivity() {
        isClosed = true
        if (halloweenView != null) {
            (halloweenView!!.parent as ViewGroup?)?.removeView(halloweenView)
            halloweenView = null
        }
    }

    private fun jumpToHalloweenActivity() {
        val context = container.context
        JumpUtil.toInternalWeb(context, Constants.getChristmasActivityUrl(), context.getString(R.string.P169))
    }

}