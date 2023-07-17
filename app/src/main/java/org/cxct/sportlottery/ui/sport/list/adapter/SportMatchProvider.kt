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
                         override val layoutId: Int = 0): BaseNodeProvider() {

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
        helper.itemView.tag = item
        binding.resetStatusView()
        binding.setupMatchInfo(matchInfo, adapter.matchType)
        matchInfo?.let { binding.setupMatchTimeAndStatus(it, adapter.matchType) }
        binding.setupOddsButton(adapter.matchType, item, adapter.oddsType)
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        (data as MatchOdd).matchInfo?.let { SportDetailActivity.startActivity(view.context, matchInfo = it, matchType = adapter.matchType) }
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode, payloads: List<Any>) {
        if (payloads.isNullOrEmpty()) {
            return
        }

        val binding = (helper as SportMatchVH)
        val matchOdd = (item as MatchOdd)

        payloads.forEach {
            when(it) {
                is SportMatchEvent.OddsChanged -> {
                    binding.setupOddsButton(adapter.matchType, matchOdd, adapter.oddsType)
                }
                is SportMatchEvent.OddSelected -> {
                    binding.setupOddsButton(adapter.matchType, matchOdd, adapter.oddsType)
                }

                is SportMatchEvent.FavoriteChanged -> {
                    binding.updateFavoriteStatus(matchOdd.matchInfo)
                }

                is SportMatchEvent.MatchStatuChanged -> {
                    updateMatchStatus(binding, matchOdd)
                }

                else -> {
                }
            }
        }


    }

    private fun updateMatchStatus(binding: SportMatchVH , matchOdd: MatchOdd) {
        matchOdd.matchInfo?.let {
            binding.setupMatchTimeAndStatus(it, adapter.matchType)
            binding.bindOTStatus(it)
        }
        binding.setupMatchScore(matchOdd.matchInfo, adapter.matchType)
    }

    private fun updateMatchOdd(binding: SportMatchVH , matchOdd: MatchOdd) {
        val matchInfo = matchOdd.matchInfo
        binding.updateMatchInfo(matchInfo, adapter.matchType)
        matchInfo?.let { binding.setupMatchTimeAndStatus(it, adapter.matchType) }
        binding.setupOddsButton(adapter.matchType, matchOdd, adapter.oddsType)
        binding.setupCsTextLayout(adapter.matchType, matchOdd)
    }


}