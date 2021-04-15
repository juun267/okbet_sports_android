package org.cxct.sportlottery.ui.game.v3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_game_league.view.*
import kotlinx.android.synthetic.main.itemview_game_league.view.league_odd_count
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.menu.OddsType

class LeagueAdapter(private val matchType: MatchType) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        ITEM, NO_DATA
    }

    var data = listOf<LeagueOdd>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var sportType: SportType? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var playType: PlayType = PlayType.OU_HDP
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var leagueOddListener: LeagueOddListener? = null

    var itemExpandListener: ItemExpandListener? = null

    override fun getItemViewType(position: Int): Int {
        return when {
            data.isEmpty() -> ItemType.NO_DATA.ordinal
            else -> ItemType.ITEM.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM.ordinal -> {
                ItemViewHolder.from(matchType, parent).apply {

                    this.itemView.league_odd_list.apply {
                        this.layoutManager =
                            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

                        addItemDecoration(
                            DividerItemDecorator(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.divider_straight
                                )
                            )
                        )
                    }
                }
            }
            else -> {
                NoDataViewHolder.from(parent)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val item = data[position]

                holder.bind(
                    item,
                    matchType,
                    sportType,
                    playType,
                    leagueOddListener,
                    itemExpandListener,
                    oddsType
                )
            }
        }
    }

    override fun getItemCount(): Int = if (data.isEmpty()) {
        1
    } else {
        data.size
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        when (holder) {
            is ItemViewHolder -> {
                holder.itemView.league_odd_list.adapter = null
            }
        }
    }

    class ItemViewHolder private constructor(matchType: MatchType, itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val leagueOddAdapter by lazy {
            LeagueOddAdapter(matchType)
        }

        fun bind(
            item: LeagueOdd,
            matchType: MatchType,
            sportType: SportType?,
            playType: PlayType,
            leagueOddListener: LeagueOddListener?,
            itemExpandListener: ItemExpandListener?,
            oddsType: OddsType
        ) {
            itemView.league_name.text = item.league.name
            itemView.league_odd_count.text = item.matchOdds.size.toString()

            setupLeagueOddList(item, sportType, playType, leagueOddListener, oddsType)
            setupLeagueOddExpand(item, matchType, sportType, itemExpandListener)
        }

        private fun setupLeagueOddList(
            item: LeagueOdd,
            sportType: SportType?,
            playType: PlayType,
            leagueOddListener: LeagueOddListener?,
            oddsType: OddsType
        ) {
            itemView.league_odd_list.apply {
                adapter = leagueOddAdapter.apply {
                    data = if (item.searchMatchOdds.isNotEmpty()) {
                        item.searchMatchOdds
                    } else {
                        item.matchOdds
                    }

                    this.playType = playType
                    this.isTimerDecrease = (sportType == SportType.BASKETBALL)
                    this.leagueOddListener = leagueOddListener
                    this.oddsType = oddsType
                }
            }
        }

        private fun setupLeagueOddExpand(
            item: LeagueOdd,
            matchType: MatchType,
            sportType: SportType?,
            itemExpandListener: ItemExpandListener?
        ) {
            itemView.league_odd_expand.setExpanded(item.isExpand, false)
            updateTimer(matchType, sportType)
            updateArrowExpand()

            itemExpandListener?.onItemExpand(item)

            itemView.setOnClickListener {
                item.isExpand = !item.isExpand
                itemView.league_odd_expand.setExpanded(item.isExpand, true)
                updateTimer(matchType, sportType)
                updateArrowExpand()

                itemExpandListener?.onItemExpand(item)
            }
        }

        private fun updateArrowExpand() {
            when (itemView.league_odd_expand.isExpanded) {
                true -> itemView.league_arrow.setImageResource(R.drawable.ic_arrow_dark)
                false -> itemView.league_arrow.setImageResource(R.drawable.ic_arrow_down_dark)
            }
        }

        private fun updateTimer(matchType: MatchType, sportType: SportType?) {
            leagueOddAdapter.isTimerEnable =
                itemView.league_odd_expand.isExpanded && (sportType == SportType.FOOTBALL || sportType == SportType.BASKETBALL || matchType == MatchType.AT_START)
        }

        companion object {
            fun from(matchType: MatchType, parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.itemview_game_league, parent, false)

                return ItemViewHolder(matchType, view)
            }
        }
    }

    class NoDataViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        companion object {
            fun from(parent: ViewGroup): NoDataViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_game_no_record, parent, false)

                return NoDataViewHolder(view)
            }
        }
    }
}

class ItemExpandListener(val expandListener: (leagueOdd: LeagueOdd) -> Unit) {
    fun onItemExpand(leagueOdd: LeagueOdd) = expandListener(leagueOdd)
}