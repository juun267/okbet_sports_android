package org.cxct.sportlottery.ui.game.hall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_country_league_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.outright.season.Season

class OutrightCountryLeagueAdapter :
    RecyclerView.Adapter<OutrightCountryLeagueAdapter.ViewHolder>() {

    var data = listOf<Season>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var outrightCountryLeagueListener: OutrightCountryLeagueListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, outrightCountryLeagueListener)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Season, outrightCountryLeagueListener: OutrightCountryLeagueListener?) {
            itemView.apply {
                country_league_choose.visibility = View.GONE
                country_league_name.text = item.name
                country_league_count.text = item.num.toString()
                country_league_pin.setOnClickListener {
                    outrightCountryLeagueListener?.onClickPin(item.id)
                }

                setOnClickListener {
                    outrightCountryLeagueListener?.onClick(item)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_country_league_v4, parent, false)

                return ViewHolder(view)
            }
        }
    }
}

class OutrightCountryLeagueListener(
    val clickListener: (item: Season) -> Unit,
    val clickListenerPin: (leagueId: String?) -> Unit
) {
    fun onClick(item: Season) = clickListener(item)
    fun onClickPin(leagueId: String?) = clickListenerPin(leagueId)
}