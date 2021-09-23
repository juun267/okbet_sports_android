package org.cxct.sportlottery.ui.game.common

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_league_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.menu.OddsType

class LeagueAdapter(private val matchType: MatchType) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        ITEM, NO_DATA
    }

    var data = mutableListOf<LeagueOdd>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var searchText = ""
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
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
                }
            }
            else -> {
                NoDataViewHolder.from(parent, searchText)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val item = data[position]

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

            setupLeagueOddList(item, leagueOddListener, oddsType)
            setupLeagueOddExpand(item, matchType, leagueListener)
        }

        private fun setupLeagueOddList(
            item: LeagueOdd,
            leagueOddListener: LeagueOddListener?,
            oddsType: OddsType
        ) {
            itemView.league_odd_list.apply {
                adapter = leagueOddAdapter.apply {
                    data = if (item.searchMatchOdds.isNotEmpty()) {
                        item.searchMatchOdds
                    } else {
                        item.matchOdds
                    }.onEach {
                        it.matchInfo?.gameType = item.gameType?.key
                    }

                    this.leagueOddListener = leagueOddListener
                    this.oddsType = oddsType
                }
            }

            itemView.apply {
                val data =
                    String.format(context.getString(R.string.svg_format), 24, 24, 24, 24, item.league.categoryIcon)
                country_webview.loadDataWithBaseURL(null, data, "text/html", "UTF-8", null)
                country_webview.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        private fun setupLeagueOddExpand(
            item: LeagueOdd,
            matchType: MatchType,
            leagueListener: LeagueListener?
        ) {
            itemView.league_expand.setExpanded(item.isExpand, false)
            updateTimer(matchType, item.gameType)

            itemView.setOnClickListener {
                item.isExpand = !item.isExpand
                itemView.league_expand.setExpanded(item.isExpand, true)
                updateTimer(matchType, item.gameType)

                leagueListener?.onClickLeague(item)
            }
        }

        private fun updateTimer(matchType: MatchType, gameType: GameType?) {
            leagueOddAdapter.isTimerEnable =
                itemView.league_expand.isExpanded && (gameType == GameType.FT || gameType == GameType.BK || matchType == MatchType.AT_START || matchType == MatchType.MY_EVENT)
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

class LeagueListener(val clickListenerLeague: (item: LeagueOdd) -> Unit) {
    fun onClickLeague(item: LeagueOdd) = clickListenerLeague(item)
}