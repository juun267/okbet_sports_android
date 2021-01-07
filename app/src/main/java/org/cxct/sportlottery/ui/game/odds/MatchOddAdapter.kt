package org.cxct.sportlottery.ui.game.odds

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

class MatchOddAdapter : RecyclerView.Adapter<MatchOddAdapter.ViewHolder>() {
    var data = listOf<MatchOdd>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var playType: PlayType = PlayType.OU_HDP
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, playType)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: MatchOdd, playType: PlayType) {
            itemView.match_odd_name.text = item.matchInfo.homeName
            itemView.match_odd_count.text = item.matchInfo.playCateNum.toString()

            setupMatchOddDetail(item, playType)
            setupMatchOddDetailExpand(item)
        }

        private fun setupMatchOddDetailExpand(item: MatchOdd) {
            itemView.match_odd_expand.setExpanded(item.isExpand, false)
            itemView.setOnClickListener {
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

            val oddOUHome = oddListOU?.get(0)
            val oddOUAway = oddListOU?.get(1)

            val oddHDPHome = oddListHDP?.get(0)
            val oddHDPAway = oddListHDP?.get(1)

            itemView.match_odd_ou_hdp.visibility = View.VISIBLE
            itemView.match_odd_1x2.visibility = View.GONE

            itemView.ou_hdp_home_name.text = item.matchInfo.homeName
            itemView.ou_hdp_away_name.text = item.matchInfo.awayName

            itemView.ou_hdp_home_ou.bet_top_text.text = oddOUHome?.spread
            itemView.ou_hdp_home_ou.bet_bottom_text.text = oddOUHome?.odds.toString()
            itemView.ou_hdp_away_ou.bet_top_text.text = oddOUAway?.spread
            itemView.ou_hdp_away_ou.bet_bottom_text.text = oddOUAway?.odds.toString()

            itemView.ou_hdp_home_hdp.bet_top_text.text = oddHDPHome?.spread
            itemView.ou_hdp_home_hdp.bet_bottom_text.text = oddHDPHome?.odds.toString()
            itemView.ou_hdp_away_hdp.bet_top_text.text = oddHDPAway?.spread
            itemView.ou_hdp_away_hdp.bet_bottom_text.text = oddHDPAway?.odds.toString()
        }

        private fun setupMatchOdd1x2(item: MatchOdd) {
            val oddList1X2 = item.odds[PlayType.X12.code]

            val oddBet1 = oddList1X2?.get(0)
            val oddBetX = oddList1X2?.get(1)
            val oddBet2 = oddList1X2?.get(2)

            itemView.match_odd_1x2.visibility = View.VISIBLE
            itemView.match_odd_ou_hdp.visibility = View.GONE

            itemView.x12_home_name.text = item.matchInfo.homeName
            itemView.x12_away_name.text = item.matchInfo.awayName

            itemView.x12_bet_1.bet_bottom_text.text = oddBet1?.odds.toString()
            itemView.x12_bet_x.bet_bottom_text.text = oddBetX?.odds.toString()
            itemView.x12_bet_2.bet_bottom_text.text = oddBet2?.odds.toString()
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
                val view = layoutInflater
                    .inflate(R.layout.itemview_match_odd, parent, false)

                return ViewHolder(view)
            }
        }
    }
}