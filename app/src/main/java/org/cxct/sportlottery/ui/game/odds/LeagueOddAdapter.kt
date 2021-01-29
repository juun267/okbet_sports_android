package org.cxct.sportlottery.ui.game.odds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_league_odd.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.Odd

class LeagueOddAdapter : RecyclerView.Adapter<LeagueOddAdapter.ViewHolder>() {
    var data = listOf<LeagueOdd>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var updatedOddsMap = mapOf<String, List<Odd>>()
        set(value) {
            field = value
            notifyDataSetChanged() //TODO Cheryl: 優化 -> 只更新展開的item
        }

    var playType: PlayType = PlayType.OU_HDP
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var matchOddListener: MatchOddListener? = null

    var itemExpandListener: ItemExpandListener? = null

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, updatedOddsMap, playType, matchOddListener, itemExpandListener)
    }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val matchOddAdapter by lazy {
            MatchOddAdapter()
        }

        fun bind(item: LeagueOdd, updatedOddsMap: Map<String, List<Odd>>, playType: PlayType, matchOddListener: MatchOddListener?, itemExpandListener: ItemExpandListener?) {
            itemView.league_odd_name.text = item.league.name
            itemView.league_odd_count.text = item.matchOdds.size.toString()

            setupMatchOddList(item, updatedOddsMap, playType, matchOddListener)
            setupMatchOddExpand(item, adapterPosition, itemExpandListener)
        }

        private fun setupMatchOddList(
            item: LeagueOdd,
            updatedOddsMap: Map<String, List<Odd>>,
            playType: PlayType,
            matchOddListener: MatchOddListener?,
        ) {
            itemView.league_odd_sub_list.apply {
                this.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                this.adapter = matchOddAdapter
            }
/*

            if (item.matchOdds.size < 2)  {
                for (i in 0 until (2 - item.matchOdds.size)) {
                    item.matchOdds.add(MatchOdd())
                }
            }
*/

            matchOddAdapter.data = item.matchOdds
            matchOddAdapter.playType = playType
            matchOddAdapter.matchOddListener = matchOddListener
            matchOddAdapter.updatedOddsMap = updatedOddsMap
        }
/*
        fun updateMatchOddList(updatedOddsMap: Map<String, List<Odd>>) {
            matchOddAdapter.updatedOddsMap = updatedOddsMap
        }
*/

        private fun setupMatchOddExpand(item: LeagueOdd, position: Int, itemExpandListener: ItemExpandListener?) {
            itemView.league_odd_sub_expand.setExpanded(item.isExpand, false)
            itemView.setOnClickListener {
                item.isExpand = !item.isExpand
                itemView.league_odd_sub_expand.setExpanded(item.isExpand, true)
                itemExpandListener?.onItemExpand(item.isExpand, item, position)
                updateArrowExpand()
            }
        }

        private fun updateArrowExpand() {
            when (itemView.league_odd_sub_expand.isExpanded) {
                true -> itemView.league_odd_arrow.setImageResource(R.drawable.ic_arrow_dark)
                false -> itemView.league_odd_arrow.setImageResource(R.drawable.ic_arrow_down_dark)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_league_odd, parent, false)

                return ViewHolder(view)
            }
        }
    }
}

class ItemExpandListener(val clickListener: (isExpand: Boolean, leagueOdd: LeagueOdd, position: Int) -> Unit) {
    fun onItemExpand(isExpand: Boolean, leagueOdd: LeagueOdd, position: Int) = clickListener(isExpand, leagueOdd, position)
}