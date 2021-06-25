package org.cxct.sportlottery.ui.game.home.gameTable4

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.home_game_table_item_4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.service.match_clock.MatchClockCO
import org.cxct.sportlottery.util.TimeUtil
import java.util.*

class Vp2GameTable4Adapter: RecyclerView.Adapter<Vp2GameTable4Adapter.ViewPagerViewHolder>() {

    private var mOnSelectItemListener: OnSelectItemListener<GameBean>? = null
    private var mDataList: List<GameBean> = listOf()
    private val mTimerMap = mutableMapOf<Int, Timer?>()

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        return ViewPagerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.home_game_table_item_4, parent, false)
        )
    }

    override fun onBindViewHolder(@NonNull viewHolder: ViewPagerViewHolder, position: Int) {
        try {
            val data = mDataList[position]
            when (data.matchType) {
                MatchType.IN_PLAY -> viewHolder.bindInPlay(data)
                MatchType.AT_START -> viewHolder.bindAtStart(data)
                else -> {
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onViewRecycled(holder: ViewPagerViewHolder) {
        super.onViewRecycled(holder)

        //當 viewHolder 被回收就 stopTimer
        holder.stopTimer()
    }

    fun setOnSelectItemListener(onSelectItemListener: OnSelectItemListener<GameBean>?) {
        mOnSelectItemListener = onSelectItemListener
    }

    fun setData(dataList: List<GameBean>?) {
        mDataList = dataList ?: listOf()

        notifyDataSetChanged()
    }

    fun stopAllTimer() {
        mTimerMap.forEach {
            val timer = it.value
            timer?.cancel()
        }
        mTimerMap.clear()
    }

    inner class ViewPagerViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var timer: Timer? = null

        @SuppressLint("SetTextI18n")
        fun bindInPlay(data: GameBean) {
            itemView.apply {
                showTeam(data)

                tv_match_status.text = data.matchStatusCO?.statusName

                when (data.code) {
                    SportType.FOOTBALL.code -> { //足球
                        showTime(data.matchClockCO?.matchTime)
                        startFTTimer(data.matchClockCO)
                    }
                    SportType.BASKETBALL.code -> { //籃球
                        showTime(data.matchClockCO?.remainingTimeInPeriod)
                        startBKTimer(data.matchClockCO)
                    }
                    else -> showTime(null)
                }

                showOdds(data)

                setOnClickListener {
                    if (data.match != null)
                        mOnSelectItemListener?.onClick(data)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bindAtStart(data: GameBean) {
            itemView.apply {
                showTeam(data)

                showStartTime(data.match?.startTime)

                showOdds(data)

                setOnClickListener {
                    if (data.match != null)
                        mOnSelectItemListener?.onClick(data)
                }
            }
        }

        private fun showTeam(data: GameBean) {
            itemView.apply {
                when (data.matchType) {
                    MatchType.IN_PLAY -> {
                        tv_game_type.text = context.getString(R.string.home_tab_in_play)

                        tv_score1.visibility = View.VISIBLE
                        tv_score2.visibility = View.VISIBLE
                        val score1 = data.matchStatusCO?.homeTotalScore ?: 0
                        val score2 = data.matchStatusCO?.awayTotalScore ?: 0
                        tv_score1.text = "$score1"
                        tv_score2.text = "$score2"

                        when {
                            score1 > score2 -> {
                                tv_score1.setTypeface(tv_score1.typeface, Typeface.BOLD)
                                tv_score2.setTypeface(tv_score2.typeface, Typeface.NORMAL)
                                tv_team1.setTypeface(tv_team1.typeface, Typeface.BOLD)
                                tv_team2.setTypeface(tv_team2.typeface, Typeface.NORMAL)
                            }
                            score1 < score2 -> {
                                tv_score1.setTypeface(tv_score1.typeface, Typeface.NORMAL)
                                tv_score2.setTypeface(tv_score2.typeface, Typeface.BOLD)
                                tv_team1.setTypeface(tv_team1.typeface, Typeface.NORMAL)
                                tv_team2.setTypeface(tv_team2.typeface, Typeface.BOLD)
                            }
                            else -> {
                                tv_score1.setTypeface(tv_score1.typeface, Typeface.NORMAL)
                                tv_score2.setTypeface(tv_score2.typeface, Typeface.NORMAL)
                                tv_team1.setTypeface(tv_team1.typeface, Typeface.NORMAL)
                                tv_team2.setTypeface(tv_team2.typeface, Typeface.NORMAL)
                            }
                        }

                        tv_team1.text = data.match?.homeName
                        tv_team2.text = data.match?.awayName
                    }
                    MatchType.AT_START -> {
                        tv_game_type.text = context.getString(R.string.home_tab_today)

                        tv_score1.visibility = View.GONE
                        tv_score2.visibility = View.GONE

                        tv_team1.setTypeface(tv_team1.typeface, Typeface.NORMAL)
                        tv_team2.setTypeface(tv_team2.typeface, Typeface.NORMAL)
                        tv_team1.text = data.match?.homeName
                        tv_team2.text = data.match?.awayName
                    }
                    else -> {}
                }
            }
        }

        private fun showOdds(data: GameBean) {
            itemView.apply {
                when (data.code) {
                    SportType.FOOTBALL.code, SportType.BASKETBALL.code  -> {
                        tv_play_type.text = context.getText(R.string.ou_hdp_hdp_title)
                    }
                    else -> {
                        tv_play_type.text = context.getText(R.string.ou_hdp_1x2_title)
                    }
                }
            }
        }

        private fun showStartTime(startTime: Long?) {
            itemView.apply {
                if (startTime == null) {
                    tv_match_status.visibility = View.GONE
                    tv_match_time.visibility = View.GONE
                } else {
                    tv_match_status.visibility = View.VISIBLE
                    tv_match_time.visibility = View.VISIBLE
                    tv_match_status.text = TimeUtil.timeFormat(startTime, "MM/dd")
                    tv_match_time.text = TimeUtil.timeFormat(startTime, "HH:mm")
                }
            }
        }

        private fun showTime(sec: Int?) {
            if (sec == null) {
                itemView.tv_match_time.visibility = View.GONE
            } else {
                itemView.tv_match_time.visibility = View.VISIBLE
                itemView.tv_match_time.text = TimeUtil.timeFormat(sec * 1000L, "mm:ss")
            }
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
                matchClockCO?.remainingTimeInPeriod =
                    matchClockCO?.remainingTimeInPeriod?.let { it - 1 }
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