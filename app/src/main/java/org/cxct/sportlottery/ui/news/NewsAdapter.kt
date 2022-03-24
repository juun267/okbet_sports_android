package org.cxct.sportlottery.ui.news

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ItemNewsBinding

class NewsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var newsList: List<String> = listOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return NewsViewHolder(ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NewsViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemCount(): Int = 4

    inner class NewsViewHolder(binding: ItemNewsBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind() {

        }
    }
}