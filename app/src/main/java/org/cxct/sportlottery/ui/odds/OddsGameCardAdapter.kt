package org.cxct.sportlottery.ui.odds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.util.TimeUtil

class OddsGameCardAdapter(private val clickListener: ItemClickListener) :
    RecyclerView.Adapter<OddsGameCardAdapter.ViewHolder>() {
    private var mSelectedPosition = 0

    var data: MutableList<MatchInfo> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
        holder.itemView.isSelected = mSelectedPosition == position
        holder.gameCard.setOnClickListener {
            if (position != mSelectedPosition) {
                mSelectedPosition = position
                notifyDataSetChanged()
            }
            clickListener.onClick(item)
        }
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        private val homeScore: TextView = itemView.findViewById(R.id.txv_home_score)
        private val homeName: TextView = itemView.findViewById(R.id.txv_home_name)

        private val awayScore: TextView = itemView.findViewById(R.id.txv_away_score)
        private val awayName: TextView = itemView.findViewById(R.id.txv_away_name)

        private val txvTime: TextView = itemView.findViewById(R.id.txv_time)
        val gameCard: LinearLayout = itemView.findViewById(R.id.ll_game_card)


        fun bind(item: MatchInfo) {
            homeScore.text = item.homeScore.toString()
            homeName.text = item.homeName

            awayScore.text = item.awayScore.toString()
            awayName.text = item.awayName

            txvTime.text = TimeUtil.stampToTimeHMS(item.startTime.toLong())//TODO Bill 應該是要倒數

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_odds_detail_game_card, parent, false)
                return ViewHolder(view)
            }
        }
    }

    class ItemClickListener(private val clickListener: (oddsData: MatchInfo) -> Unit) {
        fun onClick(oddsData: MatchInfo) = clickListener(oddsData)
    }


}