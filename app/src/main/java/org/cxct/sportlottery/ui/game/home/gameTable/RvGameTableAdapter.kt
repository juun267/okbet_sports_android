package org.cxct.sportlottery.ui.game.home.gameTable

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.home_game_table_header.view.*
import kotlinx.android.synthetic.main.home_game_table_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.service.match_clock.MatchClockCO
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusCO
import org.cxct.sportlottery.util.TimeUtil
import java.util.*

class RvGameTableAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mOnSelectItemListener: OnSelectItemListener<GameEntity>? = null
    private var mOnSelectFooterListener: OnSelectItemListener<GameEntity>? = null
    private var mDataList: MutableList<GameEntity> = mutableListOf()
    private val mTimerMap = mutableMapOf<Int, Timer?>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.HEADER.ordinal -> {
                val layout = LayoutInflater.from(viewGroup.context).inflate(R.layout.home_game_table_header, viewGroup, false)
                HeaderViewHolder(layout)
            }

            else -> {
                val layout = LayoutInflater.from(viewGroup.context).inflate(R.layout.home_game_table_item, viewGroup, false)
                ItemViewHolder(layout)
            }
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return mDataList[position].itemType.ordinal
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        try {
            val data = mDataList[position]
            when (viewHolder) {
                is HeaderViewHolder -> viewHolder.bind(data)
                is ItemViewHolder -> viewHolder.bind(data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        //當 viewHolder 被回收就 stopTimer
        if (holder is ItemViewHolder)
            holder.stopTimer()
    }

    fun setOnSelectItemListener(onSelectItemListener: OnSelectItemListener<GameEntity>?) {
        mOnSelectItemListener = onSelectItemListener
    }

    fun setOnSelectFooterListener(onSelectFooterListener: OnSelectItemListener<GameEntity>?) {
        mOnSelectFooterListener = onSelectFooterListener
    }

    fun setData(dataList: MutableList<GameEntity>?) {
        mDataList = dataList ?: mutableListOf()

        notifyDataSetChanged()
    }

    fun setMatchStatusData(matchStatusCO: MatchStatusCO?) {
        mDataList.forEachIndexed { index, gameEntity ->
            if (matchStatusCO?.matchId == gameEntity.match?.id) {
                gameEntity.matchStatusCO = matchStatusCO
                notifyItemChanged(index)
                return
            }
        }
    }

    fun setMatchClockData(matchClockCO: MatchClockCO?) {
        mDataList.forEachIndexed { index, gameEntity ->
            if (matchClockCO?.matchId == gameEntity.match?.id) {
                gameEntity.matchClockCO = matchClockCO
                notifyItemChanged(index)
                return
            }
        }
    }

    fun stopAllTimer() {
        mTimerMap.forEach {
            val timer = it.value
            timer?.cancel()
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: GameEntity) {
            itemView.apply {
                tv_game.text = data.name
            }
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var timer: Timer? = null

        fun bind(data: GameEntity) {
            itemView.apply {
                tv_score1.text = data.matchStatusCO?.homeTotalScore?.toString()
                tv_score2.text = data.matchStatusCO?.awayTotalScore?.toString()

                team1.text = data.match?.homeName
                team2.text = data.match?.awayName

                tv_session.text = data.matchStatusCO?.statusName
                updateTime(data.matchClockCO?.matchTime)

                if (data.itemType == ItemType.FOOTER) {
                    line_item.visibility = View.GONE
                    card_footer.visibility = View.VISIBLE
                    line_footer.visibility = View.VISIBLE
                } else {
                    line_item.visibility = View.VISIBLE
                    card_footer.visibility = View.GONE
                    line_footer.visibility = View.GONE
                }

                tv_footer_title.text = String.format(context.getString(R.string.label_all_something_in_play), data.name)
                tv_footer_count.text = data.num.toString()

                card_item.setOnClickListener {
                    if (data.match != null)
                        mOnSelectItemListener?.onClick(data)
                }

                card_footer.setOnClickListener {
                    mOnSelectFooterListener?.onClick(data)
                }
            }
        }

        private fun updateTime(matchTime: Int?) {
            mTimerMap[adapterPosition]?.cancel()
            stopTimer()
            if (matchTime == null) {
                itemView.tv_time.text = null
            } else {
                var timeMillis = matchTime * 1000L
                itemView.tv_time.text = TimeUtil.timeFormat(timeMillis, "mm:ss")

                timer = Timer()
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            timeMillis += 1000
                            itemView.tv_time.text = TimeUtil.timeFormat(timeMillis, "mm:ss")
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

}