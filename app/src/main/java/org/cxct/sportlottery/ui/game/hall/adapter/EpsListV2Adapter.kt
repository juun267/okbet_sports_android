package org.cxct.sportlottery.ui.game.hall.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_eps_list_item.view.*
import kotlinx.android.synthetic.main.content_eps_match_info_v2.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.eps.EpsOdds

class EpsListV2Adapter(private val clickListener: ItemClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private enum class ViewType { MATCHINFO, ODD }

    var dataList = listOf<EpsOdds>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class OddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: EpsOdds, clickListener: ItemClickListener) {
            itemView.tv_title.text = "${item.epsItem?.name}"
            itemView.btn_odd.setOnClickListener {
                clickListener.onClick("")
            }
        }

        companion object {
            fun from(parent: ViewGroup): OddViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_eps_list_item, parent, false)
                return OddViewHolder(view)
            }
        }
    }

    class MatchInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(matchInfo: MatchInfo?) {
            itemView.tv_game_title.text = "${matchInfo?.homeName} V ${matchInfo?.awayName}"
            itemView.line.visibility = if (adapterPosition == 0) View.GONE else View.VISIBLE
        }

        companion object {
            fun from(parent: ViewGroup): MatchInfoViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_eps_match_info_v2, parent, false)
                return MatchInfoViewHolder(view)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.MATCHINFO.ordinal -> MatchInfoViewHolder.from(parent)
            ViewType.ODD.ordinal -> OddViewHolder.from(parent)
            else -> MatchInfoViewHolder.from(parent)
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MatchInfoViewHolder -> {
                holder.bind(dataList[position].matchInfo)
            }
            is OddViewHolder -> {
                holder.bind(dataList[position],clickListener)
            }
        }
    }

    override fun getItemCount() = dataList.size

    override fun getItemViewType(position: Int): Int {
        return if (dataList[position].matchInfo != null) {
            ViewType.MATCHINFO.ordinal
        } else {
            ViewType.ODD.ordinal
        }
    }

    class ItemClickListener(private val clickListener: (string: String) -> Unit) {
        fun onClick(string: String) = clickListener(string)
    }
}