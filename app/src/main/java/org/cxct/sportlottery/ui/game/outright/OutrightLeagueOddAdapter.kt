package org.cxct.sportlottery.ui.game.outright

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_outright_league_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.ui.base.BaseGameAdapter
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.results.OutrightType
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.MatchOddUtil.updateOddsDiscount

@SuppressLint("NotifyDataSetChanged")
class OutrightLeagueOddAdapter : BaseGameAdapter() {

    var data: List<MatchOdd?> = listOf()
        set(value) {
            field = value
            isPreload = false
            notifyDataSetChanged()
        }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var discount: Float = 1.0F
        set(value) {
            data.forEach { matchOdd ->
                matchOdd?.oddsMap?.updateOddsDiscount(field, value)
            }

            field = value
            notifyDataSetChanged()
        }

    var outrightOddListener: OutrightOddListener? = null

    fun setPreloadItem() {
        data.toMutableList().clear()
        isPreload = true
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            OutrightType.OUTRIGHT.ordinal -> {
                LeagueOddViewHolder.from(parent)
            }
            else -> initBaseViewHolders(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LeagueOddViewHolder -> {
                holder.bind(data[position], oddsType, outrightOddListener)
            }
        }
    }

    override fun getItemCount(): Int = if (data.isEmpty()) {
        2
    } else {
        data.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (isPreload) {
            return BaseItemType.PRELOAD_ITEM.type
        }

        return when {
            data.isEmpty() -> {
                if (position == 0) {
                    BaseItemType.NO_DATA.type
                } else {
                    BaseItemType.BOTTOM_NAVIGATION.type
                }
            }
            position == (data.size) -> {
                BaseItemType.BOTTOM_NAVIGATION.type
            }
            else -> {
                OutrightType.OUTRIGHT.ordinal
            }
        }
    }

    class LeagueOddViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val outrightOddAdapter by lazy {
            OutrightOddAdapter()
        }

        fun bind(
            matchOdd: MatchOdd?,
            oddsType: OddsType,
            outrightOddListener: OutrightOddListener?
        ) {
            if (bindingAdapterPosition == 0) {
                val lp = itemView.layoutParams as RecyclerView.LayoutParams
                lp.setMargins(0, 10.dp, 0, 0)
            }

            itemView.outright_league_name.text = matchOdd?.matchInfo?.name

            itemView.outright_league_date.text = matchOdd?.startDate ?: ""

            itemView.outright_league_time.text = matchOdd?.startTime ?: ""

            itemView.outright_league_odd_list.apply {
                adapter = outrightOddAdapter.apply {
                    this.matchOdd = matchOdd
                    this.oddsType = oddsType
                    this.outrightOddListener = outrightOddListener
                }
            }

        }

        companion object {
            fun from(parent: ViewGroup): LeagueOddViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_outright_league_v4, parent, false)

                return LeagueOddViewHolder(view)
            }
        }
    }

}