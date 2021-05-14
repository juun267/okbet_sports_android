package org.cxct.sportlottery.ui.profileCenter.sportRecord.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemBetRecordDetailBinding
import org.cxct.sportlottery.network.bet.MatchOdd
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds


class BetDetailAdapter : ListAdapter<MatchOdd, BetDetailAdapter.ItemViewHolder>(DiffCallback()) {

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, oddsType)
        showDivider(position, holder)
    }

    private fun showDivider(position: Int, holder: ItemViewHolder) {
        if (position == itemCount - 1) holder.binding.divider.visibility = View.GONE
        else holder.binding.divider.visibility = View.VISIBLE
    }

    class ItemViewHolder private constructor(val binding: ItemBetRecordDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MatchOdd, oddsType: OddsType) {
            binding.data = data

            val odds = getOdds(data, oddsType)
            val oddStr = if (odds > 0)
                String.format(binding.root.context.getString(R.string.at_symbol, TextUtil.formatForOdd(odds)))
            else
                ""
            binding.tvPlayOdd.text = HtmlCompat.fromHtml(
                "${data.playName} ${data.spread} <font color=#E44438>${oddStr}</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )

            binding.executePendingBindings() //加上這句之後數據每次丟進來時才能夠即時更新
        }

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemBetRecordDetailBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MatchOdd>() {
        override fun areItemsTheSame(oldItem: MatchOdd, newItem: MatchOdd): Boolean {
            return oldItem.matchId == newItem.matchId
        }

        override fun areContentsTheSame(oldItem: MatchOdd, newItem: MatchOdd): Boolean {
            return oldItem == newItem
        }
    }

}
