package org.cxct.sportlottery.ui.sport.outright

import android.graphics.Typeface
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
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.setArrowSpin
import org.cxct.sportlottery.util.setLeagueLogo

// 冠军列表-联赛名称
class OutrightFirstProvider(val adapter: SportOutrightAdapter2,
                            val lifecycle: LifecycleOwner,
                            val onItemClick:(Int, View, BaseNode) -> Unit,
                            override val itemViewType: Int = 1,
                            override val layoutId: Int = 0): BaseNodeProvider() {

    private class LeagueVH(val logo: ImageView,
                           val name: TextView,
                           val arrow: ImageView, root: View): BaseViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val context = parent.context
        val lin = LinearLayout(context)
        lin.gravity = Gravity.CENTER_VERTICAL
        lin.layoutParams = LinearLayout.LayoutParams(-1, 50.dp)
        lin.foreground = ContextCompat.getDrawable(context, R.drawable.fg_ripple)

        val logo = AppCompatImageView(context)
        val p20 = 20.dp
        val p12 = 12.dp
        val p8 = 8.dp
        val logoParam = LinearLayout.LayoutParams(p20, p20)
        logoParam.leftMargin = p12
        logoParam.rightMargin = p8
        lin.addView(logo, logoParam)

        val name = AppCompatTextView(context)
        name.textSize = 13f
        name.typeface = Typeface.DEFAULT_BOLD
        name.setTextColor(ContextCompat.getColor(context, R.color.color_0D2245))
        lin.addView(name, LinearLayout.LayoutParams(0, -2, 1f))

        val arrow = AppCompatImageView(context)
        arrow.setPadding(p12, p12, p12, p12)
        arrow.setImageResource(R.drawable.ic_arrow_gray_up1)
        44.dp.let { lin.addView(arrow, LinearLayout.LayoutParams(it, it)) }

        return LeagueVH(logo, name, arrow, lin)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode)  {
        val matchOdd = item as MatchOdd
        val leagueVH = helper as LeagueVH
        leagueVH.logo.setLeagueLogo(matchOdd.matchInfo?.categoryIcon)
        leagueVH.name.text = matchOdd.matchInfo?.name
        leagueVH.arrow.setArrowSpin(matchOdd.isExpanded, false)
        setBackground(helper.itemView, matchOdd.isExpanded)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode, payloads: List<Any>) {
        val matchOdd = item as MatchOdd
        val leagueVH = helper as LeagueVH
        leagueVH.arrow.setArrowSpin(matchOdd.isExpanded, false)
        setBackground(helper.itemView, matchOdd.isExpanded)
    }

    private fun setBackground(view: View, isExpanded: Boolean) {
        if (isExpanded) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.color_0D025BE8))
        } else {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.color_F8F9FD))
        }
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        val matchOdd = data as MatchOdd
        val leagueVH = helper as LeagueVH
        val position = adapter.getItemPosition(data)
        adapter.expandOrCollapse(data, parentPayload = data)
        leagueVH.arrow.setArrowSpin(matchOdd.isExpanded, true)
        setBackground(helper.itemView, matchOdd.isExpanded)
        onItemClick.invoke(position, leagueVH.arrow, matchOdd)
    }

}