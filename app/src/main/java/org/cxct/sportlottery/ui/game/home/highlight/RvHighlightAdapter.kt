package org.cxct.sportlottery.ui.game.home.highlight

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.home_highlight_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.matchCategory.result.OddData
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.home.OnClickFavoriteListener
import org.cxct.sportlottery.ui.game.home.OnClickOddListener
import org.cxct.sportlottery.ui.game.home.OnClickStatisticsListener
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TimeUtil
import java.util.*

class RvHighlightAdapter : RecyclerView.Adapter<RvHighlightAdapter.ViewHolderHdpOu>() {

    private var dataList = listOf<MatchOdd>()
    fun setData(sportCode: String?, newList: List<OddData>?) {
        dataList = newList?.map {
            val matchInfo = MatchInfo(
                gameType = null,
                awayName = it.matchInfo?.awayName ?: "",
                endTime = it.matchInfo?.endTime,
                homeName = it.matchInfo?.homeName ?: "",
                id = it.matchInfo?.id ?: "",
                playCateNum = it.matchInfo?.playCateNum ?: 0,
                startTime = it.matchInfo?.startTime,
                status = it.matchInfo?.status ?: -1).apply {
                gameType = sportCode
                startDateDisplay = TimeUtil.timeFormat(it.matchInfo?.startTime, "MM/dd")
                startTimeDisplay = TimeUtil.timeFormat(it.matchInfo?.startTime, "HH:mm")
            }
            val odds: MutableMap<String, MutableList<Odd?>?> = mutableMapOf()
            it.oddsMap.forEach { odd ->
                odds[odd.key] = odd.value?.toMutableList()
            }
            MatchOdd(matchInfo, odds)
        } ?: listOf()
        notifyDataSetChanged()
    }

    fun getData() = dataList

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    //TODO simon test review 精選賽事是不是一定是 MatchType.TODAY，是的話可以再簡化判斷邏輯
    private var matchType: MatchType = MatchType.TODAY

    var onClickOddListener: OnClickOddListener? = null

    var onClickMatchListener: OnSelectItemListener<MatchOdd>? = null //賽事畫面跳轉

    var onClickFavoriteListener: OnClickFavoriteListener? = null

    var onClickStatisticsListener: OnClickStatisticsListener? = null

    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(dataList.indexOf(dataList.find { matchOdd ->
                    matchOdd.oddsMap.toList()
                        .find { map -> map.second?.find { it == odd } != null } != null
                }))
            }
        }
    }

    private val mTimerMap = mutableMapOf<Int, Timer?>()

    fun stopAllTimer() {
        mTimerMap.forEach {
            val timer = it.value
            timer?.cancel()
        }
        mTimerMap.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderHdpOu {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_highlight_item, parent, false)
        return ViewHolderHdpOu(view)
    }

    override fun onBindViewHolder(holder: ViewHolderHdpOu, position: Int) {
        try {
            val data = dataList[position]
            holder.bind(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = dataList.size

    override fun onViewRecycled(holder: ViewHolderHdpOu) {
        super.onViewRecycled(holder)
        holder.stopTimer()
    }

    inner class ViewHolderHdpOu(itemView: View) : OddStateViewHolder(itemView) {


        private var gameType: String? = null

        private var oddListHDP: MutableList<Odd?>? = null
        private var oddList1x2: MutableList<Odd?>? = null

        private var timer: Timer? = null

        fun bind(data: MatchOdd) {
            setMatchType(data)
            setupOddList(data)
            setupMatchInfo(data)
            setupTime(data)
            setupOddButton(data)

            itemView.iv_match_in_play.visibility =
                if (matchType == MatchType.AT_START) View.VISIBLE else View.GONE

            itemView.iv_match_price.visibility =
                if (data.matchInfo?.eps == 1) View.VISIBLE else View.GONE

            itemView.highlight_match_info.setOnClickListener {
                onClickMatchListener?.onClick(data)
            }

            itemView.tv_match_play_type_count.setOnClickListener {
                onClickMatchListener?.onClick(data)
            }

            itemView.btn_chart.setOnClickListener {
                onClickStatisticsListener?.onClickStatistics(data.matchInfo?.id)
            }
        }

        private fun setMatchType(data: MatchOdd) {
            matchType = if(TimeUtil.getRemainTime(data.matchInfo?.startTime) < 60 * 60 * 1000L) MatchType.AT_START else  MatchType.TODAY
        }

        private fun setupOddList(data: MatchOdd) {
            itemView.apply {
                gameType = data.matchInfo?.gameType

                oddListHDP = when (gameType) {
                    GameType.TN.key -> {
                        data.oddsMap[PlayCate.SET_HDP.value]
                    }
                    GameType.BK.key -> {
                        data.oddsMap[PlayCate.HDP_INCL_OT.value]
                    }
                    else -> {
                        data.oddsMap[PlayCate.HDP.value]
                    }
                }

                oddList1x2 = when (gameType) {
                    GameType.BK.key -> {
                        data.oddsMap[PlayCate.SINGLE_OT.value]
                    }
                    else -> {
                        data.oddsMap[PlayCate.SINGLE.value]
                    }
                }
            }
        }

        private fun setupMatchInfo(data: MatchOdd) {
            itemView.apply {
                tv_game_name_home.text = data.matchInfo?.homeName
                tv_game_name_away.text = data.matchInfo?.awayName
                showStrongTeam()
                tv_match_play_type_count.text = data.matchInfo?.playCateNum?.toString()

                btn_star.apply {
                    this.isSelected = data.matchInfo?.isFavorite ?: false

                    setOnClickListener {
                        onClickFavoriteListener?.onClickFavorite(data.matchInfo?.id)
                    }
                }
            }
        }

        private fun showStrongTeam() {
            itemView.apply {
                tv_game_name_home.apply {
                    setTypeface(
                        this.typeface, if (oddListHDP?.getOrNull(0)?.spread?.contains("-") == true)
                            Typeface.BOLD
                        else
                            Typeface.NORMAL
                    )
                }
                tv_game_name_away.apply {
                    setTypeface(
                        this.typeface, if (oddListHDP?.getOrNull(1)?.spread?.contains("-") == true)
                            Typeface.BOLD
                        else
                            Typeface.NORMAL
                    )
                }
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setupTime(data: MatchOdd) {
            itemView.apply {
                when (matchType) {
                    MatchType.AT_START -> {
                        if (data.matchInfo?.remainTime == null)
                            data.matchInfo?.remainTime = data.matchInfo?.startTime?.minus(
                                System.currentTimeMillis()
                            )
                        data.matchInfo?.remainTime?.let { remainTime ->
                            startTimer((remainTime / 1000).toInt(), true) { timeMillis ->
                                val timeStr = String.format(
                                    itemView.context.resources.getString(R.string.at_start_remain_minute),
                                    TimeUtil.timeFormat(timeMillis, "m")
                                )
                                tv_match_time.text = timeStr

                                data.matchInfo.remainTime = timeMillis
                            }
                        }
                    }

                    else -> {
                        stopTimer()
                        tv_match_time.text = data.matchInfo?.startTimeDisplay ?: ""
                    }
                }
            }
        }

        private fun setupOddButton(data: MatchOdd) {
            itemView.apply {
                gameType = data.matchInfo?.gameType

                val playCateStr = when (gameType) {
                    GameType.FT.key, GameType.BK.key -> context.getText(R.string.ou_hdp_hdp_title)
                    GameType.TN.key, GameType.VB.key -> context.getText(R.string.ou_hdp_1x2_title)
                    else -> ""
                }.toString()

                btn_match_odd1.apply {
                    isSelected = when (gameType) {
                        GameType.FT.key, GameType.BK.key -> {
                            oddListHDP?.get(0)?.isSelected ?: false
                        }
                        GameType.TN.key, GameType.VB.key -> {
                            oddList1x2?.get(0)?.isSelected ?: false
                        }
                        else -> {
                            false
                        }
                    }

                    betStatus = when (gameType) {
                        GameType.FT.key, GameType.BK.key -> {
                            if (oddListHDP == null || oddListHDP?.size ?: 0 < 2) {
                                BetStatus.LOCKED.code
                            } else {
                                oddListHDP?.get(0)?.status ?: BetStatus.LOCKED.code
                            }
                        }
                        GameType.TN.key, GameType.VB.key -> {
                            if (oddList1x2 == null || oddList1x2?.size ?: 0 < 2) {
                                BetStatus.LOCKED.code
                            } else {
                                oddList1x2?.get(0)?.status ?: BetStatus.LOCKED.code
                            }
                        }
                        else -> {
                            null
                        }
                    }

                    this@ViewHolderHdpOu.setupOddState(
                        this, when (gameType) {
                            GameType.FT.key, GameType.BK.key -> {
                                oddListHDP?.get(0)
                            }
                            GameType.TN.key, GameType.VB.key -> {
                                oddList1x2?.get(0)
                            }
                            else -> {
                                null
                            }
                        }
                    )

                    when {
                        oddListHDP != null && oddListHDP?.size ?: 0 >= 2 -> {
                            setupOdd(oddListHDP?.get(0), oddsType)
                        }
                        oddList1x2 != null && oddList1x2?.size ?: 0 >= 2 -> {
                            setupOdd(oddList1x2?.get(0), oddsType)
                        }
                    }

                    setOnClickListener {
                        when (gameType) {
                            GameType.FT.key, GameType.BK.key -> {
                                if (oddListHDP != null && oddListHDP?.size ?: 0 >= 2) {
                                    oddListHDP?.get(0)?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            playCateStr
                                        )
                                    }
                                }
                            }

                            GameType.TN.key, GameType.VB.key -> {
                                if (oddList1x2 != null && oddList1x2?.size ?: 0 >= 2) {
                                    oddList1x2?.get(0)?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            playCateStr
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                btn_match_odd2.apply {
                    isSelected = when (gameType) {
                        GameType.FT.key, GameType.BK.key -> {
                            oddListHDP?.get(1)?.isSelected ?: false
                        }
                        GameType.TN.key, GameType.VB.key -> {
                            oddList1x2?.get(1)?.isSelected ?: false
                        }
                        else -> {
                            false
                        }
                    }

                    betStatus = when (gameType) {
                        GameType.FT.key, GameType.BK.key -> {
                            if (oddListHDP == null || oddListHDP?.size ?: 0 < 2) {
                                BetStatus.LOCKED.code
                            } else {
                                oddListHDP?.get(1)?.status ?: BetStatus.LOCKED.code
                            }
                        }
                        GameType.TN.key, GameType.VB.key -> {
                            if (oddList1x2 == null || oddList1x2?.size ?: 0 < 2) {
                                BetStatus.LOCKED.code
                            } else {
                                oddList1x2?.get(1)?.status ?: BetStatus.LOCKED.code
                            }
                        }
                        else -> {
                            null
                        }
                    }

                    this@ViewHolderHdpOu.setupOddState(
                        this, when (gameType) {
                            GameType.FT.key, GameType.BK.key -> {
                                oddListHDP?.get(1)
                            }
                            GameType.TN.key, GameType.VB.key -> {
                                oddList1x2?.get(1)
                            }
                            else -> {
                                null
                            }
                        }
                    )

                    when {
                        oddListHDP != null && oddListHDP?.size ?: 0 >= 2 -> {
                            setupOdd(oddListHDP?.get(1), oddsType)
                        }
                        oddList1x2 != null && oddList1x2?.size ?: 0 >= 2 -> {
                            setupOdd(oddList1x2?.get(1), oddsType)
                        }
                    }

                    setOnClickListener {
                        when (gameType) {
                            GameType.FT.key, GameType.BK.key -> {
                                if (oddListHDP != null && oddListHDP?.size ?: 0 >= 2) {
                                    oddListHDP?.get(1)?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            playCateStr
                                        )
                                    }
                                }
                            }

                            GameType.TN.key, GameType.VB.key -> {
                                if (oddList1x2 != null && oddList1x2?.size ?: 0 >= 2) {
                                    oddList1x2?.get(1)?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            playCateStr
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private fun startTimer(
            startTime: Int,
            isDecrease: Boolean,
            timerListener: (timeMillis: Long) -> Unit
        ) {
            var timeMillis = startTime * 1000L

            mTimerMap[adapterPosition]?.cancel()
            stopTimer()

            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    when {
                        timeMillis < 0 -> {
                            timeMillis = 0
                            mTimerMap[adapterPosition]?.cancel()
                        }
                        isDecrease -> timeMillis -= 1000
                        !isDecrease -> timeMillis += 1000
                    }
                    Handler(Looper.getMainLooper()).post {
                        timerListener(timeMillis)
                    }
                }
            }, 1000L, 1000L)

            mTimerMap[adapterPosition] = timer
        }

        fun stopTimer() {
            timer?.cancel()
            timer = null
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = mOddStateRefreshListener

    }

}