package org.cxct.sportlottery.ui.game.home.highlight

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd.view.*
import kotlinx.android.synthetic.main.home_highlight_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.matchCategory.result.OddData
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.home.gameTable4.OnClickOddListener
import org.cxct.sportlottery.ui.game.widget.OddButton
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import java.util.*


class RvHighlightAdapter : RecyclerView.Adapter<RvHighlightAdapter.ViewHolderHdpOu>() {

    private var dataList = listOf<MatchOdd>()
    fun setData(sportCode: String?, newList: List<OddData>?) {
        dataList = newList?.map {
            val matchInfo = MatchInfo(
                gameType = null,
                awayName = it.matchInfo?.awayName ?: "",
                endTime = it.matchInfo?.endTime?.toString(),
                homeName = it.matchInfo?.homeName ?: "",
                id = it.matchInfo?.id ?: "",
                playCateNum = it.matchInfo?.playCateNum ?: 0,
                startTime = it.matchInfo?.startTime?.toString() ?: "",
                status = it.matchInfo?.status ?: -1
            ).apply {
                sportType = SportType.getSportType(sportCode)
                startDateDisplay = TimeUtil.timeFormat(it.matchInfo?.startTime, "MM/dd")
                startTimeDisplay = TimeUtil.timeFormat(it.matchInfo?.startTime, "HH:mm")
            }
            val odds = it.odds ?: mutableMapOf()
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

    private val matchType: MatchType = MatchType.TODAY

    var onClickOddListener: OnClickOddListener? = null

    var onClickMatchListener: OnSelectItemListener<MatchOdd>? = null //賽事畫面跳轉

    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(dataList.indexOf(dataList.find { matchOdd ->
                    matchOdd.odds.toList()
                        .find { map -> map.second.find { it == odd } != null } != null
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

        private var timer: Timer? = null

        fun bind(data: MatchOdd) {
            setupMatchInfo(data)
            setupTime(data)
            setupOddButton(data)

            //TODO simon test review 賠率 icon 顯示邏輯
            itemView.iv_match_in_play.visibility = if (matchType == MatchType.IN_PLAY) View.VISIBLE else View.GONE
//            itemView.iv_match_price.visibility = if () View.VISIBLE else View.GONE
//            itemView.iv_match_live.visibility = if () View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onClickMatchListener?.onClick(data)
            }
        }

        private fun setupMatchInfo(data: MatchOdd) {
            itemView.apply {
                tv_game_name_home.text = data.matchInfo?.homeName
                tv_game_name_away.text = data.matchInfo?.awayName
                tv_match_play_type_count.text = data.matchInfo?.playCateNum?.toString()
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setupTime(data: MatchOdd) {
            itemView.apply {
                when (matchType) {
                    MatchType.AT_START -> {
                        val remainTime = data.matchInfo?.remainTime?.let { remainTime ->
                            startTimer((remainTime / 1000).toInt(), true) { timeMillis ->
                                data.matchInfo.remainTime = timeMillis
                                String.format(
                                    itemView.context.resources.getString(R.string.at_start_remain_minute),
                                    TimeUtil.timeFormat(timeMillis, "mm")
                                )
                            }
                        }

                        tv_match_time.text = data.matchInfo?.startDateDisplay + " " + remainTime
                    }

                    else -> {
                        stopTimer()
                        tv_match_time.text =
                            "${data.matchInfo?.startDateDisplay ?: ""} ${data.matchInfo?.startTimeDisplay ?: ""}"
                    }
                }
            }
        }

        private fun setupOddButton(data: MatchOdd) {
            itemView.apply {
                val sportType = data.matchInfo?.sportType

                val playTypeStr = when (sportType) {
                    SportType.FOOTBALL, SportType.BASKETBALL -> context.getText(R.string.ou_hdp_hdp_title)
                    SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> context.getText(R.string.ou_hdp_1x2_title)
                    else -> ""
                }.toString()

                val oddListHDP = when (sportType) {
                    SportType.TENNIS -> {
                        data.odds[PlayType.SET_HDP.code]
                    }
                    SportType.BASKETBALL -> {
                        data.odds[PlayType.HDP_INCL_OT.code]
                    }
                    else -> {
                        data.odds[PlayType.HDP.code]
                    }
                }

                val oddList1x2 = when (sportType) {
                    SportType.BASKETBALL -> {
                        data.odds[PlayType.X12_INCL_OT.code]
                    }
                    else -> {
                        data.odds[PlayType.X12.code]
                    }
                }

                btn_match_odd1.apply {
                    playType = when (sportType) {
                        SportType.FOOTBALL, SportType.BASKETBALL -> {
                            PlayType.HDP
                        }
                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                            PlayType.X12
                        }
                        else -> null
                    }

                    isSelected = when (sportType) {
                        SportType.FOOTBALL, SportType.BASKETBALL -> {
                            oddListHDP?.get(0)?.isSelected ?: false
                        }
                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                            oddList1x2?.get(0)?.isSelected ?: false
                        }
                        else -> {
                            false
                        }
                    }

                    betStatus = when (sportType) {
                        SportType.FOOTBALL, SportType.BASKETBALL -> {
                            if (oddListHDP == null || oddListHDP.size < 2) {
                                BetStatus.LOCKED.code
                            } else {
                                oddListHDP[0]?.status ?: BetStatus.LOCKED.code
                            }
                        }
                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                            if (oddList1x2 == null || oddList1x2.size < 2) {
                                BetStatus.LOCKED.code
                            } else {
                                oddList1x2[0]?.status ?: BetStatus.LOCKED.code
                            }
                        }
                        else -> {
                            null
                        }
                    }

                    this@ViewHolderHdpOu.setupOddState(
                        this, when (sportType) {
                            SportType.FOOTBALL, SportType.BASKETBALL -> {
                                oddListHDP?.get(0)
                            }
                            SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                                oddList1x2?.get(0)
                            }
                            else -> {
                                null
                            }
                        }
                    )

                    onOddStatusChangedListener = object : OddButton.OnOddStatusChangedListener {
                        override fun onOddStateChangedFinish() {
                            when (sportType) {
                                SportType.FOOTBALL, SportType.BASKETBALL -> {
                                    oddListHDP?.get(0)?.oddState = OddState.SAME.state
                                }
                                SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                                    oddList1x2?.get(0)?.oddState = OddState.SAME.state
                                }
                            }
                        }
                    }

                    odd_hdp_top_text.text = if (oddListHDP == null || oddListHDP.size < 2) {
                        ""
                    } else {
                        oddListHDP[0]?.spread
                    }

                    odd_hdp_bottom_text.text = when {
                        (oddListHDP != null && oddListHDP.size >= 2 && oddsType == OddsType.EU) -> {
                            oddListHDP[0]?.odds?.let {
                                TextUtil.formatForOdd(it)
                            }
                        }
                        (oddListHDP != null && oddListHDP.size >= 2 && oddsType == OddsType.HK) -> {
                            oddListHDP[0]?.hkOdds?.let {
                                TextUtil.formatForOdd(it)
                            }
                        }
                        else -> ""
                    }

                    odd_1x2_top_text.visibility = View.GONE

                    odd_1x2_bottom_text.text = when {
                        (oddList1x2 != null && oddList1x2.size >= 2 && oddsType == OddsType.EU) -> {
                            oddList1x2[0]?.odds?.let {
                                TextUtil.formatForOdd(it)
                            }
                        }
                        (oddList1x2 != null && oddList1x2.size >= 2 && oddsType == OddsType.HK) -> {
                            oddList1x2[0]?.hkOdds?.let {
                                TextUtil.formatForOdd(it)
                            }
                        }
                        else -> ""
                    }

                    setOnClickListener {
                        when (sportType) {
                            SportType.FOOTBALL, SportType.BASKETBALL -> {
                                if (oddListHDP != null && oddListHDP.size >= 2) {
                                    oddListHDP[0]?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            playTypeStr,
                                            data.matchInfo.homeName
                                        )
                                    }
                                }
                            }

                            SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                                if (oddList1x2 != null && oddList1x2.size >= 2) {
                                    oddList1x2[0]?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            playTypeStr,
                                            data.matchInfo.homeName
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                btn_match_odd2.apply {
                    playType = when (sportType) {
                        SportType.FOOTBALL, SportType.BASKETBALL -> {
                            PlayType.HDP
                        }
                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                            PlayType.X12
                        }
                        else -> null
                    }

                    isSelected = when (sportType) {
                        SportType.FOOTBALL, SportType.BASKETBALL -> {
                            oddListHDP?.get(1)?.isSelected ?: false
                        }
                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                            oddList1x2?.get(1)?.isSelected ?: false
                        }
                        else -> {
                            false
                        }
                    }

                    betStatus = when (sportType) {
                        SportType.FOOTBALL, SportType.BASKETBALL -> {
                            if (oddListHDP == null || oddListHDP.size < 2) {
                                BetStatus.LOCKED.code
                            } else {
                                oddListHDP[1]?.status ?: BetStatus.LOCKED.code
                            }
                        }
                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                            if (oddList1x2 == null || oddList1x2.size < 2) {
                                BetStatus.LOCKED.code
                            } else {
                                oddList1x2[1]?.status ?: BetStatus.LOCKED.code
                            }
                        }
                        else -> {
                            null
                        }
                    }

                    this@ViewHolderHdpOu.setupOddState(
                        this, when (sportType) {
                            SportType.FOOTBALL, SportType.BASKETBALL -> {
                                oddListHDP?.get(1)
                            }
                            SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                                oddList1x2?.get(1)
                            }
                            else -> {
                                null
                            }
                        }
                    )

                    onOddStatusChangedListener = object : OddButton.OnOddStatusChangedListener {
                        override fun onOddStateChangedFinish() {
                            when (sportType) {
                                SportType.FOOTBALL, SportType.BASKETBALL -> {
                                    oddListHDP?.get(1)?.oddState = OddState.SAME.state
                                }
                                SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                                    oddList1x2?.get(1)?.oddState = OddState.SAME.state
                                }
                            }
                        }
                    }

                    odd_hdp_top_text.text = if (oddListHDP == null || oddListHDP.size < 2) {
                        ""
                    } else {
                        oddListHDP[1]?.spread
                    }

                    odd_hdp_bottom_text.text = when {
                        (oddListHDP != null && oddListHDP.size >= 2 && oddsType == OddsType.EU) -> {
                            oddListHDP[1]?.odds?.let {
                                TextUtil.formatForOdd(it)
                            }
                        }
                        (oddListHDP != null && oddListHDP.size >= 2 && oddsType == OddsType.HK) -> {
                            oddListHDP[1]?.hkOdds?.let {
                                TextUtil.formatForOdd(it)
                            }
                        }
                        else -> ""
                    }

                    odd_1x2_top_text.visibility = View.GONE

                    odd_1x2_bottom_text.text = when {
                        (oddList1x2 != null && oddList1x2.size >= 2 && oddsType == OddsType.EU) -> {
                            oddList1x2[1]?.odds?.let {
                                TextUtil.formatForOdd(it)
                            }
                        }
                        (oddList1x2 != null && oddList1x2.size >= 2 && oddsType == OddsType.HK) -> {
                            oddList1x2[1]?.hkOdds?.let {
                                TextUtil.formatForOdd(it)
                            }
                        }
                        else -> ""
                    }

                    setOnClickListener {
                        when (sportType) {
                            SportType.FOOTBALL, SportType.BASKETBALL -> {
                                if (oddListHDP != null && oddListHDP.size >= 2) {
                                    oddListHDP[1]?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            playTypeStr,
                                            data.matchInfo.awayName
                                        )
                                    }
                                }
                            }

                            SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                                if (oddList1x2 != null && oddList1x2.size >= 2) {
                                    oddList1x2[1]?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            playTypeStr,
                                            data.matchInfo.awayName
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        fun startTimer(
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
                        timeMillis < 0 -> timeMillis = 0
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