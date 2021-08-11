package org.cxct.sportlottery.ui.game.hall.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_eps_more_odds_info.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.util.TimeUtil

class EpsMoreInfoAdapter: RecyclerView.Adapter<EpsMoreInfoAdapter.ItemViewHolder>()  {

    var dataList = listOf<MatchInfo>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_eps_more_odds_info, parent, false)
                return ItemViewHolder(view)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(matchInfo: MatchInfo){
            itemView.txv_team.text = "${matchInfo.homeName} V ${matchInfo.awayName}"
            itemView.txv_time.text = TimeUtil.stampToDateHM(matchInfo.startTime ?: 0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
       holder.bind(dataList[position])
    }

    override fun getItemCount() = dataList.size
}