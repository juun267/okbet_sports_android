package org.cxct.sportlottery.ui.game.hall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_eps_date_line.view.*
import kotlinx.android.synthetic.main.content_eps_league_rv.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.eps.EpsLeagueOddsItem
import org.cxct.sportlottery.network.odds.eps.EpsOdds
import org.cxct.sportlottery.network.odds.list.MatchOddsItem
import org.cxct.sportlottery.util.TimeUtil

class EpsListAdapter(private val clickListener: ItemClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private enum class ViewType { DATE, ITEM }

    var dataList = listOf<EpsLeagueOddsItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(parent: ViewGroup): DateViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_eps_date_line, parent, false)
                return DateViewHolder(view)
            }
        }

        fun bind(item: EpsLeagueOddsItem) {
            if(adapterPosition != 0 ){
                val params = itemView.ll_content.layoutParams as LinearLayout.LayoutParams
                params.setMargins(0,14,0,0)
                itemView.ll_content.layoutParams = params
            }
            itemView.tv_date.text = itemView.context.getString(TimeUtil.setupDayOfWeek(item.date))

        }
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_eps_league_rv, parent, false)
                return ItemViewHolder(view)
            }
        }

        fun bind(item: EpsLeagueOddsItem, clickListener: ItemClickListener) {
            itemView.tv_league_title.text = "${item.league?.name}"
            itemView.rv_league_odd_list.layoutManager = LinearLayoutManager(
                itemView.context,
                LinearLayoutManager.VERTICAL, false
            )

            val epsListV2Adapter = EpsListV2Adapter(EpsListV2Adapter.ItemClickListener{
                clickListener.onClick("")
            })

            itemView.rv_league_odd_list.apply {
                layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
                adapter = epsListV2Adapter.apply {
                    dataList = filterEpsOddsList(item.matchOdds)
                }
            }
        }

        private fun filterEpsOddsList(matchOddsItem: List<MatchOddsItem>?): MutableList<EpsOdds> {
            val epsOddsList = mutableListOf<EpsOdds>()
            matchOddsItem?.forEach {
                epsOddsList.add(EpsOdds(matchInfo = it.matchInfo, epsItem = null))

                it.odds?.eps?.forEach { EPSItem ->
                    epsOddsList.add(EpsOdds(matchInfo = null, epsItem = EPSItem))
                }
            }
            return epsOddsList
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataList[position].date.toInt()) {
            0 -> ViewType.ITEM.ordinal
            else -> ViewType.DATE.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.ITEM.ordinal -> ItemViewHolder.from(parent)
            ViewType.DATE.ordinal -> DateViewHolder.from(parent)
            else -> DateViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                holder.bind(dataList[position], clickListener)
            }
            is DateViewHolder -> {
                holder.bind(dataList[position])
            }
        }
    }

    override fun getItemCount() = dataList.size

    class ItemClickListener(private val clickListener: (string: String) -> Unit) {
        fun onClick(string: String) = clickListener(string)
    }
}