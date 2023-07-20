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
import org.cxct.sportlottery.util.dividerView
import org.cxct.sportlottery.util.setArrowSpin
import org.cxct.sportlottery.util.setExpandArrow
import org.cxct.sportlottery.util.setLeagueLogo
import splitties.views.dsl.core.add

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
        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundResource(R.color.color_FCFDFF)
        root.foreground = ContextCompat.getDrawable(context, R.drawable.fg_ripple)
        root.addView(context.dividerView(R.color.color_ebf1fc, 1.dp))

        val lin = LinearLayout(context)
        lin.gravity = Gravity.CENTER_VERTICAL
        lin.layoutParams = LinearLayout.LayoutParams(-1, 50.dp)
        root.addView(lin)

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
        44.dp.let { lin.addView(arrow, LinearLayout.LayoutParams(it, it)) }

        return LeagueVH(logo, name, arrow, root)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode)  {
        val matchOdd = item as MatchOdd
        val leagueVH = helper as LeagueVH
        leagueVH.logo.setLeagueLogo(matchOdd.matchInfo?.categoryIcon)
        leagueVH.name.text = matchOdd.matchInfo?.name
        setExpandArrow(leagueVH.arrow, matchOdd.isExpanded)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode, payloads: List<Any>) {
        if (payloads.getOrNull(0) is OutrightFirstProvider) {
            return
        }

        val matchOdd = item as MatchOdd
        val leagueVH = helper as LeagueVH
        setExpandArrow(leagueVH.arrow, matchOdd.isExpanded)
    }


    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        val matchOdd = data as MatchOdd
        val leagueVH = helper as LeagueVH
        val position = adapter.getItemPosition(data)
        adapter.expandOrCollapse(data, parentPayload = this@OutrightFirstProvider)
        leagueVH.arrow.setArrowSpin(matchOdd.isExpanded, true) { setExpandArrow(leagueVH.arrow, matchOdd.isExpanded) }
        onItemClick.invoke(position, leagueVH.arrow, matchOdd)
    }

}