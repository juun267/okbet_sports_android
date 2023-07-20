package org.cxct.sportlottery.ui.sport.favorite

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.enums.PayLoadEnum
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.sport.common.LeagueOddListener
import org.cxct.sportlottery.ui.sport.common.OddStateViewHolder
import org.cxct.sportlottery.ui.sport.vh.SportFavoriteViewHolder
import org.cxct.sportlottery.ui.sport.vh.ViewHolderTimer

class SportFavoriteAdapter(private val matchType: MatchType) :
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
    var playSelectedCodeSelectionType: Int? = null
    var playSelectedCode: String? = null
    var isNeedRecreateViews = true
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

    // region Update functions
    fun update() {
        // Update MatchOdd list
        data.forEachIndexed { index, matchOdd -> notifyItemChanged(index, matchOdd) }
    }
    // endregion

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

    fun updateBySelectCsTab(matchOdd: MatchOdd) {
        val index = data.indexOf(data.find { it == matchOdd })
        notifyItemChanged(index, matchOdd)
    }

    fun updateByMatchIdForOdds(matchOdd: MatchOdd) {
        val index = data.indexOf(data.find { it == matchOdd })
        notifyItemChanged(index, matchOdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_sport_favorite, parent, false)
        return SportFavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
//        Log.d("Hewie", "綁定：賽事($position)")
        val matchInfoList = data.mapNotNull {
            it.matchInfo
        }

        when (holder) {
            is SportFavoriteViewHolder -> {
                holder.stopTimer()
                holder.bind(
                    matchType,
                    item,
                    leagueOddListener,
                    isTimerEnable,
                    oddsType,
                    matchInfoList
                )
            }
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
            //(holder as ViewHolderHdpOu).update(matchType, data[position], leagueOddListener, isTimerEnable, oddsType, playSelectedCodeSelectionType)
        } else {
//            Log.d("Hewie", "更新：賽事($position)")

            payloads.forEach { payload ->
                when (payload) {
                    is MatchOdd -> {
                        val matchOdd = payload as MatchOdd
                        (holder as SportFavoriteViewHolder).update(
                            matchType,
                            matchOdd,
                            leagueOddListener,
                            isTimerEnable,
                            oddsType,
                        )
                    }

                    is Pair<*, *> -> {
                        (payload as Pair<*, *>).apply {
                            when (first) {
                                PayLoadEnum.PAYLOAD_BET_INFO -> {
                                    (holder as SportFavoriteViewHolder).updateByBetInfo(
                                        item = second as MatchOdd,
                                        leagueOddListener = leagueOddListener,
                                        oddsType = oddsType,
                                    )
                                }

                                PayLoadEnum.PAYLOAD_PLAYCATE -> {
                                    (holder as SportFavoriteViewHolder).updateByPlayCate(
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
    }

    override fun getItemCount(): Int = if (data.isEmpty()) {
        1
    } else {
        data.size
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        when (holder) {
            is ViewHolderTimer -> holder.stopTimer()
        }
    }

}
