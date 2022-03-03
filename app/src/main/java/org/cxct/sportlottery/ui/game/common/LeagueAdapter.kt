package org.cxct.sportlottery.ui.game.common

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_league_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.FoldState
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.MatchOddUtil.updateOddsDiscount
import org.cxct.sportlottery.util.SvgUtil

class LeagueAdapter(private val matchType: MatchType) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        ITEM, NO_DATA
    }

    var data = mutableListOf<LeagueOdd>()
        set(value) {
            field = value
            //notifyDataSetChanged()
        }

    var discount: Float = 1.0F
        set(value) {
            if (field == value) return

            data.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.oddsMap?.updateOddsDiscount(field, value)
                }
            }

            field = value
            //notifyDataSetChanged()
        }

    var searchText = ""
        set(value) {
            field = value
            //notifyDataSetChanged()
        }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                //notifyDataSetChanged()
            }
        }

    var leagueListener: LeagueListener? = null

    var leagueOddListener: LeagueOddListener? = null

    override fun getItemViewType(position: Int): Int {
        return when {
            data.isEmpty() -> ItemType.NO_DATA.ordinal
            else -> ItemType.ITEM.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM.ordinal -> {
                ItemViewHolder.from(matchType, parent).apply {

                    this.itemView.league_odd_list.apply {
                        this.layoutManager =
                            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

                        addItemDecoration(
                            DividerItemDecorator(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.divider_gray
                                )
                            )
                        )
                    }
                    this.itemView.league_odd_list.itemAnimator = null
                }
            }
            else -> {
                NoDataViewHolder.from(parent, searchText)
            }
        }
    }

    // region update by payload functions
    fun updateLeague(position: Int, payload: LeagueOdd) {
        notifyItemChanged(position, payload)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if(payloads.isNullOrEmpty()) {
            onBindViewHolder(holder, position)
        }
        else {
            // Update with payload
            val leagueOdd = payloads.first() as LeagueOdd
            Log.d("Hewie", "更新：聯賽：($position) => ${leagueOdd.league.name}")
            (holder as ItemViewHolder).update(leagueOdd, matchType, oddsType)
        }
    }
    // endregion

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val item = data[position]
                Log.d("Hewie", "綁定：聯賽：($position) => ${item.league.name}")
                holder.bind(
                    item,
                    matchType,
                    leagueListener,
                    leagueOddListener,
                    oddsType
                )
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
            is ItemViewHolder -> {
                holder.itemView.league_odd_list.adapter = null
            }
        }
    }

    class ItemViewHolder private constructor(matchType: MatchType, itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val leagueOddAdapter by lazy {
            LeagueOddAdapter(matchType)
        }

        fun bind(
            item: LeagueOdd,
            matchType: MatchType,
            leagueListener: LeagueListener?,
            leagueOddListener: LeagueOddListener?,
            oddsType: OddsType
        ) {
            itemView.league_text.text = item.league.name

            if (item.league.categoryIcon.isNotEmpty()) {
                val countryIcon = SvgUtil.getSvgDrawable(itemView.context, item.league.categoryIcon)
                itemView.iv_country.setImageDrawable(countryIcon)
            }

            setupLeagueOddList(item, leagueOddListener, oddsType)
            setupLeagueOddExpand(item, matchType, leagueListener)
        }

        // region update functions
        fun update(item: LeagueOdd, matchType: MatchType, oddsType: OddsType) {
            itemView.league_text.text = item.league.name
            if (item.league.categoryIcon.isNotEmpty()) {
                val countryIcon = SvgUtil.getSvgDrawable(itemView.context, item.league.categoryIcon)
                itemView.iv_country.setImageDrawable(countryIcon)
            }
            updateLeagueOddList(item, oddsType)
            updateLeagueExpand(item, matchType)

        }
        fun updateLeagueOddList(item: LeagueOdd, oddsType: OddsType) {
            leagueOddAdapter.data = if (item.searchMatchOdds.isNotEmpty()) {
                item.searchMatchOdds
            } else { item.matchOdds }.onEach {
                it.matchInfo?.gameType = item.gameType?.key
            }
            leagueOddAdapter.oddsType = oddsType
            leagueOddAdapter.update()
        }
        fun updateLeagueExpand(item: LeagueOdd, matchType: MatchType) {
            itemView.league_expand.setExpanded(item.unfold == FoldState.UNFOLD.code, false)
            updateTimer(matchType, item.gameType)
        }
        // endregion

        private fun setupLeagueOddList(
            item: LeagueOdd,
            leagueOddListener: LeagueOddListener?,
            oddsType: OddsType
        ) {
            itemView.league_odd_list.apply {
                league_odd_list.itemAnimator = null
                adapter = leagueOddAdapter.apply {
                    setData(item.searchMatchOdds.ifEmpty {
                        item.matchOdds
                    }.onEach {
                        it.matchInfo?.gameType = item.gameType?.key
                    }, oddsType)

                    this.leagueOddListener = leagueOddListener
                }
            }
        }

        private fun setupLeagueOddExpand(
            item: LeagueOdd,
            matchType: MatchType,
            leagueListener: LeagueListener?
        ) {

            itemView.league_expand.setExpanded(item.unfold == FoldState.UNFOLD.code, false)
            updateTimer(matchType, item.gameType)

//            itemView.iv_refresh.isVisible = matchType != MatchType.MY_EVENT

            itemView.iv_refresh.setOnClickListener {
                leagueListener?.onRefresh(item)
            }

            itemView.setOnClickListener {
                item.unfold = if (item.unfold == FoldState.UNFOLD.code) {
                    FoldState.FOLD.code
                } else {
                    FoldState.UNFOLD.code
                }
                itemView.league_expand.setExpanded(item.unfold == FoldState.UNFOLD.code, true)
                updateTimer(matchType, item.gameType)

                leagueListener?.onClickLeague(item)
            }
        }

        private fun updateTimer(matchType: MatchType, gameType: GameType?) {
            leagueOddAdapter.isTimerEnable =
                itemView.league_expand.isExpanded && (gameType == GameType.FT || gameType == GameType.BK || matchType == MatchType.PARLAY || matchType == MatchType.AT_START || matchType == MatchType.MY_EVENT)
        }

        companion object {
            fun from(matchType: MatchType, parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.itemview_league_v4, parent, false)

                return ItemViewHolder(matchType, view)
            }
        }
    }

    class NoDataViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        companion object {
            fun from(parent: ViewGroup, searchText: String): NoDataViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val noDataLayoutId = if (searchText.isBlank())
                    R.layout.view_no_record
                else
                    R.layout.itemview_game_no_record
                val view = layoutInflater
                    .inflate(noDataLayoutId, parent, false)

                return NoDataViewHolder(view)
            }
        }
    }
}

class LeagueListener(
    val clickListenerLeague: (item: LeagueOdd) -> Unit,
    val refreshListener: (item: LeagueOdd) -> Unit
) {
    fun onClickLeague(item: LeagueOdd) = clickListenerLeague(item)
    fun onRefresh(item: LeagueOdd) = refreshListener(item)
}