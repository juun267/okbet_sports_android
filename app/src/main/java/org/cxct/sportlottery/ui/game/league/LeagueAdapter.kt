package org.cxct.sportlottery.ui.game.league

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_league.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.league.Row

class LeagueAdapter : RecyclerView.Adapter<LeagueAdapter.ViewHolder>() {
    var data = listOf<Row>()
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
        private val leagueSubAdapter by lazy {
            LeagueSubAdapter()
        }

        fun bind(item: Row) {
            itemView.league_name.text = item.name
            itemView.league_sub_expand.setExpanded(item.isExpand, false)
            itemView.setOnClickListener {
                item.isExpand = !item.isExpand
                itemView.league_sub_expand.setExpanded(item.isExpand, true)
            }

            setupLeagueSubList(item)
        }

        private fun setupLeagueSubList(item: Row) {
            itemView.league_sub_list.apply {
                this.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                this.adapter = leagueSubAdapter
            }

            leagueSubAdapter.data = item.list
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_league, parent, false)

                return ViewHolder(view)
            }
        }
    }
}