package org.cxct.sportlottery.ui.home.news

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R

class NewsAdapter(width: Int, private val clickListener: ItemClickListener) :
    RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    private var mSelectedPosition = 0

    val data = mutableListOf("游戏公告", "会员福利", "转账须知", "劲爆推荐", "导航网", "其他")

    private val tabWidth = width

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, tabWidth)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
        holder.itemView.isSelected = mSelectedPosition == position //選中改變背景
        holder.txvTab.setOnClickListener {
            if (position != mSelectedPosition) {
                mSelectedPosition = position
                notifyDataSetChanged()
                clickListener.onClick(position + 1)
            }
        }
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txvTab: TextView = itemView.findViewById(R.id.txv_tab)

        fun bind(item: String) {
            txvTab.text = item

        }

        companion object {
            fun from(parent: ViewGroup, width: Int): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_news_tab, parent, false)

                view.layoutParams.width = width
                return ViewHolder(view)
            }
        }
    }

    class ItemClickListener(private val clickListener: (position: Int) -> Unit) {
        fun onClick(position: Int) = clickListener(position)
    }

}