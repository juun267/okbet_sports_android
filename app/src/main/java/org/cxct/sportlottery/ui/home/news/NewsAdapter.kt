package org.cxct.sportlottery.ui.home.news

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.tools.ScreenUtils.getScreenWidth
import org.cxct.sportlottery.R

class NewsAdapter(context: Context?) :
    RecyclerView.Adapter<NewsAdapter.ViewHolder>() {


    val data = mutableListOf("游戏公告", "会员福利", "转账须知", "劲爆推荐", "导航网", "其他")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txvTab: TextView = itemView.findViewById(R.id.txv_tab)

        fun bind(item: String) {
            txvTab.text = item
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_news_tab, parent, false)

                val metrics = Resources.getSystem().displayMetrics.widthPixels
                view.layoutParams.width = metrics / 4

                return ViewHolder(view)
            }
        }
    }

}