package org.cxct.sportlottery.ui.sport.endscore

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.sport.list.adapter.SportMatchEvent
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
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

        val tvExpand = getView<TextView>(R.id.tvExpand)
        val linExpand = getView<View>(R.id.linExpand)
        resetStyle(linExpand, tvExpand, item.isExpanded)
        linExpand.setOnClickListener {
            adapter.expandOrCollapse(item, parentPayload = item)
            resetStyle(linExpand, tvExpand, item.isExpanded)
        }
    }

    private val expandedDrawable by lazy {
        DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.color_025BE8)
    }

    private val collapseDrawable by lazy {
        DrawableCreatorUtils.getCommonBackgroundStyle(8, android.R.color.white, R.color.color_025BE8, 1)
    }

    private fun resetStyle(linExpand: View, tvExpand: TextView, isExpand: Boolean) = tvExpand.run {
        if (isExpand) {
            setText(R.string.D039)
            setTextColor(ContextCompat.getColor(context, R.color.color_025BE8))
            linExpand.background = collapseDrawable
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_to_up_blue, 0)
        } else {
            setText(R.string.N698)
            setTextColor(Color.WHITE)
            linExpand.background = expandedDrawable
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_to_down_white, 0)
        }
    }

}