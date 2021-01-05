package org.cxct.sportlottery.ui.game.odds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_league_odd.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.LeagueOdd

class LeagueOddAdapter : RecyclerView.Adapter<LeagueOddAdapter.ViewHolder>() {
    var data = listOf<LeagueOdd>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item)
    }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val matchOddAdapter by lazy {
            MatchOddAdapter()
        }

        fun bind(item: LeagueOdd) {
            itemView.league_odd_name.text = item.league.name
            itemView.league_odd_count.text = item.matchOdds.size.toString()
            itemView.league_odd_sub_expand.setExpanded(item.isExpand, false)
            itemView.setOnClickListener {
                item.isExpand = !item.isExpand
                itemView.league_odd_sub_expand.setExpanded(item.isExpand, true)
            }

            setupMatchOddList(item)
        }

        private fun setupMatchOddList(item: LeagueOdd) {
            itemView.league_odd_sub_list.apply {
                this.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                this.adapter = matchOddAdapter
            }

            matchOddAdapter.data = item.matchOdds
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