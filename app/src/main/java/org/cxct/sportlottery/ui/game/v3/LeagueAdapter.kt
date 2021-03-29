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
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.util.ItemNonLastDecoration

class LeagueAdapter(private val matchType: MatchType) :
    RecyclerView.Adapter<LeagueAdapter.ViewHolder>() {

    var data = listOf<LeagueOdd>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var playType: PlayType = PlayType.OU_HDP
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var leagueOddListener: LeagueOddListener? = null

    var itemExpandListener: ItemExpandListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(matchType, parent).apply {

            this.itemView.league_odd_list.apply {
                this.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

                addItemDecoration(
                    ItemNonLastDecoration(
                        ContextCompat.getDrawable(
                            context, R.drawable.divider_straight
                        )
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, playType, leagueOddListener, itemExpandListener)
    }

    override fun getItemCount(): Int = data.size


    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)

        holder.itemView.league_odd_list.adapter = null
    }

    class ViewHolder private constructor(matchType: MatchType, itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val leagueOddAdapter by lazy {
            LeagueOddAdapter(matchType)
        }

        fun bind(
            item: LeagueOdd,
            playType: PlayType,
            leagueOddListener: LeagueOddListener?,
            itemExpandListener: ItemExpandListener?
        ) {
            itemView.league_name.text = item.league.name
            itemView.league_odd_count.text = item.matchOdds.size.toString()

            setupLeagueOddList(item, playType, leagueOddListener)
            setupLeagueOddExpand(item, itemExpandListener)
        }

        private fun setupLeagueOddList(
            item: LeagueOdd,
            playType: PlayType,
            leagueOddListener: LeagueOddListener?
        ) {
            itemView.league_odd_list.apply {
                adapter = leagueOddAdapter.apply {
                    data = if (item.searchMatchOdds.isNotEmpty()) {
                        item.searchMatchOdds
                    } else {
                        item.matchOdds
                    }

                    this.playType = playType
                    this.leagueOddListener = leagueOddListener
                }
            }
        }

        private fun setupLeagueOddExpand(item: LeagueOdd, itemExpandListener: ItemExpandListener?) {
            itemView.league_odd_expand.setExpanded(item.isExpand, false)

            itemView.setOnClickListener {
                item.isExpand = !item.isExpand
                itemView.league_odd_expand.setExpanded(item.isExpand, true)
                leagueOddAdapter.isTimerEnable = item.isExpand
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

        companion object {
            fun from(matchType: MatchType, parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_game_league, parent, false)

                return ViewHolder(matchType, view)
            }
        }
    }
}

class ItemExpandListener(val expandListener: (leagueOdd: LeagueOdd) -> Unit) {
    fun onItemExpand(leagueOdd: LeagueOdd) = expandListener(leagueOdd)
}