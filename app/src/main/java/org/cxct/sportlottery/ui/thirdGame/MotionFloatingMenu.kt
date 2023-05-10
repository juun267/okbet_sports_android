package org.cxct.sportlottery.ui.thirdGame

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.motion.widget.MotionLayout
import kotlinx.android.synthetic.main.menu_motion_floating.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.setVisibilityByCreditSystem

/**
 * @author Hewie
 * Modified by Simon Change 2021/02/03
 * 漂浮式選單
 */

class MotionFloatingMenu @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : MotionLayout(context, attrs, defStyle) {

    private var mOnMenuListener: OnMenuListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.menu_motion_floating, this, true)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        //使用盤開啟狀態隱藏，充值提現按鈕
        motion_layout.getConstraintSet(R.id.start).getConstraint(motion_cash_save.id).propertySet.mVisibilityMode = 1 // 1 - ignore or 0 - normal
        motion_layout.getConstraintSet(R.id.open).getConstraint(motion_cash_save.id).propertySet.mVisibilityMode = 1 // 1 - ignore or 0 - normal
        motion_cash_save.setVisibilityByCreditSystem()
        motion_layout.getConstraintSet(R.id.start).getConstraint(motion_cash_get.id).propertySet.mVisibilityMode = 1 // 1 - ignore or 0 - normal
        motion_layout.getConstraintSet(R.id.open).getConstraint(motion_cash_get.id).propertySet.mVisibilityMode = 1 // 1 - ignore or 0 - normal
        motion_cash_get.setVisibilityByCreditSystem()

        motion_back_home.setOnClickListener {
            mOnMenuListener?.onHome()
        }

        motion_cash_save.setOnClickListener {
            mOnMenuListener?.onCashSave()
        }

        motion_cash_get.setOnClickListener {
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