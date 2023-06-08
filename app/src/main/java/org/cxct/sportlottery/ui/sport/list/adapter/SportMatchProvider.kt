package org.cxct.sportlottery.ui.sport.list.adapter


import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.sport.list.SportLeagueAdapter

class SportMatchProvider(private val adapter: SportLeagueAdapter2,
                         private val onItemClick:(Int, View, BaseNode) -> Unit,
                         private val onFavoriteChanged: (MatchInfo) -> Unit,
                         override val itemViewType: Int = 2,
                         override val layoutId: Int = R.layout.item_sport_odd2): BaseNodeProvider() {

    private val oddBtnCachePool = RecyclerView.RecycledViewPool().apply { setMaxRecycledViews(
        SportLeagueAdapter.ItemType.ITEM.ordinal,150) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return SportMatchVH.of(parent, oddBtnCachePool, onItemClick, adapter.lifecycleOwner)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val binding = (helper as SportMatchVH)
        val matchInfo = (item as MatchOdd).matchInfo
        binding.resetStatusView()
        binding.setupMatchInfo(matchInfo, adapter.matchType)
        matchInfo?.let { binding.setupMatchTimeAndStatus(it, adapter.matchType) }
        binding.setupOddsButton(adapter.matchType, item, adapter.oddsType)
    }


    override fun convert(helper: BaseViewHolder, item: BaseNode, payloads: List<Any>) {
        val binding = (helper as SportMatchVH)
        val matchInfo = (item as MatchOdd).matchInfo
        binding.updateMatchInfo(matchInfo, adapter.matchType)
        matchInfo?.let { binding.setupMatchTimeAndStatus(it, adapter.matchType) }
        binding.updateOddsButton(item, adapter.oddsType)
        binding.setupCsTextLayout(adapter.matchType, item)
    }


}