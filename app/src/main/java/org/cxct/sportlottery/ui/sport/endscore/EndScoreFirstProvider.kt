package org.cxct.sportlottery.ui.sport.endscore

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.util.setArrowSpin
import org.cxct.sportlottery.util.setExpandArrow
import org.cxct.sportlottery.util.setLeagueLogo

class EndScoreFirstProvider(val adapter: EndScoreAdapter,
                            val onItemClick:(Int, View, BaseNode) -> Unit,
                            override val itemViewType: Int = 1,
                            override val layoutId: Int = R.layout.item_endscore_group): BaseNodeProvider() {

    override fun convert(helper: BaseViewHolder, item: BaseNode, payloads: List<Any>) {
        if (payloads.getOrNull(0) is EndScoreFirstProvider) {
            return
        }
        showExpand(helper, item as LeagueOdd)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode)  {
        val league = item as LeagueOdd
        helper.setText(R.id.tv_league_name, league.league.name)
        helper.getView<ImageView>(R.id.iv_league_logo).setLeagueLogo(league.league.categoryIcon)
        showExpand(helper, league)
    }

    override fun onClick(helper: BaseViewHolder, view: View, item: BaseNode, position: Int) {
        val position = adapter.getItemPosition(item)
        adapter.expandOrCollapse(item, parentPayload = this@EndScoreFirstProvider)
        val league = item as LeagueOdd
        val ivArrow = helper.getView<ImageView>(R.id.iv_league_arrow)
        ivArrow.setArrowSpin(league.isExpanded, true) {
            showExpand(helper, league)
        }
        onItemClick.invoke(position, view, league)
    }
   private fun showExpand(helper: BaseViewHolder, league: LeagueOdd){
       val ivArrow = helper.getView<ImageView>(R.id.iv_league_arrow)
       val tvNum = helper.getView<TextView>(R.id.tvNum)
       setExpandArrow(ivArrow, league.isExpanded)
       ivArrow.isVisible = league.isExpanded
       tvNum.isVisible = !league.isExpanded
       tvNum.text = "${league.matchOdds.size}"
   }

}