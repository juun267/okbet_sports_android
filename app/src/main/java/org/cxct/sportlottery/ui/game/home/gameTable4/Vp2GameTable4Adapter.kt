package org.cxct.sportlottery.ui.game.home.gameTable4

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import kotlinx.android.synthetic.main.home_game_table_item_4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.home.OnClickFavoriteListener
import org.cxct.sportlottery.ui.game.home.OnClickOddListener
import org.cxct.sportlottery.ui.game.home.OnClickStatisticsListener
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setTextTypeFace
import java.util.*

class Vp2GameTable4Adapter(
    val dataList: List<MatchOdd>,
    val oddsType: OddsType,
    val matchType: MatchType
) :
    RecyclerView.Adapter<Vp2GameTable4Adapter.ViewHolderHdpOu>() {

    var onClickOddListener: OnClickOddListener? = null

    var onClickMatchListener: OnSelectItemListener<MatchOdd>? = null //賽事畫面跳轉

    var onClickFavoriteListener: OnClickFavoriteListener? = null

    var onClickStatisticsListener: OnClickStatisticsListener? = null

    var isLogin: Boolean? = false

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
            .inflate(R.layout.home_game_table_item_4, parent, false)
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

        private var oddList: MutableList<Odd?>? = null

        private var timer: Timer? = null

        fun bind(data: MatchOdd) {
            setupOddList(data)
            setupMatchInfo(data)
            setupTime(data)
            setupOddButton(data)

            //TODO simon test review 賠率 icon 顯示邏輯
            itemView.iv_match_in_play.visibility = if (matchType == MatchType.AT_START) {
                View.VISIBLE
            } else {
                View.GONE
            }
            itemView.iv_match_price.visibility =
                if (data.matchInfo?.eps == 1) View.VISIBLE else View.GONE

            itemView.iv_play.isVisible = (data.matchInfo?.liveVideo == 1)

            itemView.table_match_info_border.setOnClickListener {
                onClickMatchListener?.onClick(data)
            }

            itemView.tv_match_play_type_count.setOnClickListener {
                onClickMatchListener?.onClick(data)
            }

            itemView.btn_chart.setOnClickListener {
                onClickStatisticsListener?.onClickStatistics(data.matchInfo?.id)
            }
        }

        /* 目前不會用，先留著，以防之後說要改回來。
                private fun ImageView.setLiveImg() {
                    when (gameType) {
                        GameType.FT.key -> setImageResource(R.drawable.ic_live_football_small)
                        GameType.BK.key -> setImageResource(R.drawable.ic_live_basketball_small)
                        GameType.TN.key -> setImageResource(R.drawable.ic_live_tennis_small)
                        GameType.VB.key -> setImageResource(R.drawable.ic_live_volleyball_small)
                        GameType.BM.key -> setImageResource(R.drawable.ic_live_badminton_small)
                        GameType.TT.key -> setImageResource(R.drawable.ic_live_pingpong_small)
                        GameType.IH.key -> setImageResource(R.drawable.ic_live_icehockey_small)
                        GameType.BX.key -> setImageResource(R.drawable.ic_live_boxing_small)
                        GameType.CB.key -> setImageResource(R.drawable.ic_live_billiards_small)
                        GameType.CK.key -> setImageResource(R.drawable.ic_live_cricket_small)
                        GameType.BB.key -> setImageResource(R.drawable.ic_live_baseball_small)
                        GameType.RB.key -> setImageResource(R.drawable.ic_live_rugby_small)
                        GameType.AFT.key -> setImageResource(R.drawable.ic_live_soccer_small)
                        GameType.MR.key -> setImageResource(R.drawable.ic_live_racing_small)
                        GameType.GF.key -> setImageResource(R.drawable.ic_live_golf_small)
                    }
                }
                */
        private fun setupOddList(data: MatchOdd) {
            itemView.apply {
                gameType = data.matchInfo?.gameType

                oddList = if (data.oddsMap.isNotEmpty()) {
                    data.oddsMap.iterator().next().value
                } else {
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
                    isSelected = data.matchInfo?.isFavorite ?: false

                    setOnClickListener {
                        onClickFavoriteListener?.onClickFavorite(data.matchInfo?.id)
                        if (isLogin == true)
                            btn_star.isSelected = !isSelected
                    }
                }

                when (matchType) {
                    MatchType.IN_PLAY -> {
                        tv_game_type.text = context.getString(R.string.home_tab_in_play)

                        when (data.matchInfo?.gameType) {
                            GameType.TN.key -> {
                                tv_match_status.visibility = View.GONE

                                //盤比分
                                tv_game_score_home.visibility = View.VISIBLE
                                tv_game_score_away.visibility = View.VISIBLE
                                val homeTotalScore = data.matchInfo.homeTotalScore ?: 0
                                val awayTotalScore = data.matchInfo.awayTotalScore ?: 0
                                tv_game_score_home.text = "$homeTotalScore"
                                tv_game_score_away.text = "$awayTotalScore"

                                //局比分
                                tv_score.visibility = View.VISIBLE
                                val homeScore = data.matchInfo.homeScore ?: 0
                                val awayScore = data.matchInfo.awayScore ?: 0
                                tv_score.text = "$homeScore–$awayScore"

                                //點比分
                                tv_point.visibility = View.VISIBLE
                                val homePoint = data.matchInfo.homePoints ?: 0
                                val awayPoint = data.matchInfo.awayPoints ?: 0
                                tv_point.text = "$homePoint–$awayPoint"
                            }
                            GameType.VB.key, GameType.TT.key -> {
                                tv_game_total_score_home_center.visibility = View.VISIBLE
                                tv_game_score_home_center.visibility = View.VISIBLE
                                tv_game_total_score_away_center.visibility = View.VISIBLE
                                tv_game_score_away_center.visibility = View.VISIBLE

                                tv_match_status.visibility = View.VISIBLE

                                tv_match_status.text =
                                    "${data.matchInfo.statusName18n} / ${data.matchInfo.spt}" ?: ""

                                tv_game_score_home.visibility = View.GONE
                                tv_game_score_away.visibility = View.GONE

                                //盤比分
                                val homeTotalScore = data.matchInfo.homeTotalScore ?: 0
                                val awayTotalScore = data.matchInfo.awayTotalScore ?: 0
                                tv_game_total_score_home_center.text = "$homeTotalScore"
                                tv_game_total_score_away_center.text = "$awayTotalScore"

                                //點比分
                                val homeScore = data.matchInfo.homeScore ?: 0
                                val awayScore = data.matchInfo.awayScore ?: 0
                                tv_game_score_home_center.text = "$homeScore"
                                tv_game_score_away_center.text = "$awayScore"

                                tv_score.visibility = View.GONE
                                tv_point.visibility = View.GONE
                            }
                            else -> {
                                tv_match_status.visibility = View.VISIBLE
                                tv_match_status.text = data.matchInfo?.statusName18n ?: ""

                                tv_game_score_home.visibility = View.VISIBLE
                                tv_game_score_away.visibility = View.VISIBLE
                                val homeScore = data.matchInfo?.homeScore ?: 0
                                val awayScore = data.matchInfo?.awayScore ?: 0
                                tv_game_score_home.text = "$homeScore"
                                tv_game_score_away.text = "$awayScore"
                            }
                        }
                    }
                    MatchType.AT_START -> {
                        tv_game_type.text = context.getString(R.string.home_tab_today)

                        tv_game_score_home.visibility = View.GONE
                        tv_game_score_away.visibility = View.GONE

                        tv_game_name_home.setTextTypeFace(Typeface.NORMAL)
                        tv_game_name_away.setTextTypeFace(Typeface.NORMAL)

                        tv_score.visibility = View.GONE
                        tv_point.visibility = View.GONE
                    }
                    else -> {
                    }
                }
            }
        }

        private fun showStrongTeam() {
            itemView.apply {

                val homeStrongType = if (oddList?.getOrNull(0)?.spread?.contains("-") == true)
                    Typeface.BOLD
                else
                    Typeface.NORMAL

                val awayStrongType = if (oddList?.getOrNull(1)?.spread?.contains("-") == true)
                    Typeface.BOLD
                else
                    Typeface.NORMAL

                tv_game_score_home.setTextTypeFace(homeStrongType)
                tv_game_name_home.setTextTypeFace(homeStrongType)

                tv_game_score_away.setTextTypeFace(awayStrongType)
                tv_game_name_away.setTextTypeFace(awayStrongType)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setupTime(data: MatchOdd) {
            itemView.apply {
                when (matchType) {
                    MatchType.IN_PLAY -> {
                        when (data.matchInfo?.gameType) {
                            GameType.FT.key -> { //足球
                                data.leagueTime?.let { leagueTime ->
                                    startTimer(leagueTime, false) { timeMillis ->
                                        val timeMillisAbs = if (timeMillis > 0) timeMillis else 0
                                        val timeStr = TimeUtil.longToMmSs(timeMillisAbs)
                                        tv_match_time.text = timeStr
                                        data.leagueTime = (timeMillisAbs / 1000).toInt()
                                    }
                                }
                            }
                            GameType.BK.key -> { //籃球
                                data.leagueTime?.let { leagueTime ->
                                    startTimer(leagueTime, true) { timeMillis ->
                                        val timeMillisAbs = if (timeMillis > 0) timeMillis else 0
                                        val timeStr = TimeUtil.longToMmSs(timeMillisAbs)
                                        tv_match_time.text = timeStr
                                        data.leagueTime = (timeMillisAbs / 1000).toInt()
                                    }
                                }
                            }
                            else -> {
                                stopTimer()
                                tv_match_time.text = null
                            }
                        }
                    }

                    MatchType.AT_START -> {
                        val statusName = if (data.matchInfo?.startDateDisplay.isNullOrEmpty())
                            ""
                        else
                            data.matchInfo?.startDateDisplay + " "

                        data.matchInfo?.remainTime?.let { remainTime ->
                            startTimer((remainTime / 1000).toInt(), true) { timeMillis ->
                                val timeMillisAbs = if (timeMillis > 0) timeMillis else 0
                                val timeStr = statusName + String.format(
                                    itemView.context.resources.getString(R.string.at_start_remain_minute),
                                    TimeUtil.longToMinute(timeMillisAbs)
                                )
                                tv_match_time.text = timeStr

                                data.matchInfo.remainTime = timeMillisAbs
                            }
                        }
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
                gameType = data.matchInfo?.gameType

                //要取 datas 的matchOdds 下面的 oddsSort 去抓排序裡第一個的翻譯顯示 2022/01/11 與後端Ｍax確認 by Bill
                val playCateName =
                    if (data.oddsSort?.split(",")?.size ?: 0 > 0) data.oddsSort?.split(",")
                        ?.getOrNull(0) else data.oddsSort

                tv_play_type.text = data.playCateNameMap?.get(data.oddsSort)
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

                        setupOdd(oddList?.getOrNull(0), oddsType)

                        setOnClickListener {
                            if (oddList != null && oddList?.size ?: 0 >= 2) {
                                oddList?.getOrNull(0)?.let { odd ->
                                    onClickOddListener?.onClickBet(
                                        data,
                                        odd,
                                        playCateName.toString(),
                                        itemView.tv_play_type.text.toString(),
                                        data.betPlayCateNameMap
                                    )
                                }
                            }

                        }
                    }

                    tv_name.visibility = when (gameType) {
                        GameType.FT.key, GameType.BK.key -> View.GONE
                        GameType.TN.key, GameType.VB.key, GameType.TT.key, GameType.CK.key, GameType.BM.key -> View.VISIBLE //1x2 獨贏盤
                        else -> View.GONE
                    }

                    //跟進h5 獨贏盤 主隊以1表示
                    tv_name.text = when (gameType) {
                        GameType.TN.key, GameType.VB.key, GameType.TT.key, GameType.CK.key, GameType.BM.key -> "1"
                        GameType.IH.key, GameType.BX.key, GameType.CB.key, GameType.BB.key, GameType.RB.key, GameType.MR.key, GameType.AFT.key -> "1"
                        else -> ""
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

                        setupOdd(oddList?.getOrNull(1), oddsType)

                        setOnClickListener {
                            if (oddList != null && oddList?.size ?: 0 >= 2) {
                                oddList?.getOrNull(1)?.let { odd ->
                                    onClickOddListener?.onClickBet(
                                        data,
                                        odd,
                                        playCateName.toString(),
                                        itemView.tv_play_type.text.toString(),
                                        data.betPlayCateNameMap
                                    )
                                }
                            }

                        }
                    }

                    tv_name.visibility = when (gameType) {
                        GameType.FT.key, GameType.BK.key -> View.GONE
                        GameType.TN.key, GameType.VB.key, GameType.TT.key, GameType.CK.key, GameType.BM.key -> View.VISIBLE //1x2 獨贏盤
                        else -> View.GONE
                    }

                    //跟進h5 獨贏盤 主隊以1表示
                    tv_name.text = when (gameType) {
                        GameType.TN.key, GameType.VB.key, GameType.TT.key, GameType.CK.key, GameType.BM.key -> "1"
                        GameType.IH.key, GameType.BX.key, GameType.CB.key, GameType.BB.key, GameType.RB.key, GameType.MR.key, GameType.AFT.key -> "1"
                        else -> ""
                    }
                }

                btn_match_odd3.apply {
                    isSelected = if (oddList.isNullOrEmpty() || oddList?.size ?: 0 < 2) {
                        false
                    } else {
                        oddList?.getOrNull(2)?.isSelected ?: false
                    }


                    betStatus = if (oddList.isNullOrEmpty() || oddList?.size ?: 0 < 2) {
                        BetStatus.DEACTIVATED.code
                    } else {
                        oddList?.getOrNull(2)?.status ?: BetStatus.LOCKED.code
                    }

                    if (!oddList.isNullOrEmpty() && oddList?.size ?: 0 >= 2) {
                        this@ViewHolderHdpOu.setupOddState(this, oddList?.getOrNull(2))

                        setupOdd(oddList?.getOrNull(2), oddsType)

                        setOnClickListener {
                            if (oddList != null && oddList?.size ?: 0 >= 2) {
                                oddList?.getOrNull(2)?.let { odd ->
                                    onClickOddListener?.onClickBet(
                                        data,
                                        odd,
                                        playCateName.toString(),
                                        itemView.tv_play_type.text.toString(),
                                        data.betPlayCateNameMap
                                    )
                                }
                            }

                        }
                    }

                    tv_name.visibility = when (gameType) {
                        GameType.FT.key, GameType.BK.key -> View.GONE
                        GameType.TN.key, GameType.VB.key, GameType.TT.key, GameType.CK.key, GameType.BM.key -> View.VISIBLE //1x2 獨贏盤
                        else -> View.GONE
                    }

                    //跟進h5 獨贏盤 主隊以1表示
                    tv_name.text = when (gameType) {
                        GameType.TN.key, GameType.VB.key, GameType.TT.key, GameType.CK.key, GameType.BM.key -> "1"
                        GameType.IH.key, GameType.BX.key, GameType.CB.key, GameType.BB.key, GameType.RB.key, GameType.MR.key, GameType.AFT.key -> "1"
                        else -> ""
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