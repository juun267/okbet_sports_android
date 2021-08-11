package org.cxct.sportlottery.ui.game.outright

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_outright_league_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.ui.menu.OddsType

class OutrightLeagueOddAdapter :
    RecyclerView.Adapter<OutrightLeagueOddAdapter.LeagueOddViewHolder>() {

    var data: List<MatchOdd?> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var outrightOddListener: OutrightOddListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueOddViewHolder {
        return LeagueOddViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: LeagueOddViewHolder, position: Int) {
        holder.bind(data[position], outrightOddListener)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class LeagueOddViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val outrightOddAdapter by lazy {
            OutrightOddAdapter(true)
        }

        fun bind(matchOdd: MatchOdd?, outrightOddListener: OutrightOddListener?) {

            itemView.outright_league_name.text = matchOdd?.matchInfo?.name

            itemView.outright_league_date.text = matchOdd?.startDate ?: ""

            itemView.outright_league_time.text = matchOdd?.startTime ?: ""

            itemView.outright_league_odd_list.apply {
                adapter = outrightOddAdapter.apply {
                    this.matchOdd = matchOdd
                    this.outrightOddListener = outrightOddListener
                }
            }

        }

        companion object {
            fun from(parent: ViewGroup): LeagueOddViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_outright_league_v4, parent, false)

                return LeagueOddViewHolder(view)
            }
        }
    }
}