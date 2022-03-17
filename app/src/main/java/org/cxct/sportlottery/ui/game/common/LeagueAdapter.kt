package org.cxct.sportlottery.ui.game.common

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.itemview_league_v5.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.FoldState
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.MatchOddUtil.updateOddsDiscount
import org.cxct.sportlottery.util.SvgUtil

class LeagueAdapter(private val matchType: MatchType, var playSelectedCodeSelectionType: Int?, var playSelectedCode: String?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        ITEM, NO_DATA, BOTTOM_NAVIGATION
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
            data.size == position -> ItemType.BOTTOM_NAVIGATION.ordinal
            else -> ItemType.ITEM.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM.ordinal -> {
                ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.itemview_league_v5, parent, false)) //itemview_league_v5
            }
            ItemType.BOTTOM_NAVIGATION.ordinal -> {
                BottomNavigationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.home_bottom_navigation, parent, false))
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
                    oddsType,
                    playSelectedCodeSelectionType,
                    playSelectedCode
                )
            }
        }
    }

    override fun getItemCount(): Int = if (data.isEmpty()) {
        1
    } else {
        data.size + 1
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        when (holder) {
            is ItemViewHolder -> {
                holder.itemView.league_odd_list.adapter = null
            }
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val leagueOddAdapter by lazy {
            LeagueOddAdapter2(matchType)
        }
        fun bind(
            item: LeagueOdd,
            matchType: MatchType,
            leagueListener: LeagueListener?,
            leagueOddListener: LeagueOddListener?,
            oddsType: OddsType,
            playSelectedCodeSelectionType: Int?,
            playSelectedCode: String?
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
            leagueOddAdapter.playSelectedCodeSelectionType = playSelectedCodeSelectionType
            leagueOddAdapter.playSelectedCode = playSelectedCode
            if(itemView.league_odd_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !itemView.league_odd_list.isComputingLayout) { leagueOddAdapter.update() }
        }
        fun updateLeagueExpand(item: LeagueOdd, matchType: MatchType) {
            //itemView.league_expand.setExpanded(item.unfold == FoldState.UNFOLD.code, false)
            //itemView.league_odd_list.visibility = if(item.unfold == FoldState.UNFOLD.code) View.VISIBLE else View.GONE
            updateTimer(matchType, item.gameType)
        }
        // endregion

        private fun setupLeagueOddList(
            item: LeagueOdd,
            leagueOddListener: LeagueOddListener?,
            oddsType: OddsType
        ) {
            itemView.league_odd_list.apply {
                //league_odd_list.itemAnimator = null
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = leagueOddAdapter.apply {
                    setData(item.searchMatchOdds.ifEmpty {
                        item.matchOdds
                    }.onEach {
                        it.matchInfo?.gameType = item.gameType?.key
                    }, oddsType)

                    this.leagueOddListener = leagueOddListener
                }
                //addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.divider_color_white8))) // TODO IllegalStateException: Cannot add item decoration during a scroll  or layout
            }
        }

        private fun setupLeagueOddExpand(item: LeagueOdd, matchType: MatchType, leagueListener: LeagueListener?) {

            //itemView.league_expand.setExpanded(item.unfold == FoldState.UNFOLD.code, false)
            Log.d("Hewie12", "data[adapterPosition].unfold -> ${data[adapterPosition].unfold}")
            itemView.league_odd_list.visibility = if(data[adapterPosition].unfold == FoldState.UNFOLD.code) View.VISIBLE else View.GONE
            checkSpaceItemDecoration()
            updateTimer(matchType, item.gameType)

//            itemView.iv_refresh.isVisible = matchType != MatchType.MY_EVENT

            itemView.iv_refresh.setOnClickListener {
                leagueListener?.onRefresh(item)
            }

            itemView.setOnClickListener {
                if(adapterPosition > data.size - 1) return@setOnClickListener
                data[adapterPosition].unfold = if (data[adapterPosition].unfold == FoldState.UNFOLD.code) { FoldState.FOLD.code } else { FoldState.UNFOLD.code } // TODO IndexOutOfBoundsException: Index: 10, Size: 5

                notifyItemChanged(adapterPosition)
                //updateTimer(matchType, item.gameType)

                leagueListener?.onClickLeague(item)
            }
        }

        private fun updateTimer(matchType: MatchType, gameType: GameType?) {
            leagueOddAdapter.isTimerEnable =
                itemView.league_odd_list.isShown && (gameType == GameType.FT || gameType == GameType.BK || matchType == MatchType.PARLAY || matchType == MatchType.AT_START || matchType == MatchType.MY_EVENT)
        }

        private fun checkSpaceItemDecoration() {
            Log.d("Hewie12", "itemView.league_odd_list.isShown(${itemView.league_odd_list.visibility}) => ${itemView.league_odd_list.isShown}")
            itemView.SpaceItemDecorationView.visibility = when(itemView.league_odd_list.visibility) {
                View.VISIBLE -> View.GONE
                View.GONE -> View.VISIBLE
                else -> View.VISIBLE
            }
            //itemView.SpaceItemDecorationView.visibility = if(itemView.league_odd_list.isShown) View.GONE else View.VISIBLE
        }
    }

    class NoDataViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

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

    class BottomNavigationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

class LeagueListener(
    val clickListenerLeague: (item: LeagueOdd) -> Unit,
    val refreshListener: (item: LeagueOdd) -> Unit
) {
    fun onClickLeague(item: LeagueOdd) = clickListenerLeague(item)
    fun onRefresh(item: LeagueOdd) = refreshListener(item)
}