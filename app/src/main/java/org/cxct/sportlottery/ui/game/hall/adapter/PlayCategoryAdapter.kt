package org.cxct.sportlottery.ui.game.hall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_play_category_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.sport.query.Play

class PlayCategoryAdapter : RecyclerView.Adapter<PlayCategoryAdapter.ViewHolderPlayCategory>() {
    var data = listOf<Play>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderPlayCategory {
        return ViewHolderPlayCategory.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolderPlayCategory, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    class ViewHolderPlayCategory private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(item: Play) {
            itemView.play_category_name.text = item.name
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolderPlayCategory {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_play_category_v4, parent, false)

                return ViewHolderPlayCategory(view)
            }
        }
    }
}