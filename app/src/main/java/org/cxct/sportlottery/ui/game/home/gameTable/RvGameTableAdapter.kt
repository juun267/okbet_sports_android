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
import org.cxct.sportlottery.network.common.MatchType
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
                is ItemViewHolder -> {
                    when (data.matchType) {
                        MatchType.IN_PLAY -> viewHolder.bindInPlay(data)
                        MatchType.TODAY -> viewHolder.bindToday(data)
                    }
                }
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
        mTimerMap.clear()
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

        fun bindInPlay(data: GameEntity) {
            itemView.apply {
                tv_score1.text = data.matchStatusCO?.homeTotalScore?.toString() ?: "-"
                tv_score2.text = data.matchStatusCO?.awayTotalScore?.toString() ?: "-"

                team1.text = data.match?.homeName
                team2.text = data.match?.awayName

                tv_session.text = data.matchStatusCO?.statusName

                when (data.code) {
                    "FT" -> { //足球
                        showTime(data.matchClockCO?.matchTime)
                        startFTTimer(data.matchClockCO)
                    }
                    "BK" -> { //籃球
                        showTime(data.matchClockCO?.remainingTimeInPeriod)
                        startBKTimer(data.matchClockCO)
                    }
                }

                if (data.itemType == ItemType.FOOTER) {
                    line_item.visibility = View.GONE
                    card_footer.visibility = View.VISIBLE
                    line_footer.visibility = if (adapterPosition == mDataList.lastIndex) View.GONE else View.VISIBLE
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

        fun bindToday(data: GameEntity) {
            itemView.apply {
                tv_score1.text = "–"
                tv_score2.text = "–"

                team1.text = data.match?.homeName
                team2.text = data.match?.awayName

                showStartTime(data.match?.startTime)

                if (data.itemType == ItemType.FOOTER) {
                    line_item.visibility = View.GONE
                    card_footer.visibility = View.VISIBLE
                    line_footer.visibility = if (adapterPosition == mDataList.lastIndex) View.GONE else View.VISIBLE
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

        private fun showStartTime(startTime: Long?) {
            itemView.apply {
                startTime?.let {
                    tv_session.text = TimeUtil.timeFormat(it, "MM/dd")
                    tv_time.text = TimeUtil.timeFormat(it, "HH:mm")
                }
            }
        }

        private fun showTime(sec: Int?) {
            itemView.tv_time.text = if (sec == null)
                null
            else
                TimeUtil.timeFormat(sec * 1000L, "mm:ss")
        }

        private fun startFTTimer(matchClockCO: MatchClockCO?) {
            //足球每秒時間往上加
            startTimer {
                matchClockCO?.matchTime = matchClockCO?.matchTime?.let { it + 1 }
                showTime(matchClockCO?.matchTime)
            }
        }

        private fun startBKTimer(matchClockCO: MatchClockCO?) {
            //籃球每秒時間倒數
            startTimer {
                matchClockCO?.remainingTimeInPeriod = matchClockCO?.remainingTimeInPeriod?.let { it - 1 }
                showTime(matchClockCO?.remainingTimeInPeriod)
            }
        }

        private fun startTimer(timerTask: () -> Unit) {
            mTimerMap[adapterPosition]?.cancel()
            stopTimer()
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    Handler(Looper.getMainLooper()).post {
                        timerTask()
                    }
                }
            }, 1000L, 1000L)
            mTimerMap[adapterPosition] = timer
        }

        fun stopTimer() {
            timer?.cancel()
        }
    }

}