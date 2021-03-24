package org.cxct.sportlottery.ui.main.news

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_news_tab.view.*
import org.cxct.sportlottery.R

class NewsAdapter(val context: Context?, val clickListener: ItemClickListener) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    private var mSelectedPosition = 0
    private val mDataList = mutableListOf(
        context?.getString(R.string.game_announcement),
        context?.getString(R.string.member_benefits),
        context?.getString(R.string.transfer_notes),
        context?.getString(R.string.best_recommend),
        context?.getString(R.string.navigation_web),
        context?.getString(R.string.other)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.content_news_tab, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val data = mDataList[position]
            holder.bind(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = mDataList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: String?) {
            itemView.apply {
                txv_tab.text = data
                txv_tab.setOnClickListener {
                    if (layoutPosition != mSelectedPosition) {
                        mSelectedPosition = layoutPosition
                        notifyDataSetChanged()
                        clickListener.onClick(layoutPosition + 1)
                    }
                }

                itemView.isSelected = mSelectedPosition == layoutPosition //選中改變背景
            }
        }
    }

    class ItemClickListener(private val clickListener: (position: Int) -> Unit) {
        fun onClick(position: Int) = clickListener(position)
    }

}