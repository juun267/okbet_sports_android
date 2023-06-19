package org.cxct.sportlottery.ui.sport.endscore

import android.util.Log
import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.sport.list.adapter.SportMatchEvent
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setTeamLogo

class EndScoreSecondProvider(val adapter: EndScoreAdapter,
                             val onItemClick:(Int, View, BaseNode) -> Unit,
                             override val itemViewType: Int = 2,
                             override val layoutId: Int = R.layout.item_endscore_battle): BaseNodeProvider() {

    override fun convert(helper: BaseViewHolder, item: BaseNode, payloads: List<Any>) = helper.run {
        if (payloads.isEmpty()) {
            return@run
        }

        val matchInfo = (item as MatchOdd).matchInfo
        if (payloads.first() is SportMatchEvent.FavoriteChanged) {
            getView<View>(R.id.league_odd_match_favorite).isSelected = matchInfo?.isFavorite ?: false
        }
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) = helper.run {
        helper.itemView.tag = item
        val matchInfo = (item as MatchOdd).matchInfo
        setText(R.id.tvHomeName, matchInfo?.homeName)
        setText(R.id.tvAwayName, matchInfo?.awayName)
        setText(R.id.league_odd_match_time, TimeUtil.timeFormat(matchInfo?.startTime, TimeUtil.DM_HM_FORMAT))
        getView<ImageView>(R.id.ivHomeLogo).setTeamLogo(matchInfo?.homeIcon)
        getView<ImageView>(R.id.ivAwayLogo).setTeamLogo(matchInfo?.awayIcon)
        getView<View>(R.id.lin_match).setOnClickListener {
            onItemClick.invoke(helper.bindingAdapterPosition,
                it,
                item)
        }

        getView<View>(R.id.league_odd_match_favorite).run {
            isSelected = matchInfo?.isFavorite ?: false
            setOnClickListener { onItemClick.invoke(helper.bindingAdapterPosition, this, item) }
        }

        getView<View>(R.id.tvExpand).setOnClickListener { adapter.expandOrCollapse(item, parentPayload = item) }

    }

}