package org.cxct.sportlottery.ui.thirdGame

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.setPadding
import kotlinx.android.synthetic.main.menu_motion_floating.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.clickDelay

/**
 * @author Hewie
 * Modified by Simon Change 2021/02/03
 * 漂浮式選單
 */

class MotionFloatingMenu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : MotionLayout(context, attrs, defStyle) {

    private var mOnMenuListener: OnMenuListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.menu_motion_floating, this, true)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //竖屏
        } else {
            //横屏
            fun dp2px(dpValue: Float): Int {
                val scale = Resources.getSystem().displayMetrics.density
                return (dpValue * scale + 0.5f).toInt()
            }

            val dp60 = dp2px(60f)
            val dp50 = dp2px(50f)
            val dp30 = dp2px(30f)
            val dp20 = dp2px(20f)
            val lpn = menu_button.layoutParams
            lpn.width = dp50
            lpn.height = dp50
            menu_button.layoutParams = lpn
            val cs = motion_layout.getConstraintSet(R.id.open)
            fun changeWidgetSize(padding: Float, size: Int, vId: View) {
                vId.setPadding(dp2px(padding))
                cs.constrainWidth(vId.id, size)
                cs.constrainHeight(vId.id, size)
            }
            cs.setMargin(R.id.motion_back_home, 4, dp30)
            cs.setMargin(R.id.motion_cash_save, 6, dp20)
            cs.setMargin(R.id.motion_cash_save, 3, dp30)
            cs.setMargin(R.id.motion_cash_get, 3, dp30)
            changeWidgetSize(14f, dp60, motion_back_home)
            changeWidgetSize(14f, dp60, motion_cash_save)
            changeWidgetSize(14f, dp60, motion_cash_get)
            changeWidgetSize(14f, dp60, motion_close)
        }

        //使用盤開啟狀態隱藏，充值提現按鈕
        motion_layout.getConstraintSet(R.id.start)
            .getConstraint(motion_cash_save.id).propertySet.mVisibilityMode =
            1 // 1 - ignore or 0 - normal
        motion_layout.getConstraintSet(R.id.open)
            .getConstraint(motion_cash_save.id).propertySet.mVisibilityMode =
            1 // 1 - ignore or 0 - normal
        motion_layout.getConstraintSet(R.id.start)
            .getConstraint(motion_cash_get.id).propertySet.mVisibilityMode =
            1 // 1 - ignore or 0 - normal
        motion_layout.getConstraintSet(R.id.open)
            .getConstraint(motion_cash_get.id).propertySet.mVisibilityMode =
            1 // 1 - ignore or 0 - normal

        motion_back_home.setOnClickListener {
            mOnMenuListener?.onHome()
        }

        motion_cash_save.setOnClickListener {
            mOnMenuListener?.onCashSave()
        }

        motion_cash_get.clickDelay {
            mOnMenuListener?.onCashGet()
        }
    }

    fun setOnMenuListener(onMenuListener: OnMenuListener?) {
        mOnMenuListener = onMenuListener
    }

    interface OnMenuListener {
        fun onHome()
        fun onCashSave()
        fun onCashGet()
    }
}