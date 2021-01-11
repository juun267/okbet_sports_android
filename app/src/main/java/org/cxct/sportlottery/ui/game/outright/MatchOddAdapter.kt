package org.cxct.sportlottery.ui.game.outright

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_sub_league.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import timber.log.Timber


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
            itemView.sub_league_name.text = item.matchInfo.id
            itemView.item_sub_league.setOnClickListener {
                //TODO open fragment for odd list
                Timber.i("enter match odd list")
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_sub_league, parent, false)

                return ViewHolder(view)
            }
        }
    }
}