package org.cxct.sportlottery.ui.game.hall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_country_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.league.Row
import org.cxct.sportlottery.ui.common.SocketLinearManager

class CountryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        ITEM_PIN, ITEM, NO_DATA
    }

    var data = listOf<Row>()
        set(value) {
            field = value

            notifyDataSetChanged()
        }

    var datePin = listOf<League>()
        set(value) {
            field = value

            notifyDataSetChanged()
        }

    var searchText = ""
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var countryLeagueListener: CountryLeagueListener? = null

    override fun getItemViewType(position: Int): Int {
        return when {
            data.isEmpty() -> ItemType.NO_DATA.ordinal
            (position == 0) -> ItemType.ITEM_PIN.ordinal
            else -> ItemType.ITEM.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM_PIN.ordinal -> {
                ItemViewHolderPin.from(parent).apply {
                    this.itemView.country_league_list.apply {
                        this.layoutManager =
                            SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
                    }
                }
            }
            ItemType.ITEM.ordinal -> {
                ItemViewHolder.from(parent).apply {
                    this.itemView.country_league_list.apply {
                        this.layoutManager =
                            SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
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
            is ItemViewHolderPin -> {
                holder.bind(datePin, countryLeagueListener)
            }
            is ItemViewHolder -> {
                val item = data[position - 1]

                holder.bind(item, countryLeagueListener)
            }
        }
    }

    override fun getItemCount(): Int = if (data.isEmpty()) {
        1
    } else {
        data.size + 1
    }

    class ItemViewHolderPin private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val countryLeagueAdapter by lazy {
            CountryLeagueAdapter()
        }

        fun bind(leagueList: List<League>, countryLeagueListener: CountryLeagueListener?) {
            itemView.country_border.visibility = View.GONE
            itemView.country_expand.setExpanded(true, false)

            setupLeagueList(leagueList, countryLeagueListener)
        }

        private fun setupLeagueList(
            leagueList: List<League>,
            countryLeagueListener: CountryLeagueListener?
        ) {
            itemView.country_league_list.apply {
                adapter = countryLeagueAdapter.apply {
                    this.countryLeagueListener = countryLeagueListener

                    data = leagueList
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ItemViewHolderPin {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_country_v4, parent, false)

                return ItemViewHolderPin(view)
            }
        }
    }

    class ItemViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val countryLeagueAdapter by lazy {
            CountryLeagueAdapter()
        }

        fun bind(item: Row, countryLeagueListener: CountryLeagueListener?) {
            itemView.country_text.text = item.name

            setupLeagueList(item, countryLeagueListener)
            setupCountryExpand(item)
        }

        private fun setupLeagueList(item: Row, countryLeagueListener: CountryLeagueListener?) {
            itemView.country_league_list.apply {
                adapter = countryLeagueAdapter.apply {
                    this.countryLeagueListener = countryLeagueListener

                    data = if (item.searchList.isNotEmpty()) {
                        item.searchList
                    } else {
                        item.list
                    }
                }
            }
        }

        private fun setupCountryExpand(item: Row) {
            itemView.country_expand.setExpanded(item.isExpand, false)
            itemView.setOnClickListener {
                item.isExpand = !item.isExpand
                itemView.country_expand.setExpanded(item.isExpand, true)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_country_v4, parent, false)

                return ItemViewHolder(view)
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