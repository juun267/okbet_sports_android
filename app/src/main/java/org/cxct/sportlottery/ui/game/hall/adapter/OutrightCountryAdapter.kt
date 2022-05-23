package org.cxct.sportlottery.ui.game.hall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_country.view.*
import kotlinx.android.synthetic.main.itemview_country.view.iv_country
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.outright.season.Row
import org.cxct.sportlottery.network.outright.season.Season
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.util.SvgUtil

class OutrightCountryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    enum class ItemType {
        ITEM_PIN, ITEM, NO_DATA, BOTTOM_NAVIGATION
    }

    var data = listOf<Row>()
        set(value) {
            field = value
            //notifyDataSetChanged()
        }

    var datePin = listOf<Season>()
        set(value) {
            field = value
            //notifyDataSetChanged()
        }

    var outrightCountryLeagueListener: OutrightCountryLeagueListener? = null

    override fun getItemViewType(position: Int): Int {
        return when {
            data.isEmpty() -> ItemType.NO_DATA.ordinal
            (position == 0) -> ItemType.ITEM_PIN.ordinal
            position == data.size + 1 -> ItemType.BOTTOM_NAVIGATION.ordinal
            else -> ItemType.ITEM.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM_PIN.ordinal -> {
                ItemViewHolderPin.from(parent).apply {
                    this.itemView.league_list.apply {
                        this.layoutManager =
                            SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
                    }
                }
            }

            ItemType.ITEM.ordinal -> {
                ItemViewHolder.from(parent).apply {
                    this.itemView.league_list.apply {
                        this.layoutManager =
                            SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
                    }
                }
            }

            ItemType.BOTTOM_NAVIGATION.ordinal -> {
                BottomNavigationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.home_bottom_navigation, parent, false))
            }
            else -> {
                NoDataViewHolder.from(parent)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolderPin -> {
                holder.bind(datePin, outrightCountryLeagueListener)
//                holder.itemView.view_space_top.isVisible = position == 0
            }
            is ItemViewHolder -> {
                val item = data[position - 1]
                holder.bind(item, outrightCountryLeagueListener)
            }
        }
    }

    override fun getItemCount(): Int = if (data.isEmpty()) {
        1
    } else {
        data.size + 2
    }

    class ItemViewHolderPin private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val countryLeagueAdapter by lazy {
            OutrightCountryLeagueAdapter()
        }

        fun bind(
            datePin: List<Season>,
            outrightCountryLeagueListener: OutrightCountryLeagueListener?
        ) {
            itemView.country_name.visibility = View.GONE
            itemView.iv_country.visibility = View.GONE
            itemView.country_league_expand.setExpanded(true, false)

            setupLeagueList(datePin, outrightCountryLeagueListener)
        }

        private fun setupLeagueList(
            datePin: List<Season>,
            outrightCountryLeagueListener: OutrightCountryLeagueListener?
        ) {
            itemView.league_list.apply {
                adapter = countryLeagueAdapter.apply {
                    this.outrightCountryLeagueListener = outrightCountryLeagueListener

                    data = datePin
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ItemViewHolderPin {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_country, parent, false)

                return ItemViewHolderPin(view)
            }
        }
    }

    class ItemViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val countryLeagueAdapter by lazy {
            OutrightCountryLeagueAdapter()
        }

        fun bind(item: Row, outrightCountryLeagueListener: OutrightCountryLeagueListener?) {
            itemView.apply {
                country_name.text = item.name

                if (item.icon.isNotEmpty()){
                    val countryIcon = SvgUtil.getSvgDrawable(context, item.icon)
                    iv_country.setImageDrawable(countryIcon)
                }
            }

            setupLeagueList(item, outrightCountryLeagueListener)
            setupCountryExpand(item)
        }

        private fun setupLeagueList(
            item: Row,
            outrightCountryLeagueListener: OutrightCountryLeagueListener?
        ) {
            itemView.league_list.apply {
                adapter = countryLeagueAdapter.apply {
                    this.outrightCountryLeagueListener = outrightCountryLeagueListener

                    data = if (item.searchList.isNotEmpty()) {
                        item.searchList
                    } else {
                        item.list
                    }
                }
            }
        }

        private fun setupCountryExpand(item: Row) {
            itemView.country_league_expand.setExpanded(item.isExpand, false)
            itemView.setOnClickListener {
                item.isExpand = !item.isExpand
                itemView.country_league_expand.setExpanded(item.isExpand, true)
                if(item.isExpand) {
                    itemView.SpaceItemDecorationView.visibility = View.GONE
                } else {
                    itemView.SpaceItemDecorationView.visibility = View.VISIBLE
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_country, parent, false)

                return ItemViewHolder(view)
            }
        }
    }

    class NoDataViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        companion object {
            fun from(parent: ViewGroup): NoDataViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.view_no_record_for_game, parent, false)

                return NoDataViewHolder(view)
            }
        }
    }

    class BottomNavigationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}