package org.cxct.sportlottery.ui.sport.filter

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.rotationAnimation
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.util.setArrowSpin
import org.cxct.sportlottery.util.setExpandArrow
import org.cxct.sportlottery.util.setLeagueLogo

class FilterLeagueProvider(val adapter: LeagueSelectAdapter,
                           val onItemClick:(Int, View, BaseNode) -> Unit,
                           override val itemViewType: Int = 1,
                           override val layoutId: Int = R.layout.item_filter_league): BaseNodeProvider() {

    override fun convert(helper: BaseViewHolder, item: BaseNode)  {
        val leagueOdd = item as LeagueOdd
        helper.setText(R.id.tvLeagueName, leagueOdd.league.name)
        helper.getView<ImageView>(R.id.ivLogo).setLeagueLogo(leagueOdd.league?.categoryIcon)
        helper.setText(R.id.tvNum,leagueOdd.matchOdds.count { it.isSelected }.toString())
        helper.setText(R.id.tvTotalNum,leagueOdd.matchOdds.size.toString())
        val ivArrow = helper.getView<ImageView>(R.id.ivArrow)
        helper.itemView.isSelected = leagueOdd.isExpanded
        val ivCheck=helper.getView<ImageView>(R.id.ivCheck)
        ivCheck.isSelected = leagueOdd.league.isSelected
        setExpandArrow(ivArrow, leagueOdd.isExpanded)
        ivArrow.setOnClickListener {
            adapter.expandOrCollapse(item)
            helper.itemView.isSelected = leagueOdd.isExpanded
            ivArrow.setArrowSpin(leagueOdd.isExpanded, true) { setExpandArrow(ivArrow, leagueOdd.isExpanded) }
        }
        helper.itemView.setOnClickListener {
            leagueOdd.league.isSelected = !leagueOdd.league.isSelected
            ivCheck.isSelected = leagueOdd.league.isSelected
            onItemClick.invoke(adapter.getItemPosition(item), it, leagueOdd)
        }
    }


}