package org.cxct.sportlottery.ui.game.outright

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_league_odd.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.outright.odds.LeagueOdd


class OutrightOddAdapter : RecyclerView.Adapter<OutrightOddAdapter.ViewHolder>() {
    var data = listOf<LeagueOdd>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var matchOddListener: MatchOddAdapter.MatchOddListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, matchOddListener)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var matchOddAdapter: MatchOddAdapter

        fun bind(item: LeagueOdd, matchOddListener: MatchOddAdapter.MatchOddListener?) {
            itemView.league_odd_name.text = item.league.name
            itemView.league_odd_count.text = item.matchOdds.size.toString()

            setupMatchOddAdapter(matchOddListener)
            setupMatchOddList(item)
            setupMatchOddExpand(item)
        }

        private fun setupMatchOddAdapter(matchOddListener: MatchOddAdapter.MatchOddListener?) {
            matchOddAdapter = MatchOddAdapter(matchOddListener)
        }

        private fun setupMatchOddList(item: LeagueOdd) {
            itemView.league_odd_sub_list.apply {
                this.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                this.adapter = matchOddAdapter
            }

            matchOddAdapter.data = item.matchOdds
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
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_league_odd, parent, false)

                return ViewHolder(view)
            }
        }
    }
}