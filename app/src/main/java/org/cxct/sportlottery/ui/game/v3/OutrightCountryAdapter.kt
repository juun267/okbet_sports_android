package org.cxct.sportlottery.ui.game.v3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_country.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.outright.season.Row
import org.cxct.sportlottery.ui.common.SocketLinearManager

class OutrightCountryAdapter : RecyclerView.Adapter<OutrightCountryAdapter.ViewHolder>() {

    var data = listOf<Row>()
        set(value) {
            field = value

            notifyDataSetChanged()
        }

    var outrightCountryLeagueListener: OutrightCountryLeagueListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent).apply {

            this.itemView.league_list.apply {
                this.layoutManager =
                    SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)

                this.addItemDecoration(
                    DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, outrightCountryLeagueListener)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val countryLeagueAdapter by lazy {
            OutrightCountryLeagueAdapter()
        }

        fun bind(item: Row, outrightCountryLeagueListener: OutrightCountryLeagueListener?) {
            itemView.country_name.text = item.name

            setupLeagueList(item, outrightCountryLeagueListener)
            setupCountryExpand(item)
        }

        private fun setupLeagueList(
            item: Row,
            outrightCountryLeagueListener: OutrightCountryLeagueListener?
        ) {
            itemView.league_list.apply {
                adapter = countryLeagueAdapter.apply {
                    this.outrightCountryLeagueListener = outrightCountryLeagueListener
                    data = item.list
                }
            }
        }

        private fun setupCountryExpand(item: Row) {
            itemView.country_league_expand.setExpanded(item.isExpand, false)
            itemView.setOnClickListener {
                item.isExpand = !item.isExpand
                itemView.country_league_expand.setExpanded(item.isExpand, true)
                updateArrowExpand()
            }
        }

        private fun updateArrowExpand() {
            when (itemView.country_league_expand.isExpanded) {
                true -> itemView.country_arrow.setImageResource(R.drawable.ic_arrow_dark)
                false -> itemView.country_arrow.setImageResource(R.drawable.ic_arrow_down_dark)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_country, parent, false)

                return ViewHolder(view)
            }
        }
    }
}