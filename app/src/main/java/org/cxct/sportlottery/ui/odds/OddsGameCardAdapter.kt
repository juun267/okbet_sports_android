package org.cxct.sportlottery.ui.odds

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_odds_detail_game_card.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.util.TimeUtil
import java.util.*

class OddsGameCardAdapter(private val clickListener: ItemClickListener) :
    RecyclerView.Adapter<OddsGameCardAdapter.ViewHolder>() {
    private var mSelectedPosition = 0
    private val mTimerMap = mutableMapOf<Int, Timer?>()

    var data: MutableList<MatchInfo?> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater
            .inflate(R.layout.content_odds_detail_game_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        item?.let { holder.bind(it) }
        holder.itemView.isSelected = mSelectedPosition == position
        holder.gameCard.setOnClickListener {
            if (position != mSelectedPosition) {
                mSelectedPosition = position
                notifyDataSetChanged()
            }
            item?.let { it1 -> clickListener.onClick(it1) }
        }
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var timer: Timer? = null

        private val homeScore: TextView = itemView.findViewById(R.id.txv_home_score)
        private val homeName: TextView = itemView.findViewById(R.id.txv_home_name)

        private val awayScore: TextView = itemView.findViewById(R.id.txv_away_score)
        private val awayName: TextView = itemView.findViewById(R.id.txv_away_name)

        private val txvTime: TextView = itemView.findViewById(R.id.txv_time)
        val gameCard: LinearLayout = itemView.findViewById(R.id.ll_game_card)


        fun bind(item: MatchInfo) {
            homeScore.text = (item.homeScore ?: 0).toString()
            homeName.text = item.homeName

            awayScore.text = (item.awayScore ?: 0).toString()
            awayName.text = item.awayName

            if(item.startTime.isNotEmpty()&& item.endTime?.isNotEmpty() == true){
                footBasketballTime(item.startTime.toLong(),item.startTime.toLong())
            }else if(item.startTime.isNotEmpty()){
                footBallTime(item.startTime.toLong())
            }else{
                itemView.txv_time.text =""
            }
        }

        //足球 累積時間
        private fun footBallTime(starTime: Long?) {
            mTimerMap[adapterPosition]?.cancel()
            stopTimer()

            val currentTime = System.currentTimeMillis()
            if (starTime == null) {
                txvTime.text = null
            } else {
                var timeMillis = (currentTime - starTime) * 1000L
                itemView.txv_time.text = TimeUtil.timeFormat(timeMillis, "HH:mm:ss")

                timer = Timer()
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            timeMillis += 1000
                            itemView.txv_time.text = TimeUtil.timeFormat(timeMillis, "HH:mm:ss")
                        }
                    }
                }, 1000L, 1000L)
            }

            mTimerMap[layoutPosition] = timer
        }

        //籃球 倒數時間
        private fun footBasketballTime(starTime: Long?, endTime: Long?) {
            mTimerMap[layoutPosition]?.cancel()
            stopTimer()

            if (starTime == null || endTime == null) {
                txvTime.text = null
            } else {
                var timeMillis = (endTime.minus(starTime)).times(1000L)
                itemView.txv_time.text = TimeUtil.timeFormat(timeMillis, "HH:mm:ss")

                timer = Timer()
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            timeMillis = timeMillis.minus(1000)
                            itemView.txv_time.text = TimeUtil.timeFormat(timeMillis, "HH:mm:ss")
                        }
                    }
                }, 1000L, 1000L)
            }

            mTimerMap[adapterPosition] = timer
        }

        fun stopTimer() {
            timer?.cancel()
        }
    }

    class ItemClickListener(private val clickListener: (oddsData: MatchInfo) -> Unit) {
        fun onClick(oddsData: MatchInfo) = clickListener(oddsData)
    }


}