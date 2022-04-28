package org.cxct.sportlottery.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.service.order_settlement.SportBet
import org.cxct.sportlottery.network.service.order_settlement.Status
import org.cxct.sportlottery.util.ArithUtil

class NotificationView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val rootLayout: ConstraintLayout by lazy { findViewById(R.id.cl_root) }
    private val ivIcon: ImageView by lazy { findViewById(R.id.iv_broadcast_icon) }
    private val tvMessage: TextView by lazy { findViewById(R.id.tv_notification) }

    companion object {
        private const val SHOW_TIME: Long = 3000 //訊息顯示持續時間
        private const val DELAY_TIME: Long = 1000 //訊息間間隔時間
        private const val ANIMATION_DURATION: Long = 1000 //動畫所需時間
    }

    private val notificationList = mutableListOf<NotificationData>()

    //動畫開始時顯示, 結束後隱藏
    private val animationStartListener = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
            rootLayout.visibility = View.VISIBLE
        }

        override fun onAnimationEnd(animation: Animation?) {
        }

        override fun onAnimationRepeat(animation: Animation?) {
        }
    }
    private val animationEndListener = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            rootLayout.visibility = View.INVISIBLE
            postDelayed(notificationRunnable, DELAY_TIME)
        }

        override fun onAnimationRepeat(animation: Animation?) {
        }
    }

    private val notificationRunnable = Runnable {
        if (notificationList.size == 0) {
            handlerRunning = false
            return@Runnable
        }
        handlerRunning = true

        getNotification()
        postDelayed({ hideAnimation() }, SHOW_TIME)
    }
    var handlerRunning: Boolean = false //通知訊息是否正在顯示


    data class NotificationData(val orderNo: String?, val grossWin: Double?, val status: Int?)

    init {
        View.inflate(context, R.layout.view_notification, this)
        apply {
            rootLayout.apply {
                visibility = View.INVISIBLE
            }
        }
    }

    private fun getNotification() {
        val notification = notificationList.firstOrNull()
        notificationList.removeFirstOrNull()
        val tailOrderNo = notification?.orderNo?.let { it.substring(it.length - 4, it.length) }

        try {
            when (notification?.status) {
                Status.WIN.code, Status.WIN_HALF.code -> {
                    rootLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.color_317FFF_1053af))
                    ivIcon.setImageResource(R.drawable.ic_good_news)
                    tvMessage.text = String.format(context.getString(R.string.congratulation_win), tailOrderNo, ArithUtil.toMoneyFormat(notification.grossWin))
                    this.requestLayout()
                }
                Status.CANCEL.code -> {
                    rootLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.color_E44438_e44438))
                    ivIcon.setImageResource(R.drawable.ic_warnning_news)
                    tvMessage.text = String.format(context.getString(R.string.warning_cancel), tailOrderNo)
                    this.requestLayout()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        showAnimation()
    }

    fun addNotification(messageData: SportBet) {
        notificationList.add(NotificationData(messageData.orderNo, messageData.grossWin, messageData.status))
        if (!handlerRunning)
            notificationRunnable.run()
    }

    private fun showAnimation() {
        val startAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_top_to_bottom_enter).apply { duration = ANIMATION_DURATION }
        startAnimation.setAnimationListener(animationStartListener)
        rootLayout.startAnimation(startAnimation)
    }

    private fun hideAnimation() {
        val exitAnimation = AnimationUtils.loadAnimation(context, R.anim.push_bottom_to_top_exit).apply { duration = ANIMATION_DURATION }
        exitAnimation.setAnimationListener(animationEndListener)
        rootLayout.startAnimation(exitAnimation)

    }
}