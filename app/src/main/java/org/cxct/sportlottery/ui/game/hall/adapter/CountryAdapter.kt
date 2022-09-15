package org.cxct.sportlottery.ui.game.hall.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_country_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.league.Row
import org.cxct.sportlottery.ui.base.BaseGameAdapter
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.util.SvgUtil

@SuppressLint("NotifyDataSetChanged")
class CountryAdapter : BaseGameAdapter() {

    private val itemPinPosition = 0

    enum class ItemType {
        ITEM_PIN, ITEM
    }

    var data = listOf<Row>()
        set(value) {
            field = value
            isPreload = false
        }

    var datePin = listOf<League>()
        set(value) {
            field = value
            if (data.isNotEmpty())
                notifyItemChanged(itemPinPosition)
        }

    var countryLeagueListener: CountryLeagueListener? = null

    fun setPreloadItem() {
        data.toMutableList().clear()
        isPreload = true
        notifyDataSetChanged()
    }

    fun notifyCountryItem(dataPosition: Int) {
        if (data.isNotEmpty()) {
            val notifyPosition = dataPosition + 1
            if (notifyPosition <= itemCount) {
                notifyItemChanged(dataPosition + 1)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (isPreload) {
            return BaseItemType.PRELOAD_ITEM.type
        }
        return when {
            data.isEmpty() -> BaseItemType.NO_DATA.type
            position == itemPinPosition -> ItemType.ITEM_PIN.ordinal
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
            else -> initBaseViewHolders(parent, viewType)
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
        2
    } else {
        data.size + 2
    }

    class ItemViewHolderPin private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val countryLeagueAdapter by lazy {
            CountryLeagueAdapter()
        }

        fun bind(leagueList: List<League>, countryLeagueListener: CountryLeagueListener?) {
            itemView.country_border.visibility = View.GONE
            itemView.country_text.visibility = View.GONE
            itemView.iv_country.visibility = View.GONE
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
            itemView.apply {

                country_text.text = item.name

                if (item.icon.isNotEmpty()) {
                    val countryIcon = SvgUtil.getSvgDrawable(context, item.icon)
                    iv_country.setImageDrawable(countryIcon)
                }
            }
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

}