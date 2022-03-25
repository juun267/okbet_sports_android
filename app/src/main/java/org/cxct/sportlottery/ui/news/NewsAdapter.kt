package org.cxct.sportlottery.ui.news

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ItemNewsBinding
import org.cxct.sportlottery.network.news.News

class NewsAdapter(val newsListener: NewsListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var newsList: List<News> = listOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return NewsViewHolder(ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemData = newsList[position]
        when (holder) {
            is NewsViewHolder -> {
                holder.bind(itemData)
            }
        }
    }

    override fun getItemCount(): Int = 4

    inner class NewsViewHolder(val binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: News) {
            with(binding) {
                tvTitle.text = data.title

                tvTime.text = data.showDate

                tvDetail.setOnClickListener {
                    newsListener.onClickDetail(data)
                }
            }
        }
    }

    class NewsListener(private val onClickDetail: (news: News) -> Unit) {
        fun onClickDetail(news: News) = onClickDetail.invoke(news)
    }
}