package org.cxct.sportlottery.ui.main.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_news_item_rv.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.message.Row
import org.cxct.sportlottery.util.TimeUtil

class NewsContentAdapter : RecyclerView.Adapter<NewsContentAdapter.ViewHolder>() {

    private var mDataList = listOf<Row>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.content_news_item_rv, parent, false)
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

    fun setData(newDataList: List<Row>?) {
        mDataList = newDataList?: listOf()
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: Row) {
            itemView.apply {
                txv_title.text = data.title
                txv_content.text = data.message
                txv_time.text = TimeUtil.stampToDateHMS(data.addTime)
            }
        }
    }
}