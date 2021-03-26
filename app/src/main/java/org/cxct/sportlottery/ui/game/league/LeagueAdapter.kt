package org.cxct.sportlottery.ui.game.league

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_league.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.league.Row
import org.cxct.sportlottery.ui.common.DividerItemDecorator

class LeagueAdapter(private val leagueListener: LeagueListener) : RecyclerView.Adapter<LeagueAdapter.ViewHolder>() {
    var data = listOf<Row>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, leagueListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, leagueListener)
    }

    class ViewHolder private constructor(itemView: View, leagueListener: LeagueListener) : RecyclerView.ViewHolder(itemView) {

        private lateinit var leagueSubAdapter: LeagueSubAdapter

        fun bind(item: Row, leagueListener: LeagueListener) {
            itemView.league_name.text = item.name

            setupLeagueSubAdapter(item, leagueListener)
            setupLeagueSubList(item)
            setupLeagueSubExpand(item)
        }

        private fun setupLeagueSubAdapter(item: Row, leagueListener: LeagueListener) {
            leagueSubAdapter = LeagueSubAdapter(LeagueSubAdapter.LeagueSubListener {league ->
                leagueListener.onClick(item.list.find { it == league }?.id ?: "")
            })
        }

        private fun setupLeagueSubList(item: Row) {
            itemView.league_sub_list.apply {
                this.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                this.adapter = leagueSubAdapter
                addItemDecoration(
                    DividerItemDecorator(
                        ContextCompat.getDrawable(context, R.drawable.divider_straight)
                    )
                )
            }

            leagueSubAdapter.data = item.list
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
            fun from(parent: ViewGroup, leagueListener: LeagueListener): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_league, parent, false)

                return ViewHolder(view, leagueListener)
            }
        }
    }
}

class LeagueListener(val clickListener: (item: String) -> Unit) {
    fun onClick(item: String) = clickListener(item)
}