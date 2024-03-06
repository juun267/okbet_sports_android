package org.cxct.sportlottery.ui.sport.endcard.bet

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable

class EndCardOddsAdapter: BaseQuickAdapter<String, BaseViewHolder>(0) {

    private val defaultBg by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_1E2535))
            .setRadius(8.dp.toFloat())
            .setWidth(88.dp)
            .setHeight(44.dp)
    }

    private val disableBg by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_353B4E))
            .setRadius(8.dp.toFloat())
            .setWidth(88.dp)
            .setHeight(44.dp)
    }

    private val selectedBg by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_025BE8))
            .setRadius(8.dp.toFloat())
            .setWidth(88.dp)
            .setHeight(44.dp)
    }

    private val oddId = View.generateViewId()
    private val userId = View.generateViewId()
    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val cxt = parent.context
        val root = LinearLayout(cxt)
        root.gravity = Gravity.CENTER
        root.orientation = LinearLayout.VERTICAL
        val dp3 = 3.dp
        root.setPadding(dp3, dp3, dp3, dp3)
        val dp4 = 4.dp
        val rootLp = LinearLayout.LayoutParams(-1, 44.dp)
//        rootLp.leftMargin = dp4
//        rootLp.topMargin = dp3
//        rootLp.rightMargin = dp4
//        rootLp.bottomMargin = dp3
        root.layoutParams = rootLp

        val lp = LinearLayout.LayoutParams(-2, 0, 1f)

        val odd = AppCompatTextView(cxt)
        odd.id = oddId
        odd.textSize = 16f
        odd.typeface = AppFont.inter_bold
        odd.setTextColor(Color.WHITE)
        odd.gravity = Gravity.CENTER
        root.addView(odd, lp)

        val user = AppCompatTextView(cxt)
        user.id = userId
        user.textSize = 12f
        user.setTextColor(cxt.getColor(R.color.color_BEC7DC))
        user.gravity = Gravity.CENTER
        root.addView(user, lp)

        return BaseViewHolder(root)
    }

    override fun convert(holder: BaseViewHolder, item: String) {
        val oddText = holder.getView<TextView>(oddId)
        val userText = holder.getView<TextView>(userId)
        oddText.text = item

        if (item.startsWith("3")) {
            userText.gone()

        } else {
            userText.text = "army"
            userText.visible()
        }

        if (item.startsWith("2")) {
            holder.itemView.background = disableBg
        } else if (item.startsWith("4")) {
            holder.itemView.background = selectedBg
        } else if (item.startsWith("5")) {
            holder.itemView.setBackgroundResource(R.drawable.bg_selected_endodd)
        } else {
            holder.itemView.background = defaultBg
        }
    }
}