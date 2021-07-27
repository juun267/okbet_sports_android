package org.cxct.sportlottery.ui.game.home.highlight

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.home_highlight_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.matchCategory.result.OddData
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.home.OnClickOddListener
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
            val odds: MutableMap<String, MutableList<Odd?>> = mutableMapOf()
            it.odds?.forEach { odd ->
                odds[odd.key] = odd.value.toMutableList()
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


        private var sportType: SportType? = null

        private var oddListHDP: MutableList<Odd?>? = null
        private var oddList1x2: MutableList<Odd?>? = null

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
                sportType = data.matchInfo?.sportType

                val playCateStr = when (sportType) {
                    SportType.FOOTBALL, SportType.BASKETBALL -> context.getText(R.string.ou_hdp_hdp_title)
                    SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> context.getText(R.string.ou_hdp_1x2_title)
                    else -> ""
                }.toString()

                oddListHDP = when (sportType) {
                    SportType.TENNIS -> {
                        data.odds[PlayCate.SET_HDP.value]
                    }
                    SportType.BASKETBALL -> {
                        data.odds[PlayCate.HDP_INCL_OT.value]
                    }
                    else -> {
                        data.odds[PlayCate.HDP.value]
                    }
                }

                oddList1x2 = when (sportType) {
                    SportType.BASKETBALL -> {
                        data.odds[PlayCate.SINGLE_OT.value]
                    }
                    else -> {
                        data.odds[PlayCate.SINGLE.value]
                    }
                }

                btn_match_odd1.apply {
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
                            if (oddListHDP == null || oddListHDP?.size ?: 0 < 2) {
                                BetStatus.LOCKED.code
                            } else {
                                oddListHDP?.get(0)?.status ?: BetStatus.LOCKED.code
                            }
                        }
                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
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

                    when {
                        oddListHDP != null && oddListHDP?.size ?: 0 >= 2 -> {
                            setupOdd(oddListHDP?.get(0), oddsType)
                        }

                        oddList1x2 != null && oddList1x2?.size ?: 0 >= 2 -> {
                            setupOdd(oddList1x2?.get(0), oddsType)
                        }
                    }

                    setOnClickListener {
                        when (sportType) {
                            SportType.FOOTBALL, SportType.BASKETBALL -> {
                                if (oddListHDP != null && oddListHDP?.size ?: 0 >= 2) {
                                    oddListHDP?.get(0)?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            playCateStr,
                                            data.matchInfo?.homeName ?: ""
                                        )
                                    }
                                }
                            }

                            SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                                if (oddList1x2 != null && oddList1x2?.size ?: 0 >= 2) {
                                    oddList1x2?.get(0)?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            playCateStr,
                                            data.matchInfo?.homeName ?: ""
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                btn_match_odd2.apply {
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
                            if (oddListHDP == null || oddListHDP?.size ?: 0 < 2) {
                                BetStatus.LOCKED.code
                            } else {
                                oddListHDP?.get(1)?.status ?: BetStatus.LOCKED.code
                            }
                        }
                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
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

                    when {
                        oddListHDP != null && oddListHDP?.size ?: 0 >= 2 -> {
                            setupOdd(oddListHDP?.get(1), oddsType)
                        }

                        oddList1x2 != null && oddList1x2?.size ?: 0 >= 2 -> {
                            setupOdd(oddList1x2?.get(1), oddsType)
                        }
                    }

                    setOnClickListener {
                        when (sportType) {
                            SportType.FOOTBALL, SportType.BASKETBALL -> {
                                if (oddListHDP != null && oddListHDP?.size ?: 0 >= 2) {
                                    oddListHDP?.get(1)?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            playCateStr,
                                            data.matchInfo?.awayName ?: ""
                                        )
                                    }
                                }
                            }

                            SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                                if (oddList1x2 != null && oddList1x2?.size ?: 0 >= 2) {
                                    oddList1x2?.get(1)?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            playCateStr,
                                            data.matchInfo?.awayName ?: ""
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