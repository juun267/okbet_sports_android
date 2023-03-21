package org.cxct.sportlottery.ui.sport.endscore

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.game.widget.OddsOutrightButton
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setTeamLogo

class EndScoreSecondProvider(val adapter: EndScoreAdapter,
                             val onItemClick:(Int, View, BaseNode) -> Unit,
                             override val itemViewType: Int = 2,
                             override val layoutId: Int = R.layout.item_endscore_battle): BaseNodeProvider() {

    override fun convert(helper: BaseViewHolder, item: BaseNode, payloads: List<Any>) = helper.run {
        val matchInfo = (item as MatchOdd).matchInfo
        getView<View>(R.id.league_odd_match_favorite).isSelected = matchInfo?.isFavorite ?: false
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) = helper.run {
        val matchInfo = (item as MatchOdd).matchInfo
        setText(R.id.tvHomeName, matchInfo?.homeName)
        setText(R.id.tvAwayName, matchInfo?.awayName)
        setText(R.id.league_odd_match_time, TimeUtil.timeFormat(matchInfo?.startTime, TimeUtil.DM_HM_FORMAT))
        getView<ImageView>(R.id.ivHomeLogo).setTeamLogo(matchInfo?.homeIcon)
        getView<ImageView>(R.id.ivAwayLogo).setTeamLogo(matchInfo?.awayIcon)
        getView<View>(R.id.llMatchInfo).setOnClickListener { onItemClick.invoke(helper.bindingAdapterPosition, it, item) }
        getView<View>(R.id.league_odd_match_favorite).run {
            isSelected = matchInfo?.isFavorite ?: false
            setOnClickListener { onItemClick.invoke(helper.bindingAdapterPosition, this, item) }
        }

    }

}