package org.cxct.sportlottery.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import org.cxct.sportlottery.R

class NotificationView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    lateinit var rootLayout: ConstraintLayout
    lateinit var ivIcon: ImageView
    lateinit var tvMessage: TextView
    var delayTime: Long = 1500
    private val animationListener = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            rootLayout.visibility = View.GONE
        }

        override fun onAnimationRepeat(animation: Animation?) {
        }
    }

    init {
        View.inflate(context, R.layout.view_notification, this)
        apply {
            rootLayout = findViewById(R.id.cl_root)
            ivIcon = findViewById(R.id.iv_broadcast_icon)
            tvMessage = findViewById(R.id.tv_notification)

            rootLayout.apply {
                visibility = View.GONE
            }
        }
    }

    override fun attachViewToParent(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.attachViewToParent(child, index, params)

    }

    fun addNotification() {
        //TODO Dean : add to list
        pushNotification()
    }

    private fun pushNotification() {
        showAnimation()
        postDelayed({ hideAnimation() }, delayTime)
    }

    private fun showAnimation() {
        val notificationView = LayoutInflater.from(context).inflate(R.layout.view_notification, rootLayout)
        val startAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_top_to_bottom_enter).apply { duration = 1000 }
        rootLayout.visibility = View.VISIBLE
        notificationView.startAnimation(startAnimation)
    }

    private fun hideAnimation() {
        val exitAnimation = AnimationUtils.loadAnimation(context, R.anim.push_bottom_to_top_exit).apply { duration = 1000 }
        exitAnimation.setAnimationListener(animationListener)
        rootLayout.startAnimation(exitAnimation)

    }
}