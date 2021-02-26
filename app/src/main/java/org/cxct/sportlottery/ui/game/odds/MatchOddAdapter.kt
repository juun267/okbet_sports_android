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
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.util.TimeUtil

class MatchOddAdapter : RecyclerView.Adapter<MatchOddAdapter.ViewHolder>() {
    var data = listOf<MatchOdd>()
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
        holder.bind(data, item, playType, matchOddListener)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var timer: CountDownTimer

        fun bind(data: List<MatchOdd>, item: MatchOdd, playType: PlayType, matchOddListener: MatchOddListener?) {
            itemView.match_odd_home_name.text = item.matchInfo?.homeName
            itemView.match_odd_away_name.text = item.matchInfo?.awayName
            itemView.match_odd_count.text = item.matchInfo?.playCateNum.toString()
            itemView.setOnClickListener {
                matchOddListener?.onItemClick(item, data)
            }

            setupMatchOddDetail(item, playType, matchOddListener)
            setupMatchOddDetailExpand(item, matchOddListener)
        }

        private fun setupMatchOddDetailExpand(item: MatchOdd, matchOddListener: MatchOddListener?) {
            itemView.match_odd_expand.setExpanded(item.isExpand, false)
            itemView.match_odd_arrow.setOnClickListener {
                matchOddListener?.onItemExpand(item)
            }
            updateArrowExpand()
        }

        private fun setupMatchOddDetail(
            item: MatchOdd,
            playType: PlayType,
            matchOddListener: MatchOddListener?
        ) {
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

            var oddBet1: Odd? = null
            var oddBetX: Odd? = null
            var oddBet2: Odd? = null

            when (oddList1X2?.size ?: 0) {
                2 -> { //tennis badminton volleyball
                    oddBet1 = if (oddList1X2?.size ?: 0 >= 1) oddList1X2?.get(0) else null
                    oddBet2 = if (oddList1X2?.size ?: 0 >= 2) oddList1X2?.get(1) else null
                }
                3 -> { //football basketball
                    oddBet1 = if (oddList1X2?.size ?: 0 >= 1) oddList1X2?.get(0) else null
                    oddBetX = if (oddList1X2?.size ?: 0 >= 2) oddList1X2?.get(1) else null
                    oddBet2 = if (oddList1X2?.size ?: 0 >= 3) oddList1X2?.get(2) else null
                }
            }

            itemView.match_odd_1x2.visibility = View.VISIBLE
            itemView.match_odd_ou_hdp.visibility = View.GONE

            itemView.x12_home_name.text = item.matchInfo?.homeName
            itemView.x12_away_name.text = item.matchInfo?.awayName

            if (oddBet1 == null) {
                itemView.x12_bet_1.visibility = View.INVISIBLE
            }
            if (oddBetX == null) {
                itemView.x12_bet_x.visibility = View.INVISIBLE
            }
            if (oddBet2 == null) {
                itemView.x12_bet_2.visibility = View.INVISIBLE
            }

            oddBet1?.let {
                itemView.x12_bet_1.apply {
                    isSelected = it.isSelected ?: false
                    it.odds?.let { odd -> bet_bottom_text.text = TextUtil.formatForOdd(odd) }
                    setStatus(it.odds?.isNaN() ?: true, it.status)
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
                    setStatus(it.odds?.isNaN() ?: true, it.status)
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
                    setStatus(it.odds?.isNaN() ?: true, it.status)
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

class MatchOddListener(
    val clickListener: (matchOdd: MatchOdd, data: List<MatchOdd>) -> Unit,
    val expandListener: (matchOdd: MatchOdd) -> Unit,
    val betClickListener: (matchOdd: MatchOdd, oddString: String, odd: Odd) -> Unit
) {
    fun onItemClick(matchOdd: MatchOdd, data: List<MatchOdd>) = clickListener(matchOdd, data)
    fun onItemExpand(matchOdd: MatchOdd) = expandListener(matchOdd)
    fun onBet(matchOdd: MatchOdd, oddString: String, odd: Odd) =
        betClickListener(matchOdd, oddString, odd)
}
