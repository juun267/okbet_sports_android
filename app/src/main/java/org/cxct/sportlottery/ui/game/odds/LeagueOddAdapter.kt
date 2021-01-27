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
import org.cxct.sportlottery.ui.odds.OnMatchOddClickListener

class LeagueOddAdapter(private val onMatchOddClickListener: OnMatchOddClickListener) : RecyclerView.Adapter<LeagueOddAdapter.ViewHolder>() {
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

    var matchOddListener: MatchOddListener? = null

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, onMatchOddClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, playType, matchOddListener)
    }

    class ViewHolder private constructor(itemView: View, private val onMatchOddClickListener: OnMatchOddClickListener) : RecyclerView.ViewHolder(itemView) {
        private val matchOddAdapter by lazy {
            MatchOddAdapter(onMatchOddClickListener)
        }

        fun bind(item: LeagueOdd, playType: PlayType, matchOddListener: MatchOddListener?) {
            itemView.league_odd_name.text = item.league.name
            itemView.league_odd_count.text = item.matchOdds.size.toString()

            setupMatchOddList(item, playType, matchOddListener)
            setupMatchOddExpand(item)
        }

        private fun setupMatchOddList(
            item: LeagueOdd,
            playType: PlayType,
            matchOddListener: MatchOddListener?
        ) {
            itemView.league_odd_sub_list.apply {
                this.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                this.adapter = matchOddAdapter
            }

            matchOddAdapter.data = item.matchOdds
            matchOddAdapter.playType = playType
            matchOddAdapter.matchOddListener = matchOddListener
        }

        private fun setupMatchOddExpand(item: LeagueOdd) {
            itemView.league_odd_sub_expand.setExpanded(item.isExpand, false)
            itemView.setOnClickListener {
                item.isExpand = !item.isExpand
                itemView.league_odd_sub_expand.setExpanded(item.isExpand, true)
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
            fun from(parent: ViewGroup, onMatchOddClickListener: OnMatchOddClickListener): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_league_odd, parent, false)

                return ViewHolder(view, onMatchOddClickListener)
            }
        }
    }
}