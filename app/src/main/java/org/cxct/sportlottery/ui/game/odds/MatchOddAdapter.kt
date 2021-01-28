package org.cxct.sportlottery.ui.game.odds

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class MatchOddAdapter : RecyclerView.Adapter<MatchOddAdapter.ViewHolder>() {
    var data = listOf<MatchOdd>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var updatedOddsMap = mapOf<String, List<Odd>>()
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
//        Log.e(">>>", "before matchOdd, HDP first = ${item.odds[PlayType.HDP.code]?.firstOrNull()?.odds}")
        updateItemDataFromSocket(item)
//        Log.e(">>>", "after matchOdd, HDP first = ${item.odds[PlayType.HDP.code]?.firstOrNull()?.odds}")
        holder.bind(item, playType, matchOddListener)
    }

    private fun updateItemDataFromSocket(originItem: MatchOdd) {
        if (updatedOddsMap.isNullOrEmpty()) return
        for ((key, value) in updatedOddsMap) {
            when (key) {
                PlayType.OU.code -> {
                    value.forEach {
                        when (it.id) {
                            originItem.odds[PlayType.OU.code]?.firstOrNull()?.id -> {
                                val oddData = originItem.odds[PlayType.OU.code]?.get(0)
                                oddData?.oddState = getOddState(oddData, it)
                                originItem.odds[PlayType.OU.code]?.set(0, it)
                            }

                            originItem.odds[PlayType.OU.code]?.get(1)?.id -> {
                                val oddData = originItem.odds[PlayType.OU.code]?.get(1)
                                oddData?.oddState = getOddState(oddData, it)
                                originItem.odds[PlayType.OU.code]?.set(1, it)
                            }

                        }
                    }
                }

                PlayType.HDP.code -> {
                    value.forEach {
                        when (it.id) {
                            originItem.odds[PlayType.HDP.code]?.firstOrNull()?.id -> {
                                val oddData = originItem.odds[PlayType.HDP.code]?.get(0)
                                oddData?.oddState = getOddState(oddData, it)
                                originItem.odds[PlayType.HDP.code]?.set(0, it)
                            }
                            originItem.odds[PlayType.HDP.code]?.get(1)?.id -> {
                                val oddData = originItem.odds[PlayType.HDP.code]?.get(1)
                                oddData?.oddState = getOddState(oddData, it)
                                originItem.odds[PlayType.HDP.code]?.set(1, it)
                            }
                        }
                    }
                }

                PlayType.X12.code -> {
                    value.forEach {
                        when (it.id) {
                            originItem.odds[PlayType.X12.code]?.firstOrNull()?.id -> {
                                val oddData = originItem.odds[PlayType.X12.code]?.get(0)
                                oddData?.oddState = getOddState(oddData, it)
                                originItem.odds[PlayType.X12.code]?.set(0, it)
                            }
                            originItem.odds[PlayType.X12.code]?.get(1)?.id -> {
                                val oddData = originItem.odds[PlayType.X12.code]?.get(1)
                                oddData?.oddState = getOddState(oddData, it)
                                originItem.odds[PlayType.X12.code]?.set(1, it)
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

        fun bind(item: MatchOdd, playType: PlayType, matchOddListener: MatchOddListener?) {
            itemView.match_odd_home_name.text = item.matchInfo.homeName
            itemView.match_odd_away_name.text = item.matchInfo.awayName
            itemView.match_odd_count.text = item.matchInfo.playCateNum.toString()
            itemView.setOnClickListener {
                matchOddListener?.onClick(item)
            }

            setupMatchOddDetail(item, playType)
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

        private fun setupMatchOddDetail(item: MatchOdd, playType: PlayType) {
            when (playType) {
                PlayType.OU_HDP -> {
                    setupMatchOddOuHdp(item)
                }
                PlayType.X12 -> {
                    setupMatchOdd1x2(item)
                }
                else -> {
                }
            }
        }

        private fun setupMatchOddOuHdp(item: MatchOdd) {
            val oddListOU = item.odds[PlayType.OU.code]
            val oddListHDP = item.odds[PlayType.HDP.code]

            val oddOUHome = if (oddListOU?.size ?: 0 >= 1) oddListOU?.get(0) else null
            val oddOUAway = if (oddListOU?.size ?: 0 >= 2) oddListOU?.get(1) else null

            val oddHDPHome = if (oddListHDP?.size ?: 0 >= 1) oddListHDP?.get(0) else null
            val oddHDPAway = if (oddListHDP?.size ?: 0 >= 2) oddListHDP?.get(1) else null

            itemView.match_odd_ou_hdp.visibility = View.VISIBLE
            itemView.match_odd_1x2.visibility = View.GONE

            itemView.ou_hdp_home_name.text = item.matchInfo.homeName
            itemView.ou_hdp_away_name.text = item.matchInfo.awayName

            oddOUHome?.let {
                itemView.ou_hdp_home_ou.bet_top_text.text = it.spread
                itemView.ou_hdp_home_ou.bet_bottom_text.text = it.odds.toString()
                itemView.ou_hdp_home_ou.setStatus(it.odds.toString().isEmpty(), it.status)
                itemView.ou_hdp_home_ou.setHighlight(it.oddState)
            }

            oddOUAway?.let {
                itemView.ou_hdp_away_ou.bet_top_text.text = it.spread
                itemView.ou_hdp_away_ou.bet_bottom_text.text = it.odds.toString()
                itemView.ou_hdp_away_ou.setStatus(it.odds.toString().isEmpty(), it.status)
                itemView.ou_hdp_away_ou.setHighlight(it.oddState)
            }

            oddHDPHome?.let {
                itemView.ou_hdp_home_hdp.bet_top_text.text = it.spread
                itemView.ou_hdp_home_hdp.bet_bottom_text.text = it.odds.toString()
                itemView.ou_hdp_home_hdp.setStatus(it.odds.toString().isEmpty(), it.status)
                itemView.ou_hdp_home_hdp.setHighlight(it.oddState)
            }

            oddHDPAway?.let {
                itemView.ou_hdp_away_hdp.bet_top_text.text = it.spread
                itemView.ou_hdp_away_hdp.bet_bottom_text.text = it.odds.toString()
                itemView.ou_hdp_away_hdp.setStatus(it.odds.toString().isEmpty(), it.status)
                itemView.ou_hdp_away_hdp.setHighlight(it.oddState)
            }
        }

        private fun setupMatchOdd1x2(item: MatchOdd) {
            val oddList1X2 = item.odds[PlayType.X12.code]

            val oddBet1 = if (oddList1X2?.size ?: 0 >= 1) oddList1X2?.get(0) else null
            val oddBetX = if (oddList1X2?.size ?: 0 >= 2) oddList1X2?.get(1) else null
            val oddBet2 = if (oddList1X2?.size ?: 0 >= 3) oddList1X2?.get(2) else null

            itemView.match_odd_1x2.visibility = View.VISIBLE
            itemView.match_odd_ou_hdp.visibility = View.GONE

            itemView.x12_home_name.text = item.matchInfo.homeName
            itemView.x12_away_name.text = item.matchInfo.awayName

            oddBet1?.let {
                itemView.x12_bet_1.bet_bottom_text.text = it.odds.toString()
            }

            oddBetX?.let {
                itemView.x12_bet_x.bet_bottom_text.text = it.odds.toString()
            }

            oddBet2?.let {
                itemView.x12_bet_2.bet_bottom_text.text = it.odds.toString()
            }
        }

        private fun updateArrowExpand() {
            when (itemView.match_odd_expand.isExpanded) {
                true -> itemView.match_odd_arrow.setImageResource(R.drawable.ic_arrow_gray_up)
                false -> itemView.match_odd_arrow.setImageResource(R.drawable.ic_arrow_gray)
            }
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

class MatchOddListener(val clickListener: (matchOdd: MatchOdd) -> Unit) {
    fun onClick(matchOdd: MatchOdd) = clickListener(matchOdd)
}