package org.cxct.sportlottery.ui.game.hall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_country.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.outright.season.Row
import org.cxct.sportlottery.network.outright.season.Season
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.common.SocketLinearManager

class OutrightCountryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    enum class ItemType {
        ITEM_PIN, ITEM, NO_DATA
    }

    var data = listOf<Row>()
        set(value) {
            field = value

            notifyDataSetChanged()
        }

    var datePin = listOf<Season>()
        set(value) {
            field = value

            notifyDataSetChanged()
        }

    var outrightCountryLeagueListener: OutrightCountryLeagueListener? = null

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
                    this.itemView.league_list.apply {
                        this.layoutManager =
                            SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)

                        this.addItemDecoration(
                            DividerItemDecorator(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.divider_color_white8
                                )
                            )
                        )
                    }
                }
            }

            ItemType.ITEM.ordinal -> {
                ItemViewHolder.from(parent).apply {
                    this.itemView.league_list.apply {
                        this.layoutManager =
                            SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)

                        this.addItemDecoration(
                            DividerItemDecorator(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.divider_color_white8
                                )
                            )
                        )
                    }
                }
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
        data.size + 1
    }

    class ItemViewHolderPin private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val countryLeagueAdapter by lazy {
            OutrightCountryLeagueAdapter()
        }

        fun bind(
            datePin: List<Season>,
            outrightCountryLeagueListener: OutrightCountryLeagueListener?
        ) {
            itemView.country_border.visibility = View.GONE
            itemView.country_name.visibility = View.GONE
            itemView.country_img.visibility = View.GONE
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
            itemView.country_name.text = item.name

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
                    .inflate(R.layout.itemview_game_no_record, parent, false)

                return NoDataViewHolder(view)
            }
        }
    }
}