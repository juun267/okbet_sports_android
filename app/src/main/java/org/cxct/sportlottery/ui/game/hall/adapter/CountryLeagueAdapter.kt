package org.cxct.sportlottery.ui.game.hall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_country_league.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.league.League

class CountryLeagueAdapter : RecyclerView.Adapter<CountryLeagueAdapter.ViewHolder>() {

    var data = listOf<League>()
        set(value) {
            field = value

            notifyDataSetChanged()
        }

    var countryLeagueListener: CountryLeagueListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, countryLeagueListener)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: League, countryLeagueListener: CountryLeagueListener?) {
            itemView.country_league_name.text = item.name
            itemView.country_league_odd_count.text = item.num.toString()

            itemView.setOnClickListener {
                countryLeagueListener?.onClick(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_country_league, parent, false)

                return ViewHolder(view)
            }
        }
    }
}

class CountryLeagueListener(val clickListener: (item: League) -> Unit) {
    fun onClick(item: League) = clickListener(item)
}