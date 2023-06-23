package org.cxct.sportlottery.ui.sport.search

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.toLongS
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.sport.SearchResponse
import org.cxct.sportlottery.network.sport.SearchResult
import org.cxct.sportlottery.common.adapter.BaseNodeAdapter
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setArrowSpin
import org.cxct.sportlottery.util.setLeagueLogo
import org.cxct.sportlottery.util.setTeamLogo
import org.cxct.sportlottery.view.highLightTextView.HighlightTextView


class SportSearchResultAdapter : BaseNodeAdapter() {

    private var searchKey = ""

    init {
        val block = { searchKey }
        addFullSpanNodeProvider(SportTypeNodeProvider()) // 球类
        addFullSpanNodeProvider(LeagueNodeProvider(this, block)) // 联赛
        addNodeProvider(MatchNodeProvider(block)) //赛事
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is SearchResult -> 1
            is SearchResult.SearchResultLeague -> 2
            else -> 3
        }
    }

    fun getDataCount() = getDefItemCount()

    fun setNewData(key: String, dataList: MutableList<SearchResult>?) {
        searchKey = key
        setNewInstance(dataList as MutableList<BaseNode>?)
    }

    private class SportTypeNodeProvider(
        override val itemViewType: Int = 1,
        override val layoutId: Int = R.layout.item_search_result_sport
    ) : BaseNodeProvider() {

        override fun convert(helper: BaseViewHolder, item: BaseNode) {
            helper.setText(R.id.tvResultTittle, (item as SearchResult).sportTitle)
        }
    }

    private class LeagueNodeProvider(
        val adapter: BaseNodeAdapter,
        val searchKey: () -> String,
        override val itemViewType: Int = 2,
        override val layoutId: Int = R.layout.item_search_result_league
    ) : BaseNodeProvider() {

        override fun convert(helper: BaseViewHolder, item: BaseNode) {
            val tvLeagueTittle = helper.getView<HighlightTextView>(R.id.tvLeagueTittle)
            tvLeagueTittle.setCustomText((item as SearchResult.SearchResultLeague).league)
            tvLeagueTittle.highlight(searchKey())
            var ivIcon = helper.getView<ImageView>(R.id.view1)
            ivIcon.setLeagueLogo(item.icon)
        }

        override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
            val position = adapter.getItemPosition(data)
            adapter.expandOrCollapse(data, parentPayload = position)
            helper.getView<View>(R.id.view_arrow_top).setArrowSpin((data as SearchResult.SearchResultLeague).isExpanded, true)
        }

    }

    private class MatchNodeProvider(
        val searchKey: () -> String,
        override val itemViewType: Int = 3,
        override val layoutId: Int = R.layout.item_search_result_match
    ) : BaseNodeProvider() {

        override fun convert(helper: BaseViewHolder, data: BaseNode) {

            val item = data as SearchResponse.Row.LeagueMatch.MatchInfo
            helper.setText(
                R.id.tv_time, TimeUtil.timeFormat(item.startTime.toLongS(), TimeUtil.YMD_FORMAT)
                        + "\n" + TimeUtil.timeFormat(
                    item.startTime.toLongS(),
                    TimeUtil.HM_FORMAT_SS
                )
            )

            val tvHomeName = helper.getView<HighlightTextView>(R.id.tv_home_name)
            tvHomeName.setCustomText(item.homeName)
            tvHomeName.highlight(searchKey())

            val tvAwayName = helper.getView<HighlightTextView>(R.id.tv_away_name)
            tvAwayName.setCustomText(item.awayName)
            tvAwayName.highlight(searchKey())

            val ivHomeLogo = helper.getView<ImageView>(R.id.ivSRMHomeLogo)
            val ivAwayLogo = helper.getView<ImageView>(R.id.ivSRMAwayLogo)
            ivHomeLogo.setTeamLogo(item.homeIcon)
            ivAwayLogo.setTeamLogo(item.awayIcon)
        }

        override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
            val item = data as SearchResponse.Row.LeagueMatch.MatchInfo
            var matchInfo = MatchInfo(
                id = item.matchId,
                gameType = item.gameType,
                homeName = item.homeName,
                awayName = item.awayName,
                startTime = item.startTime.toLong(),
                endTime = null,
                parlay = null,
                playCateNum = null,
                source = null,
                status = null,
            )

            SportDetailActivity.startActivity(context, matchInfo, null)
        }

    }
}