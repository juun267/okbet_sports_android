package org.cxct.sportlottery.ui.sport.list.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.ui.sport.list.SportLeagueAdapter

class SportMatchProvider(private val adapter: SportLeagueAdapter2,
                         private val onOddClick: OnOddClickListener,
                         private val onFavoriteClick: (String) -> Unit,
                         override val itemViewType: Int = 2,
                         override val layoutId: Int = R.layout.item_sport_odd2): BaseNodeProvider() {

    init {
        addChildClickViewIds(R.id.league_odd_match_favorite)
    }

    private val oddBtnCachePool = RecyclerView.RecycledViewPool().apply {
        setMaxRecycledViews(SportLeagueAdapter.ItemType.ITEM.ordinal,150)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return SportMatchVH.of(parent, oddBtnCachePool, onOddClick, adapter.lifecycleOwner, onFavoriteClick)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val binding = (helper as SportMatchVH)
        val matchInfo = (item as MatchOdd).matchInfo
        adapter.holderMatchOdd[helper] = item
        binding.resetStatusView()
        binding.setupMatchInfo(matchInfo, adapter.matchType)
        matchInfo?.let { binding.setupMatchTimeAndStatus(it, adapter.matchType) }
        binding.setupOddsButton(adapter.matchType, item, adapter.oddsType)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode, payloads: List<Any>) {
        val binding = (helper as SportMatchVH)
        val matchOdd = (item as MatchOdd)

        if (payloads.isNullOrEmpty()) {
            updateMatchOdd(binding, matchOdd)
            return
        }

        when(payloads.first()) {
            is SportMatchEvent.OddSelected -> {
                binding.setupOddsButton(adapter.matchType, matchOdd, adapter.oddsType)
            }

            is SportMatchEvent.FavoriteChanged -> {
                binding.updateFavoriteStatus(matchOdd.matchInfo)
            }

            is SportMatchEvent.FavoriteChanged -> {
                updateMatchOdd(binding, matchOdd)
            }

            else -> {
                updateMatchOdd(binding, matchOdd)
            }
        }
    }

    private fun updateMatchOdd(binding: SportMatchVH , matchOdd: MatchOdd) {
        val matchInfo = matchOdd.matchInfo
        binding.updateMatchInfo(matchInfo, adapter.matchType)
        matchInfo?.let { binding.setupMatchTimeAndStatus(it, adapter.matchType) }
        binding.setupOddsButton(adapter.matchType, matchOdd, adapter.oddsType)
        binding.setupCsTextLayout(adapter.matchType, matchOdd)
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        (data as MatchOdd).matchInfo?.let { SportDetailActivity.startActivity(view.context, it, adapter.matchType) }
    }


}