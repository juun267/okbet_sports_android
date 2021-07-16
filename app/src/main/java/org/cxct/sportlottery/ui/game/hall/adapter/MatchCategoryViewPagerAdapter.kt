package org.cxct.sportlottery.ui.game.hall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_match_category_v4.view.*
import org.cxct.sportlottery.R

class MatchCategoryViewPagerAdapter : RecyclerView.Adapter<MatchCategoryViewHolder>() {
    //TODO replace to api 12.1 date previous 1 and 2
    var data = listOf(Pair("今日賽事", 6), Pair("所有賽事", 10))
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchCategoryViewHolder {
        return MatchCategoryViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MatchCategoryViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}

class MatchCategoryViewHolder private constructor(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(item: Pair<String, Int>) {
        itemView.match_category_name.text = item.first
        itemView.match_category_count.text = item.second.toString()
    }

    companion object {
        fun from(parent: ViewGroup): MatchCategoryViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater
                .inflate(R.layout.itemview_match_category_v4, parent, false)

            return MatchCategoryViewHolder(view)
        }
    }
}