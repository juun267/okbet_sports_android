package org.cxct.sportlottery.ui.sport.list

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_sport_odd.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.enums.PayLoadEnum
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.sport.common.LeagueOddListener
import org.cxct.sportlottery.ui.sport.common.OddStateViewHolder
import org.cxct.sportlottery.ui.sport.vh.SportListViewHolder
import org.cxct.sportlottery.ui.sport.vh.ViewHolderTimer

class SportOddAdapter(
    private val matchType: MatchType,
    private val oddBtnCachePool: RecyclerView.RecycledViewPool,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var data = listOf<MatchOdd>()
    var oddsType: OddsType = OddsType.EU
    fun setData(data: List<MatchOdd> = listOf(), oddsType: OddsType = OddsType.EU) {
        this.data = data
        this.oddsType = oddsType
        //notifyDataSetChanged()
    }

    var isTimerEnable = false
        set(value) {
            if (value != field) {
                field = value
                //notifyDataSetChanged()
            }
        }

    var leagueOddListener: LeagueOddListener? = null
    var leagueOdd: LeagueOdd? = null
    private val oddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(data.indexOf(data.find { matchOdd ->
                    matchOdd.oddsMap?.toList()
                        ?.find { map -> map.second?.find { it == odd } != null } != null
                }))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return SportLeagueAdapter.ItemType.ITEM.ordinal
    }

    // region Update functions
    fun update() {
        data.forEachIndexed { index, matchOdd -> notifyItemChanged(index, matchOdd) }
    }

    fun updateIndex(index: Int, matchOdd: MatchOdd) {
        notifyItemChanged(index, matchOdd)
    }

    fun updateByBetInfo(clickOdd: Odd?) {
        data.forEachIndexed { index, matchOdd ->
            matchOdd.oddsMap?.values?.forEach { oddList ->
                if (oddList?.any { it?.id == clickOdd?.id } == true) {
                    notifyItemChanged(index, Pair(PayLoadEnum.PAYLOAD_BET_INFO, matchOdd))
                    leagueOddListener?.clickOdd = null
                }
            }
        }
    }

    fun updateByPlayCate() {
        data.forEachIndexed { index, matchOdd ->
            notifyItemChanged(index, Pair(PayLoadEnum.PAYLOAD_PLAYCATE, matchOdd))
        }
    }

    fun updateByMatchIdForOdds(matchOdd: MatchOdd) {
        val index = data.indexOf(data.find { it == matchOdd })
        notifyItemChanged(index, matchOdd)
    }

    fun updateBySelectCsTab(matchOdd: MatchOdd) {
        val index = data.indexOf(data.find { it == matchOdd })
        notifyItemChanged(index, matchOdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_sport_odd, parent, false)
        val vh = SportListViewHolder(view, oddStateRefreshListener)
        vh.itemView.rv_league_odd_btn_pager_main.setRecycledViewPool(oddBtnCachePool)
        return vh
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SportListViewHolder) {
            val item = data[position]
            val matchInfoList = data.mapNotNull { it.matchInfo }
            holder.bind(matchType, item, leagueOddListener, isTimerEnable, oddsType, matchInfoList)
        }
    }

    // region update by payload functions
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isNullOrEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            Log.d("Hewie", "更新：賽事($position)")

            when (payloads.first()) {
                is MatchOdd -> {
                    val matchOdd = payloads.first() as MatchOdd
                    (holder as SportListViewHolder).update(
                        matchType,
                        matchOdd,
                        leagueOddListener,
                        isTimerEnable,
                        oddsType
                    )
                }

                is Pair<*, *> -> {
                    (payloads.first() as Pair<*, *>).apply {
                        when (first) {
                            PayLoadEnum.PAYLOAD_BET_INFO -> {
                                (holder as SportListViewHolder).updateByBetInfo(
                                    item = second as MatchOdd,
                                    leagueOddListener = leagueOddListener,
                                    oddsType = oddsType,
                                )
                            }

                            PayLoadEnum.PAYLOAD_PLAYCATE -> {
                                (holder as SportListViewHolder).updateByPlayCate(
                                    item = second as MatchOdd,
                                    oddsType = oddsType,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        when (holder) {
            is ViewHolderTimer -> holder.stopTimer()
        }
    }
}
