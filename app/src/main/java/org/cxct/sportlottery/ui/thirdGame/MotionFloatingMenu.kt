package org.cxct.sportlottery.ui.thirdGame

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.setPadding
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.databinding.MenuMotionFloatingBinding
import splitties.systemservices.layoutInflater

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
    private val binding by lazy { MenuMotionFloatingBinding.inflate(layoutInflater,this,true) }

    override fun onAttachedToWindow()=binding.run {
        super.onAttachedToWindow()
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
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
            val lpn = menuButton.layoutParams
            lpn.width = dp50
            lpn.height = dp50
            menuButton.layoutParams = lpn
            val cs = motionLayout.getConstraintSet(R.id.open)
            fun changeWidgetSize(padding: Float, size: Int, vId: View) {
                vId.setPadding(dp2px(padding))
                cs.constrainWidth(vId.id, size)
                cs.constrainHeight(vId.id, size)
            }
            cs.setMargin(R.id.motion_back_home, 4, dp30)
            cs.setMargin(R.id.motion_cash_save, 6, dp20)
            cs.setMargin(R.id.motion_cash_save, 3, dp30)
            cs.setMargin(R.id.motion_cash_get, 3, dp30)
            changeWidgetSize(14f, dp60, motionBackHome)
            changeWidgetSize(14f, dp60, motionCashSave)
            changeWidgetSize(14f, dp60, motionCashGet)
            changeWidgetSize(14f, dp60, motionClose)
        }

        //使用盤開啟狀態隱藏，充值提現按鈕
        motionLayout.getConstraintSet(R.id.start)
            .getConstraint(motionCashSave.id).propertySet.mVisibilityMode =
            1 // 1 - ignore or 0 - normal
        motionLayout.getConstraintSet(R.id.open)
            .getConstraint(motionCashSave.id).propertySet.mVisibilityMode =
            1 // 1 - ignore or 0 - normal
        motionLayout.getConstraintSet(R.id.start)
            .getConstraint(motionCashGet.id).propertySet.mVisibilityMode =
            1 // 1 - ignore or 0 - normal
        motionLayout.getConstraintSet(R.id.open)
            .getConstraint(motionCashGet.id).propertySet.mVisibilityMode =
            1 // 1 - ignore or 0 - normal

        motionBackHome.setOnClickListener {
            mOnMenuListener?.onHome()
        }

        motionCashSave.setOnClickListener {
            mOnMenuListener?.onCashSave()
        }

        motionCashGet.clickDelay {
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