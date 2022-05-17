package org.cxct.sportlottery.ui.game.outright

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_outright_league_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.results.OutrightType
import org.cxct.sportlottery.util.MatchOddUtil.updateOddsDiscount

class OutrightLeagueOddAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

    var discount: Float = 1.0F
        set(value) {
            data.forEach { matchOdd ->
                matchOdd?.oddsMap?.updateOddsDiscount(field, value)
            }

            field = value
            notifyDataSetChanged()
        }

    var outrightOddListener: OutrightOddListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //return LeagueOddViewHolder.from(parent)
        return when (viewType) {
            OutrightType.OUTRIGHT.ordinal -> {
                LeagueOddViewHolder.from(parent)
            }
            OutrightType.BOTTOM_NAVIGATION.ordinal -> {
                BottomNavigationViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.home_bottom_navigation, parent, false)
                )
            }
            else -> NoDataViewHolder.from(parent)
        }
    }

//    override fun onBindViewHolder(holder: LeagueOddViewHolder, position: Int) {
//        holder.bind(data[position], oddsType, outrightOddListener)
//    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LeagueOddViewHolder -> {
                holder.bind(data[position], oddsType, outrightOddListener)
            }
        }
    }

    override fun getItemCount(): Int = if (data.isEmpty()) {
        2
    } else {
        data.size+1
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            data.isEmpty() -> {
                if(position == 0 ){
                    OutrightType.NO_DATA.ordinal
                }else{
                    OutrightType.BOTTOM_NAVIGATION.ordinal
                }
            }
            position == (data.size) ->{
                OutrightType.BOTTOM_NAVIGATION.ordinal
            }
            else -> {
                OutrightType.OUTRIGHT.ordinal
            }
        }
    }

    class LeagueOddViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val outrightOddAdapter by lazy {
            OutrightOddAdapter()
        }

        fun bind(
            matchOdd: MatchOdd?,
            oddsType: OddsType,
            outrightOddListener: OutrightOddListener?
        ) {

            itemView.outright_league_name.text = matchOdd?.matchInfo?.name

            itemView.outright_league_date.text = matchOdd?.startDate ?: ""

            itemView.outright_league_time.text = matchOdd?.startTime ?: ""

            itemView.outright_league_odd_list.apply {
                adapter = outrightOddAdapter.apply {
                    this.matchOdd = matchOdd
                    this.oddsType = oddsType
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
    class NoDataViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(parent: ViewGroup): NoDataViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_game_no_record, parent, false)

                return NoDataViewHolder(view)
            }
        }
    }
    class BottomNavigationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}