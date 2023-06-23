package org.cxct.sportlottery.ui.sport.outright

import android.graphics.Typeface
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.outright.odds.CategoryOdds
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.dividerView
import org.cxct.sportlottery.util.setArrowSpin
import org.cxct.sportlottery.util.setExpandArrow

// 冠军列表-玩法
class OutrightSecondProvider(val adapter: SportOutrightAdapter2,
                             val lifecycle: LifecycleOwner,
                             val onItemClick:(Int, View, BaseNode) -> Unit,
                             override val itemViewType: Int = 2,
                             override val layoutId: Int = R.layout.item_outright_type): BaseNodeProvider() {


    private class MatchVH(val name: TextView,
                          val time: TextView,
                          val arrow: ImageView, root: View): BaseViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        val context = parent.context

        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL
        root.addView(context.dividerView(R.color.color_ebf1fc, 1.dp, margins = 12.dp))

        val lin = LinearLayout(context)
        val p10 = 10.dp
        val p12 = 12.dp
        lin.setPadding(p12, 0, 2.dp, 0)
        lin.gravity = Gravity.CENTER_VERTICAL
        lin.minimumHeight = 40.dp
        root.addView(lin, LinearLayout.LayoutParams(-1, -2))

        val name = AppCompatTextView(context)
        name.maxLines = 2
        name.textSize = 12f
        name.typeface = Typeface.DEFAULT_BOLD
        name.setPadding(0, p10, 0, p10)
        name.setTextColor(ContextCompat.getColor(context, R.color.color_0D2245))
        val nameParam = LinearLayout.LayoutParams(0, -2, 1f)
        nameParam.rightMargin = p12
        lin.addView(name, nameParam)

        val time = AppCompatTextView(context)
        time.textSize = 12f
        time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calendar, 0, 0, 0)
        time.setTextColor(ContextCompat.getColor(context, R.color.color_6D7693))
        lin.addView(time, LinearLayout.LayoutParams(-2, -2))

        val arrow = AppCompatImageView(context)
        arrow.setPadding(p10, 0, p10, 0)
        arrow.setImageResource(R.drawable.ic_arrow_gray_up1)
        40.dp.let { lin.addView(arrow, LinearLayout.LayoutParams(it, -2)) }

        return MatchVH(name, time, arrow, root)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val bean = item as CategoryOdds
        val matchVH = helper as MatchVH
        val matchOdd = bean.matchOdd
        helper.itemView.tag = matchOdd
        matchVH.name.text = bean.name
        var startDate = matchOdd?.startDate

        if (TextUtils.isEmpty(startDate)) { //不知道为啥第一条数据一开始的时候会空
            startDate = TimeUtil.timeFormat(matchOdd?.matchInfo?.endTime, TimeUtil.YMDE_FORMAT)
            matchOdd?.startTime = TimeUtil.timeFormat(matchOdd?.matchInfo?.endTime, TimeUtil.HM_FORMAT)
        }
        matchVH.time.text = " ${context.getString(R.string.deadline)}:${startDate} ${matchOdd?.startTime}"
        setExpandArrow(matchVH.arrow, bean.isExpanded)
        matchVH.arrow.setOnClickListener {
            adapter.expandOrCollapse(bean, parentPayload = bean)
            onItemClick.invoke(adapter.getItemPosition(bean), it, bean)
            it.setArrowSpin(bean.isExpanded, true) { setExpandArrow(matchVH.arrow, bean.isExpanded) }
        }
    }


}