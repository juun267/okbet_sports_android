package org.cxct.sportlottery.ui.game.odds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_league_odd.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.common.DividerItemDecorator

class LeagueOddAdapter : RecyclerView.Adapter<LeagueOddAdapter.ViewHolder>() {
    var data = listOf<LeagueOdd>()
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

    var itemExpandListener: ItemExpandListener? = null

    var betInfoListData: List<BetInfoListData>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, playType, matchOddListener, itemExpandListener, betInfoListData)
    }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val matchOddAdapter by lazy {
            MatchOddAdapter()
        }

        fun bind(
            item: LeagueOdd,
            playType: PlayType,
            matchOddListener: MatchOddListener?,
            itemExpandListener: ItemExpandListener?,
            betInfoListData: List<BetInfoListData>?
        ) {
            itemView.league_odd_name.text = item.league.name
            itemView.league_odd_count.text = item.matchOdds.size.toString()

            setupMatchOddList(
                item,
                playType, matchOddListener, betInfoListData
            )

            setupMatchOddExpand(item, adapterPosition, itemExpandListener)
        }

        private fun setupMatchOddList(
            item: LeagueOdd,
            playType: PlayType,
            matchOddListener: MatchOddListener?,
            betInfoListData: List<BetInfoListData>?
        ) {
            itemView.league_odd_sub_list.apply {
                this.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                this.adapter = matchOddAdapter
                addItemDecoration(
                    DividerItemDecorator(
                        ContextCompat.getDrawable(context, R.drawable.divider_straight)
                    )
                )
            }

            if (item.isExpand) {
                matchOddAdapter.playType = playType
                matchOddAdapter.matchOddListener = matchOddListener
                matchOddAdapter.data = item.matchOdds
                matchOddAdapter.betInfoListData = betInfoListData
            }
        }

        private fun setupMatchOddExpand(
            item: LeagueOdd,
            position: Int,
            itemExpandListener: ItemExpandListener?
        ) {
            itemView.league_odd_sub_expand.setExpanded(item.isExpand, false)
            itemView.setOnClickListener {
                item.isExpand = !item.isExpand
                itemView.league_odd_sub_expand.setExpanded(item.isExpand, true)
                itemExpandListener?.onItemExpand(item.isExpand, item, position)
                updateArrowExpand()
            }
        }

        private fun updateArrowExpand() {
            when (itemView.league_odd_sub_expand.isExpanded) {
                true -> itemView.league_odd_arrow.setImageResource(R.drawable.ic_arrow_dark)
                false -> itemView.league_odd_arrow.setImageResource(R.drawable.ic_arrow_down_dark)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_league_odd, parent, false)

                return ViewHolder(view)
            }
        }
    }
}

class ItemExpandListener(val clickListener: (isExpand: Boolean, leagueOdd: LeagueOdd, position: Int) -> Unit) {
    fun onItemExpand(isExpand: Boolean, leagueOdd: LeagueOdd, position: Int) =
        clickListener(isExpand, leagueOdd, position)
}