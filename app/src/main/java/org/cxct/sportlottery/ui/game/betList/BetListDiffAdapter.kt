package org.cxct.sportlottery.ui.game.betList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ContentBetListItemBinding
import org.cxct.sportlottery.ui.bet.list.BetInfoListData

class BetListDiffAdapter : ListAdapter<BetInfoListData, RecyclerView.ViewHolder>(BetListDiffCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemData = getItem(holder.adapterPosition)
        when (holder) {
            is ViewHolder -> holder.bind(itemData)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val binding: ContentBetListItemBinding =
                    ContentBetListItemBinding.inflate(layoutInflater, viewGroup, false)
                return ViewHolder(binding.root)
            }
        }

        fun bind(itemData: BetInfoListData) {

        }
    }

}

//TODO review diff method
class BetListDiffCallBack : DiffUtil.ItemCallback<BetInfoListData>() {
    override fun areItemsTheSame(oldItem: BetInfoListData, newItem: BetInfoListData): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: BetInfoListData, newItem: BetInfoListData): Boolean {
        return oldItem == newItem
    }

}