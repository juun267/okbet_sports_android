package org.cxct.sportlottery.ui.news

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemFooterNoDataBinding
import org.cxct.sportlottery.databinding.ItemNewsBinding
import org.cxct.sportlottery.network.news.News

@SuppressLint("NotifyDataSetChanged")
class NewsAdapter(val newsListener: NewsListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var newsList: List<News> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var showAllNews: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    enum class ItemType {
        ITEM, FOOTER
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            !showAllNews && position == newsList.size -> ItemType.FOOTER.ordinal
            else -> ItemType.ITEM.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.FOOTER.ordinal -> {
                FooterViewHolder(ItemFooterNoDataBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            else -> {
                NewsViewHolder(ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NewsViewHolder -> {
                holder.bind(newsList[position])
            }
            is FooterViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemCount(): Int = if (showAllNews) newsList.size else newsList.size + 1

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

    inner class FooterViewHolder(val binding: ItemFooterNoDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.tvNoData.text =
                binding.root.context.getString(R.string.loading)

            newsListener.onLoadMoreData()
        }
    }

    class NewsListener(
        private val onClickDetail: (news: News) -> Unit,
        private val onLoadMoreData: () -> Unit
    ) {
        fun onClickDetail(news: News) = onClickDetail.invoke(news)
        fun onLoadMoreData() = onLoadMoreData.invoke()
    }
}