package org.cxct.sportlottery.ui.game.odds

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_match_odd.view.*
import kotlinx.android.synthetic.main.play_category_1x2.view.*
import kotlinx.android.synthetic.main.play_category_bet_btn.view.*
import kotlinx.android.synthetic.main.play_category_ou_hdp.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusCO
import org.cxct.sportlottery.util.TimeUtil

class MatchOddAdapter : RecyclerView.Adapter<MatchOddAdapter.ViewHolder>() {
    var data = listOf<MatchOdd>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var updatedOddsMap = mapOf<String, List<Odd?>?>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var betInfoListData: List<BetInfoListData>? = null
        set(value) {
            field = value
            data.forEach { matchOdd ->
                matchOdd.odds.forEach { map ->
                    map.value.forEach { odd ->
                        odd.isSelected = value?.any { it.matchOdd.oddsId == odd.id } ?: false
                    }
                }
            }
            notifyDataSetChanged()
        }

    var updatedMatchStatus: MatchStatusCO? = MatchStatusCO()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var playType: PlayType = PlayType.OU_HDP
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var matchOddListener: MatchOddListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        updateItemDataFromSocket(item)
        updatedMatchStatusFromSocket(item)
        holder.bind(item, playType, matchOddListener)
    }

    private fun updatedMatchStatusFromSocket(originItem: MatchOdd) {
        val originMatchInfo = originItem.matchInfo

        if (originMatchInfo?.id == updatedMatchStatus?.matchId) {
            originItem.matchInfo?.apply {
                awayScore = updatedMatchStatus?.awayScore
                homeScore = updatedMatchStatus?.homeScore
                statusName = updatedMatchStatus?.statusName //TODO Cheryl: 這值是放ＵＩ的哪？
            }
        }

    }

    private fun addDataIfNotEnough(originItem: MatchOdd, code: String) {
        val oddList = originItem.odds[code]
        if (oddList?.size ?: 0 < 2) {
            for (i in 0 until (2 - (oddList?.size ?: 0))) {
                originItem.odds[code]?.add(Odd())
                if (updatedOddsMap[code]?.size ?: 0 > i) {
                    updatedOddsMap[code]?.get(i)?.let { originItem.odds[code]?.set(i, it) }
                }
            }
        }
    }


    private fun updateItemDataFromSocket(originItem: MatchOdd) {
        if (updatedOddsMap.isNullOrEmpty()) return

        addDataIfNotEnough(originItem, PlayType.OU.code)
        addDataIfNotEnough(originItem, PlayType.HDP.code)
        addDataIfNotEnough(originItem, PlayType.X12.code)

        for ((key, value) in updatedOddsMap) {
            when (key) {
                PlayType.OU.code -> {
                    value?.forEach { odd ->
                        val oddItem = originItem.odds[PlayType.OU.code]
                        odd?.let {
                            when (it.id) {
                                oddItem?.firstOrNull()?.id -> {
                                    val oddData = oddItem?.get(0)
                                    oddData?.oddState = getOddState(oddData, it)
                                    oddItem?.set(0, oddData ?: it)
                                }
                                oddItem?.get(1)?.id -> {
                                    val oddData = oddItem?.get(1)
                                    oddData?.oddState = getOddState(oddData, it)
                                    oddItem?.set(1, oddData ?: it)
                                }
                            }
                        }
                    }
                }

                PlayType.HDP.code -> {
                    value?.forEach { odd ->
                        val oddItem = originItem.odds[PlayType.HDP.code]
                        odd?.let {
                            when (it.id) {
                                oddItem?.firstOrNull()?.id -> {
                                    val oddData = oddItem?.get(0)
                                    oddData?.oddState = getOddState(oddData, it)
                                    oddItem?.set(0, oddData ?: it)
                                }
                                oddItem?.get(1)?.id -> {
                                    val oddData = oddItem?.get(1)
                                    oddData?.oddState = getOddState(oddData, it)
                                    oddItem?.set(1, oddData ?: it)
                                }
                            }
                        }
                    }
                }

                PlayType.X12.code -> {
                    value?.forEach { odd ->
                        val oddItem = originItem.odds[PlayType.X12.code]
                        odd?.let {
                            it.id?.let { oddId ->
                                when (oddId) {
                                    oddItem?.firstOrNull()?.id -> {
                                        val oddData = oddItem[0]
                                        oddData.oddState = getOddState(oddData, it)
                                        oddItem[0] = oddData
                                    }
                                    oddItem?.get(1)?.id -> {
                                        val oddData = oddItem[1]
                                        oddData.oddState = getOddState(oddData, it)
                                        oddItem[1] = oddData
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun getOddState(oldItem: Odd?, it: Odd): Int {
        val oldOdd = oldItem?.odds ?: 0.0
        val newOdd = it.odds ?: 0.0
        return when {
            newOdd == oldOdd -> OddState.SAME.state
            newOdd > oldOdd -> OddState.LARGER.state
            newOdd < oldOdd -> OddState.SMALLER.state
            else -> OddState.SAME.state
        }
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var timer: CountDownTimer

        fun bind(item: MatchOdd, playType: PlayType, matchOddListener: MatchOddListener?) {
            itemView.match_odd_home_name.text = item.matchInfo?.homeName
            itemView.match_odd_away_name.text = item.matchInfo?.awayName
            itemView.match_odd_count.text = item.matchInfo?.playCateNum.toString()
            itemView.setOnClickListener {
                matchOddListener?.onItemClick(item)
            }

            setupMatchOddDetail(item, playType, matchOddListener)
            setupMatchOddDetailExpand(item)
        }

        private fun setupMatchOddDetailExpand(item: MatchOdd) {
            itemView.match_odd_expand.setExpanded(item.isExpand, false)
            itemView.match_odd_arrow.setOnClickListener {
                item.isExpand = !item.isExpand
                itemView.match_odd_expand.setExpanded(item.isExpand, true)
                updateArrowExpand()
            }
        }

        private fun setupMatchOddDetail(item: MatchOdd, playType: PlayType, matchOddListener: MatchOddListener?) {
            when (playType) {
                PlayType.OU_HDP -> {
                    setupMatchOddOuHdp(item, matchOddListener)
                }
                PlayType.X12 -> {
                    setupMatchOdd1x2(item, matchOddListener) //TODO Dean : review
                }
                else -> {
                }
            }
        }

        private fun setupMatchOddOuHdp(item: MatchOdd, matchOddListener: MatchOddListener?) {
            val ouOddString = PlayType.OU.code
            val hdpOddString = PlayType.HDP.code
            val oddListOU = item.odds[ouOddString]
            val oddListHDP = item.odds[hdpOddString]

            val oddOUHome = if (oddListOU?.size ?: 0 >= 1) oddListOU?.get(0) else Odd()
            val oddOUAway = if (oddListOU?.size ?: 0 >= 2) oddListOU?.get(1) else Odd()

            val oddHDPHome = if (oddListHDP?.size ?: 0 >= 1) oddListHDP?.get(0) else Odd()
            val oddHDPAway = if (oddListHDP?.size ?: 0 >= 2) oddListHDP?.get(1) else Odd()

            itemView.match_odd_ou_hdp.visibility = View.VISIBLE
            itemView.match_odd_1x2.visibility = View.GONE

            itemView.ou_hdp_home_name.text = item.matchInfo?.homeName
            itemView.ou_hdp_away_name.text = item.matchInfo?.awayName

            oddOUHome?.let {
                itemView.ou_hdp_home_ou.apply {
                    isSelected = it.isSelected ?: false
                    bet_top_text.text = it.spread
                    it.odds?.let { odd -> bet_bottom_text.text = TextUtil.formatForOdd(odd) }
                    setOnClickListener { _ ->
//                        setHighlight(OddState.LARGER.state)
                        matchOddListener?.onBet(item, ouOddString, it)
                    }
                    setStatus(it.odds?.isNaN() ?: true, it.status)
                    setHighlight(it.oddState)
                }
            }

            oddOUAway?.let {
                itemView.ou_hdp_away_ou.apply {
                    isSelected = it.isSelected ?: false
                    bet_top_text.text = it.spread
                    it.odds?.let { odd -> bet_bottom_text.text = TextUtil.formatForOdd(odd) }
                    setOnClickListener { _ ->
                        matchOddListener?.onBet(item, ouOddString, it)
                    }
                    setStatus(it.odds?.isNaN() ?: true, it.status)
                    setHighlight(it.oddState)
                }
            }

            oddHDPHome?.let {
                itemView.ou_hdp_home_hdp.apply {
                    isSelected = it.isSelected ?: false
                    bet_top_text.text = it.spread
                    it.odds?.let { odd -> bet_bottom_text.text = TextUtil.formatForOdd(odd) }
                    setOnClickListener { _ ->
                        matchOddListener?.onBet(item, hdpOddString, it)
                    }
                    setStatus(it.odds?.isNaN() ?: true, it.status)
                    setHighlight(it.oddState)
                }
            }

            oddHDPAway?.let {
                itemView.ou_hdp_away_hdp.apply {
                    isSelected = it.isSelected ?: false
                    bet_top_text.text = it.spread
                    it.odds?.let { odd -> bet_bottom_text.text = TextUtil.formatForOdd(odd) }
                    setOnClickListener { _ ->
                        matchOddListener?.onBet(item, hdpOddString, it)
                    }
                    setStatus(it.odds?.isNaN() ?: true, it.status)
                    setHighlight(it.oddState)
                }
            }

            item.leagueTime?.let {
                updateLeagueTime(itemView.ou_hdp_game_time, it.toLong())
            }

            item.matchInfo?.apply {
                awayScore?.let {
                    itemView.ou_hdp_away_score.text = it.toString()
                }

                homeScore?.let {
                    itemView.ou_hdp_home_score.text = it.toString()
                }

                statusName?.let {
                    itemView.ou_hdp_game_state.text = it
                }
            }


        }

        private fun setupMatchOdd1x2(item: MatchOdd, matchOddListener: MatchOddListener?) {
            val odd1X2String = PlayType.X12.code
            val oddList1X2 = item.odds[odd1X2String]

            val oddBet1 = if (oddList1X2?.size ?: 0 >= 1) oddList1X2?.get(0) else null
            val oddBetX = if (oddList1X2?.size ?: 0 >= 2) oddList1X2?.get(1) else null
            val oddBet2 = if (oddList1X2?.size ?: 0 >= 3) oddList1X2?.get(2) else null

            itemView.match_odd_1x2.visibility = View.VISIBLE
            itemView.match_odd_ou_hdp.visibility = View.GONE

            itemView.x12_home_name.text = item.matchInfo?.homeName
            itemView.x12_away_name.text = item.matchInfo?.awayName

            oddBet1?.let {
                itemView.x12_bet_1.apply {
                    isSelected = it.isSelected ?: false
                    it.odds?.let { odd -> bet_bottom_text.text = TextUtil.formatForOdd(odd) }
                    setStatus(it.odds?.isNaN()?:true, it.status)
                    setHighlight(it.oddState)
                    setOnClickListener { _ ->
                        matchOddListener?.onBet(item, odd1X2String, it)
                    }
                }
            }

            oddBetX?.let {
                itemView.x12_bet_x.apply {
                    isSelected = it.isSelected ?: false
                    it.odds?.let { odd -> bet_bottom_text.text = TextUtil.formatForOdd(odd) }
                    setStatus(it.odds?.isNaN()?:true, it.status)
                    setHighlight(it.oddState)
                    setOnClickListener { _ ->
                        matchOddListener?.onBet(item, odd1X2String, it)
                    }
                }
            }

            oddBet2?.let {
                itemView.x12_bet_2.apply {
                    isSelected = it.isSelected ?: false
                    it.odds?.let { odd -> bet_bottom_text.text = TextUtil.formatForOdd(odd) }
                    setStatus(it.odds?.isNaN()?:true, it.status)
                    setHighlight(it.oddState)
                    setOnClickListener { _ ->
                        matchOddListener?.onBet(item, odd1X2String, it)
                    }
                }
            }

            item.leagueTime?.let {
                updateLeagueTime(itemView.x12_game_time, it.toLong())
            }

            item.matchInfo?.apply {
                awayScore.let {
                    itemView.ou_hdp_away_score.text = it.toString()
                }

                homeScore.let {
                    itemView.ou_hdp_home_score.text = it.toString()
                }

                statusName.let {
                    itemView.ou_hdp_game_state.text = it
                }
            }

        }

        private fun updateArrowExpand() {
            when (itemView.match_odd_expand.isExpanded) {
                true -> itemView.match_odd_arrow.setImageResource(R.drawable.ic_arrow_gray_up)
                false -> itemView.match_odd_arrow.setImageResource(R.drawable.ic_arrow_gray)
            }
        }

        private fun updateLeagueTime(textView: TextView, leagueTime: Long) {
            val upperBound = ((59 * 60 + 59) * 1000).toLong() //at most 59:59

            timer = object : CountDownTimer(upperBound, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val leagueTimeDisplay = TimeUtil.timeFormat(
                            leagueTime * 1000 + (upperBound - millisUntilFinished),
                            "mm:ss"
                    )
                    textView.text = leagueTimeDisplay
                }

                override fun onFinish() {
                }
            }.start()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.itemview_match_odd, parent, false)

                return ViewHolder(view)
            }
        }
    }
}

class MatchOddListener(val clickListener: (matchOdd: MatchOdd) -> Unit, val betClickListener: (matchOdd: MatchOdd, oddString: String, odd: Odd) -> Unit) {
    fun onItemClick(matchOdd: MatchOdd) = clickListener(matchOdd)
    fun onBet(matchOdd: MatchOdd, oddString: String, odd: Odd) = betClickListener(matchOdd, oddString, odd)
}
