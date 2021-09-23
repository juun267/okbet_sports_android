package org.cxct.sportlottery.ui.game.hall.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.caverock.androidsvg.SVG
import kotlinx.android.synthetic.main.content_eps_date_line.view.*
import kotlinx.android.synthetic.main.content_eps_league_rv.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsLeagueOddsItem
import org.cxct.sportlottery.network.odds.eps.EpsOdds
import org.cxct.sportlottery.network.odds.eps.MatchOddsItem
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TimeUtil

class EpsListAdapter(private val epsOddListener: EpsOddListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private enum class ViewType { DATE, ITEM, NO_DATA }

    var dataList = listOf<EpsLeagueOddsItem>()
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

    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(parent: ViewGroup): DateViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_eps_date_line, parent, false)
                return DateViewHolder(view)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: EpsLeagueOddsItem) {
            if(adapterPosition != 0 ){
                val params = itemView.ll_content.layoutParams as LinearLayout.LayoutParams
                params.setMargins(0,14,0,0)
                itemView.ll_content.layoutParams = params
            }
            itemView.tv_date.text = TimeUtil.stampToMD(item.date) + itemView.context.getString(TimeUtil.setupDayOfWeek(item.date))
        }
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val epsListV2Adapter by lazy {
            EpsListV2Adapter()
        }

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_eps_league_rv, parent, false)
                return ItemViewHolder(view)
            }
        }

        fun bind(item: EpsLeagueOddsItem, mOddsType: OddsType, epsOddListener: EpsOddListener) {
            itemView.apply {
                ll_league_title.setOnClickListener {
                    rv_league_odd_list.visibility = if(rv_league_odd_list.visibility == View.VISIBLE){View.GONE} else{View.VISIBLE}
                    item.isClose = !item.isClose
                    epsOddListener.clickListenerLeague(item)
                }

                item.league?.name?.let {
                    tv_league_title.text = it
                }

                val data =
                    String.format(context.getString(R.string.svg_format), 48, 48, 24, 24, item.league?.categoryIcon)
                val svgFile = SVG.getFromString(data)
                val vectorDrawable = PictureDrawable(svgFile.renderToPicture())
                iv_country.setImageDrawable(vectorDrawable)

                if (item.isClose)
                    rv_league_odd_list.visibility = View.GONE
                else
                    rv_league_odd_list.visibility = View.VISIBLE
            }

            epsListV2Adapter.epsOddListener = epsOddListener

            itemView.rv_league_odd_list.apply {
                adapter = epsListV2Adapter.apply {
                    dataList = filterEpsOddsList(item.matchOdds)
                    oddsType = mOddsType
                }
            }
        }

        private fun filterEpsOddsList(matchOddsItem: List<MatchOddsItem>?): MutableList<EpsOdds> {
            val epsOddsList = mutableListOf<EpsOdds>()
            matchOddsItem?.forEach {
                epsOddsList.add(EpsOdds(matchInfo = it.matchInfo, epsItem = null, isTitle = true))

                it.oddsEps?.eps?.forEach { EPSItem ->
                    epsOddsList.add(EpsOdds(matchInfo = it.matchInfo, epsItem = EPSItem, isTitle = false))
                }
            }
            return epsOddsList
        }
    }

    class NoDataViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        companion object {
            fun from(parent: ViewGroup): NoDataViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_no_record, parent, false)

                return NoDataViewHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            dataList.isNullOrEmpty() -> ViewType.NO_DATA.ordinal
            (dataList[position].date.toInt() == 0) -> ViewType.ITEM.ordinal
            else -> ViewType.DATE.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.NO_DATA.ordinal -> NoDataViewHolder.from(parent)
            ViewType.ITEM.ordinal -> ItemViewHolder.from(parent)
            ViewType.DATE.ordinal -> DateViewHolder.from(parent)
            else -> DateViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                holder.bind(dataList[position], oddsType, epsOddListener)
            }
            is DateViewHolder -> {
                holder.bind(dataList[position])
            }
        }
    }

    override fun getItemCount() = if (dataList.isEmpty()) {
        1
    } else {
        dataList.size
    }

    class ItemClickListener(private val clickListener: (odd: Odd) -> Unit) {
        fun onClick(odd: Odd) = clickListener(odd)
    }

    class EpsOddListener(
        val clickListenerLeague: (item: EpsLeagueOddsItem) -> Unit,
        val clickListenerBet: (odd: Odd, matchInfo: MatchInfo) -> Unit,
        val clickBetListenerInfo: (matchInfo: MatchInfo) -> Unit
    ) {
        fun onClickLeague(item: EpsLeagueOddsItem) = clickListenerLeague(item)
        fun onClickBet(odd: Odd, matchInfo: MatchInfo) = clickListenerBet(odd, matchInfo)
        fun onClickInfo(matchInfo: MatchInfo) = clickBetListenerInfo(matchInfo)
    }
}