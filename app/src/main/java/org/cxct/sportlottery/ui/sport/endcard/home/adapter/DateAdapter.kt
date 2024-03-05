package org.cxct.sportlottery.ui.sport.endcard.home.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable

class DateAdapter: BaseQuickAdapter<String, BaseViewHolder>(0) {

    private val selectedColor by lazy { context.getColor(R.color.color_6AA4FF) }
    private val unSelectedColor by lazy { context.getColor(R.color.color_6D7693) }
    private val weekId = View.generateViewId()
    private val dateId = View.generateViewId()
    private val selectedDrawable by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_353B4E))
            .setRadius(8.dp.toFloat())
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val frameLayout = FrameLayout(context)
        val lp = LinearLayout.LayoutParams(60.dp, 44.dp)
        lp.gravity = Gravity.CENTER_VERTICAL
        lp.leftMargin = 12.dp
        lp.topMargin = 10.dp
        frameLayout.layoutParams = lp

        val week = AppCompatTextView(context)
        week.gravity = Gravity.CENTER_HORIZONTAL
        week.id = weekId
        week.textSize = 12f
        val weekLP = FrameLayout.LayoutParams(-1, -2)
        weekLP.topMargin = 3.dp
        frameLayout.addView(week, weekLP)

        val date = AppCompatTextView(context)
        date.gravity = Gravity.CENTER_HORIZONTAL
        date.id = dateId
        date.textSize = 14f
        date.setTextColor(Color.WHITE)
        val dateLP = FrameLayout.LayoutParams(-1, -2)
        dateLP.bottomMargin = 3.dp
        dateLP.gravity = Gravity.BOTTOM
        frameLayout.addView(date, dateLP)

        return BaseViewHolder(frameLayout)
    }

    override fun convert(holder: BaseViewHolder, item: String) {
        val isSelected = item == "Mon"
        val week = holder.getView<TextView>(weekId)
        val date = holder.getView<TextView>(dateId)
        week.text = item
        date.text = item
        if (isSelected) {
            week.setTextColor(selectedColor)
            holder.itemView.background = selectedDrawable
        } else {
            week.setTextColor(unSelectedColor)
            holder.itemView.background = null
        }

    }
}