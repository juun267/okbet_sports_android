package org.cxct.sportlottery.ui.game.v3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_country.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.league.Row
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.util.SpaceItemDecoration

class CountryAdapter : RecyclerView.Adapter<CountryAdapter.ViewHolder>() {

    var data = listOf<Row>()
        set(value) {
            field = value

            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent).apply {
            setupLeagueList(this)
        }
    }

    private fun setupLeagueList(viewHolder: ViewHolder) {
        viewHolder.itemView.league_list.apply {
            this.layoutManager =
                SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)

            this.addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val countryLeagueAdapter by lazy {
            CountryLeagueAdapter()
        }

        fun bind(item: Row) {
            itemView.country_name.text = item.name

            setupLeagueList(item)
            setupCountryExpand(item)
        }

        private fun setupLeagueList(item: Row) {
            itemView.league_list.adapter = countryLeagueAdapter.apply {
                data = item.list
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