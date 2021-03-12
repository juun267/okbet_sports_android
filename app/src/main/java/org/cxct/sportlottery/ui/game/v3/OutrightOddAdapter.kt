package org.cxct.sportlottery.ui.game.v3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_outright_odd_subtitlev3.view.*
import kotlinx.android.synthetic.main.itemview_outright_oddv3.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.Odd

class OutrightOddAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    enum class ItemType {
        SUB_TITLE, ODD
    }

    var data = listOf<Any>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is Odd -> ItemType.ODD.ordinal
            else -> ItemType.SUB_TITLE.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.SUB_TITLE.ordinal -> SubTitleViewHolder.from(parent)
            else -> OddViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SubTitleViewHolder -> {
                val item = data[position] as String
                holder.bind(item)
            }
            is OddViewHolder -> {
                val item = data[position] as Odd
                holder.bind(item)
            }
        }

    }

    override fun getItemCount(): Int = data.size

    class OddViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Odd) {
            itemView.outright_odd_name.text = item.spread
        }

        companion object {
            fun from(parent: ViewGroup): OddViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_outright_oddv3, parent, false)

                return OddViewHolder(view)
            }
        }
    }

    class SubTitleViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(item: String) {
            itemView.outright_odd_subtitle.text = item
        }

        companion object {
            fun from(parent: ViewGroup): SubTitleViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_outright_odd_subtitlev3, parent, false)

                return SubTitleViewHolder(view)
            }
        }
    }

}