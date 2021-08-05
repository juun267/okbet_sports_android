package org.cxct.sportlottery.ui.game.hall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_match_category_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.today.Row

class MatchCategoryViewPagerAdapter(private val itemClickListener: OnItemClickListener) : RecyclerView.Adapter<MatchCategoryViewHolder>() {
    var data = listOf<Row>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchCategoryViewHolder {
        return MatchCategoryViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MatchCategoryViewHolder, position: Int) {
        holder.bind(itemClickListener, data[position])
    }

    override fun getItemCount(): Int = data.size
}

class OnItemClickListener(val clickListener: (data: Row) -> Unit) {
    fun onClick(data: Row) = clickListener(data)
}

class MatchCategoryViewHolder private constructor(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(itemClickListener: OnItemClickListener, item: Row) {
        itemView.match_category_name.text = item.categoryName
        itemView.match_category_count.text = item.matchNums.toString()
        itemView.setOnClickListener {
            itemClickListener.onClick(item)
        }
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