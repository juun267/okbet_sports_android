package org.cxct.sportlottery.ui.game.outright.season

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_league.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.outright.season.Row

class SeasonAdapter : RecyclerView.Adapter<SeasonAdapter.ViewHolder>() {
    var data = listOf<Row>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var seasonSubListener: SeasonSubAdapter.SeasonSubListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, seasonSubListener)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var seasonSubAdapter: SeasonSubAdapter

        fun bind(item: Row, seasonSubListener: SeasonSubAdapter.SeasonSubListener?) {
            itemView.league_name.text = item.name

            setupSeasonAdapter(seasonSubListener)
            setupSeasonSublist(item)
            setupLeagueSubExpand(item)
        }

        private fun setupSeasonAdapter(seasonSubListener: SeasonSubAdapter.SeasonSubListener?) {
            seasonSubAdapter = SeasonSubAdapter(seasonSubListener)
        }

        private fun setupSeasonSublist(item: Row) {
            itemView.league_sub_list.apply {
                this.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                this.adapter = seasonSubAdapter
            }

            seasonSubAdapter.data = item.list
        }

        private fun setupLeagueSubExpand(item: Row) {
            itemView.league_sub_expand.setExpanded(item.isExpand, false)
            itemView.setOnClickListener {
                item.isExpand = !item.isExpand
                itemView.league_sub_expand.setExpanded(item.isExpand, true)
                updateArrowExpand()
            }
        }

        private fun updateArrowExpand() {
            when (itemView.league_sub_expand.isExpanded) {
                true -> itemView.league_arrow.setImageResource(R.drawable.ic_arrow_dark)
                false -> itemView.league_arrow.setImageResource(R.drawable.ic_arrow_down_dark)
            }
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