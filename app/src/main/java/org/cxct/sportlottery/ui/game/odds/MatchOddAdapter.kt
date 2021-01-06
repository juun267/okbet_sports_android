package org.cxct.sportlottery.ui.game.odds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_match_odd.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.MatchOdd

class MatchOddAdapter : RecyclerView.Adapter<MatchOddAdapter.ViewHolder>() {
    var data = listOf<MatchOdd>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: MatchOdd) {
            itemView.match_odd_name.text = item.matchInfo.homeName
            itemView.match_odd_count.text = item.odds.size.toString()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_match_odd, parent, false)

                return ViewHolder(view)
            }
        }
    }
}