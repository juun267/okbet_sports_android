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

class NotificationView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var ivIcon: ImageView
    private lateinit var tvMessage: TextView
    var showTime: Long = 3000 //訊息顯示時間
    var delayTime: Long = 1000 //訊息間間隔時間
    private val notificationList = mutableListOf<NotificationData>()
    private val animationListener = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            rootLayout.visibility = View.INVISIBLE
            postDelayed(notificationRunnable, delayTime)
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
        postDelayed({ hideAnimation() }, showTime)
    }
    var handlerRunning: Boolean = false


    data class NotificationData(val orderNo: String?, val grossWin: Double?, val status: Int?)

    init {
        View.inflate(context, R.layout.view_notification, this)
        apply {
            rootLayout = findViewById(R.id.cl_root)
            ivIcon = findViewById(R.id.iv_broadcast_icon)
            tvMessage = findViewById(R.id.tv_notification)

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
                2, 3 -> {
                    rootLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBlue))
                    ivIcon.setImageResource(R.drawable.ic_good_news)
                    tvMessage.text = String.format(context.getString(R.string.congratulation_win), tailOrderNo, notification.grossWin?.toString() ?: run { "" })
                    this.requestLayout()
                }
                7 -> {
                    rootLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRed))
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
        val startAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_top_to_bottom_enter).apply { duration = 1000 }
        rootLayout.visibility = View.VISIBLE
        rootLayout.startAnimation(startAnimation)
    }

    private fun hideAnimation() {
        val exitAnimation = AnimationUtils.loadAnimation(context, R.anim.push_bottom_to_top_exit).apply { duration = 1000 }
        exitAnimation.setAnimationListener(animationListener)
        rootLayout.startAnimation(exitAnimation)

    }
}