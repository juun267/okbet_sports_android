package org.cxct.sportlottery.ui.maintab.accountHistory.next

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_account_history_next_context_listview_score.view.*
import org.cxct.sportlottery.R

class RoundAdapter : ListAdapter<String, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                holder.bind(getItem(position))
            }
        }
    }

    class ItemViewHolder private constructor(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: String?) {
            itemView.tv_score.text = item
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val binding = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_account_history_next_context_listview_score, parent, false)
                return ItemViewHolder(binding)
            }
        }
    }

    class DiffCallback: DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }

}