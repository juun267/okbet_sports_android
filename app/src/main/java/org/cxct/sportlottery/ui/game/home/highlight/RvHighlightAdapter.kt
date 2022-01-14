package org.cxct.sportlottery.ui.game.home.highlight

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
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
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.MatchOddUtil.updateOddsDiscount
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setTextTypeFace
import java.util.*

class RvHighlightAdapter : RecyclerView.Adapter<RvHighlightAdapter.ViewHolderHdpOu>() {

    private var dataList = listOf<MatchOdd>()

    var discount: Float = 1.0F
        set(newDiscount) {
            dataList.forEach { matchOdd ->
                matchOdd.oddsMap.updateOddsDiscount(field, newDiscount)
            }

            notifyDataSetChanged()
            field = newDiscount
        }

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
                eps = it.matchInfo?.eps,
                spt = it.matchInfo?.spt,
                liveVideo = it.matchInfo?.liveVideo,
                status = it.matchInfo?.status ?: -1).apply {
                gameType = sportCode
                startDateDisplay = TimeUtil.timeFormat(it.matchInfo?.startTime, "MM/dd")
                startTimeDisplay = TimeUtil.timeFormat(it.matchInfo?.startTime, "HH:mm")
                isAtStart = TimeUtil.isTimeAtStart(it.matchInfo?.startTime)
            }
            val odds: MutableMap<String, MutableList<Odd?>?> = mutableMapOf()
            it.oddsMap.forEach { odd ->
                odds[odd.key] = odd.value?.toMutableList()
            }
            MatchOdd(
                it.betPlayCateNameMap,
                it.playCateNameMap,
                matchInfo,
                odds,
                it.dynamicMarkets,
                it.quickPlayCateList,
                it.oddsSort
            )
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
            var lastIndex = if (position > 0) position - 1 else 0
            val lastData = dataList[lastIndex]
            holder.bind(data,lastData)
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

        private var oddList: MutableList<Odd?>? = null


        private var timer: Timer? = null

        fun bind(data: MatchOdd, lastData: MatchOdd) {
            setTitle(data,lastData)
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

        private fun setTitle(data: MatchOdd, lastData: MatchOdd) {
            try {
                itemView.apply {
                    when {
                        bindingAdapterPosition == 0 -> {
                            ll_highlight_type.visibility = View.VISIBLE
                            tv_game_type.isVisible = true
                            tv_play_type_highlight.isVisible = true

                            val playCate = if(data.oddsSort?.split(",")?.size?:0 > 0) data.oddsSort?.split(",")
                                ?.getOrNull(0) else data.oddsSort

                            tv_play_type_highlight.text =
                                data.playCateNameMap?.get(playCate)
                                    ?.get(LanguageManager.getSelectLanguage(context).key) ?: ""
                        }
                        TimeUtil.isTimeToday(data.matchInfo?.startTime) && !TimeUtil.isTimeToday(
                            lastData.matchInfo?.startTime
                        ) -> {
                            ll_highlight_type.visibility = View.VISIBLE

                            tv_game_type.isVisible = true
                            tv_play_type_highlight.visibility = View.INVISIBLE
                        }
                        !TimeUtil.isTimeToday(data.matchInfo?.startTime) && TimeUtil.isTimeToday(
                            lastData.matchInfo?.startTime
                        ) -> {
                            ll_highlight_type.visibility = View.VISIBLE

                            tv_game_type.isVisible = true
                            tv_play_type_highlight.visibility = View.INVISIBLE
                        }
                        else -> {
                            ll_highlight_type.visibility = View.GONE
                        }
                    }

                    tv_game_type.text = if (TimeUtil.isTimeToday(data.matchInfo?.startTime)) {
                        resources.getString(R.string.home_tab_today)
                    } else {
                        "${resources.getString(TimeUtil.setupDayOfWeekAndToday(data.matchInfo?.startTime))} ${data.matchInfo?.startDateDisplay}"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun setMatchType(data: MatchOdd) {
            matchType = if(data.matchInfo?.isAtStart == true) MatchType.AT_START else  MatchType.TODAY
        }

        private fun setupOddList(data: MatchOdd) {
            itemView.apply {
                gameType = data.matchInfo?.gameType

                oddList = if(data.oddsMap.isNotEmpty()) {
                    data.oddsMap.iterator().next().value
                }else{
                    mutableListOf()
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
                    setTextTypeFace(if (oddList?.getOrNull(0)?.spread?.contains("-") == true)
                            Typeface.BOLD
                        else
                            Typeface.NORMAL
                    )
                }
                tv_game_name_away.apply {
                    setTextTypeFace(if (oddList?.getOrNull(1)?.spread?.contains("-") == true)
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
                                    TimeUtil.longToMinute(timeMillis)
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
            try {
                itemView.apply {
                    gameType = data.matchInfo?.gameType

                    val playCateName =
                        if (data.oddsSort?.split(",")?.size ?: 0 > 0) data.oddsSort?.split(",")
                            ?.getOrNull(0) else data.oddsSort

                    val playCateStr = data.playCateNameMap?.get(playCateName)
                        ?.get(LanguageManager.getSelectLanguage(context).key)

                    btn_match_odd1.apply {
                        isSelected = if (oddList.isNullOrEmpty() || oddList?.size ?: 0 < 2) {
                            false
                        } else {
                            oddList?.getOrNull(0)?.isSelected ?: false
                        }

                        betStatus = if (oddList.isNullOrEmpty() || oddList?.size ?: 0 < 2) {
                            BetStatus.DEACTIVATED.code
                        } else {
                            oddList?.getOrNull(0)?.status ?: BetStatus.LOCKED.code
                        }

                        if (!oddList.isNullOrEmpty() && oddList?.size ?: 0 >= 2) {
                            this@ViewHolderHdpOu.setupOddState(this, oddList?.getOrNull(0))

                            setupOdd(
                                oddList?.getOrNull(0),
                                oddsType,
                                "disable"
                            ) //TODO Bill 這裡要看球種顯示 1/2 不能用disable

                            setOnClickListener {
                                if (oddList != null && oddList?.size ?: 0 >= 2) {
                                    oddList?.getOrNull(0)?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            playCateName.toString(),
                                            playCateStr,
                                            data.betPlayCateNameMap
                                        )
                                    }
                                }
                            }
                        }

                    }

                    btn_match_odd2.apply {
                        isSelected = if (oddList.isNullOrEmpty() || oddList?.size ?: 0 < 2) {
                            false
                        } else {
                            oddList?.getOrNull(1)?.isSelected ?: false
                        }

                        betStatus = if (oddList.isNullOrEmpty() || oddList?.size ?: 0 < 2) {
                            BetStatus.DEACTIVATED.code
                        } else {
                            oddList?.getOrNull(1)?.status ?: BetStatus.LOCKED.code
                        }

                        if (!oddList.isNullOrEmpty() && oddList?.size ?: 0 >= 2) {
                            this@ViewHolderHdpOu.setupOddState(this, oddList?.getOrNull(1))

                            setupOdd(
                                oddList?.getOrNull(1),
                                oddsType,
                                "disable"
                            )  //TODO Bill 這裡要看球種顯示 1/2 不能用disable

                            setOnClickListener {
                                if (oddList != null && oddList?.size ?: 0 >= 2) {
                                    oddList?.getOrNull(1)?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            playCateName.toString(),
                                            playCateStr,
                                            data.betPlayCateNameMap
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun startTimer(
            startTime: Int,
            isDecrease: Boolean,
            timerListener: (timeMillis: Long) -> Unit
        ) {
            var timeMillis = startTime * 1000L

            mTimerMap[bindingAdapterPosition]?.cancel()
            stopTimer()

            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    when {
                        timeMillis < 0 -> {
                            timeMillis = 0
                            mTimerMap[bindingAdapterPosition]?.cancel()
                        }
                        isDecrease -> timeMillis -= 1000
                        !isDecrease -> timeMillis += 1000
                    }
                    Handler(Looper.getMainLooper()).post {
                        timerListener(timeMillis)
                    }
                }
            }, 1000L, 1000L)

            mTimerMap[bindingAdapterPosition] = timer
        }

        fun stopTimer() {
            timer?.cancel()
            timer = null
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = mOddStateRefreshListener

    }

}