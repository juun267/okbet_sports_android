package org.cxct.sportlottery.ui.game.hall.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_eps_date_line.view.*
import kotlinx.android.synthetic.main.content_eps_league_rv.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsLeagueOddsItem
import org.cxct.sportlottery.network.odds.eps.EpsOdds
import org.cxct.sportlottery.network.odds.eps.MatchOddsItem
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.MatchOddUtil.updateEpsDiscount
import org.cxct.sportlottery.util.SvgUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.DAY_FORMAT
import org.cxct.sportlottery.util.TimeUtil.VI_MD_FORMAT

class EpsListAdapter(private val epsOddListener: EpsOddListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private enum class ViewType { DATE, ITEM, NO_DATA }

    var dataList = listOf<EpsLeagueOddsItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var discount: Float = 1.0F
        set(value) {
            dataList.forEach { epsLeagueOddsItem ->
                epsLeagueOddsItem.leagueOdds?.matchOdds?.forEach { matchOddsItem ->
                    matchOddsItem.oddsEps?.updateEpsDiscount(field, value)
                }
            }

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
            itemView.tv_date.text = when (LanguageManager.getSelectLanguage(itemView.context)) {
                LanguageManager.Language.VI -> " ${TimeUtil.timeFormat(item.date, VI_MD_FORMAT)}"
                LanguageManager.Language.EN -> " ${TimeUtil.timeFormat(item.date, DAY_FORMAT)} ${TimeUtil.monthFormat(itemView.context, item.date)}"
                else -> " ${TimeUtil.timeFormat(item.date, DAY_FORMAT)} ${TimeUtil.monthFormat(itemView.context, item.date)}"
            }
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
                    rv_league_odd_list.visibility = if(rv_league_odd_list.visibility == View.VISIBLE){View.GONE} else {View.VISIBLE}
                    item.isClose = !item.isClose
                    epsOddListener.clickListenerLeague(item)
                }

                item.leagueOdds?.league?.name?.let {
                    tv_league_title.text = it
                }

                item.leagueOdds?.league?.categoryIcon?.let { iconSvg ->
                    if (iconSvg.isNotEmpty()){
                        val countryIcon = SvgUtil.getSvgDrawable(context, iconSvg)
                        iv_country.setImageDrawable(countryIcon)
                    }
                }

                if (item.isClose)
                    rv_league_odd_list.visibility = View.GONE
                else
                    rv_league_odd_list.visibility = View.VISIBLE
            }

            epsListV2Adapter.epsOddListener = epsOddListener

            itemView.rv_league_odd_list.apply {
                adapter = epsListV2Adapter.apply {
                    dataList = filterEpsOddsList(item.leagueOdds?.matchOdds)
                    oddsType = mOddsType
                }
            }
        }

        private fun filterEpsOddsList(matchOddsItem: List<MatchOddsItem>?): MutableList<EpsOdds> {
            val epsOddsList = mutableListOf<EpsOdds>()
            matchOddsItem?.forEach {
                epsOddsList.add(
                    EpsOdds(
                        betPlayCateNameMap = it.betPlayCateNameMap,
                        matchInfo = it.matchInfo,
                        epsItem = null,
                        isTitle = true
                    )
                )

                it.oddsEps?.eps?.forEach { epsItem ->
                    epsOddsList.add(
                        EpsOdds(
                            betPlayCateNameMap = it.betPlayCateNameMap,
                            matchInfo = it.matchInfo,
                            epsItem = epsItem,
                            isTitle = false
                        )
                    )
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
        val clickListenerBet: (odd: Odd, matchInfo: MatchInfo, betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?) -> Unit,
        val clickBetListenerInfo: (matchInfo: MatchInfo) -> Unit
    ) {
        fun onClickLeague(item: EpsLeagueOddsItem) = clickListenerLeague(item)
        fun onClickBet(odd: Odd, matchInfo: MatchInfo, betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?) = clickListenerBet(odd, matchInfo, betPlayCateNameMap)
        fun onClickInfo(matchInfo: MatchInfo) = clickBetListenerInfo(matchInfo)
    }
}