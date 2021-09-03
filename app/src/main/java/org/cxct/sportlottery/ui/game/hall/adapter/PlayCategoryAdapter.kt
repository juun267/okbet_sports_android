package org.cxct.sportlottery.ui.game.hall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_play_category_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.SelectionType
import org.cxct.sportlottery.network.sport.query.Play

class PlayCategoryAdapter : RecyclerView.Adapter<PlayCategoryAdapter.ViewHolderPlayCategory>() {
    var data = listOf<Play>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var playCategoryListener: PlayCategoryListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderPlayCategory {
        return ViewHolderPlayCategory.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolderPlayCategory, position: Int) {
        holder.bind(data[position], playCategoryListener)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolderPlayCategory private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(item: Play, playCategoryListener: PlayCategoryListener?) {
            itemView.play_name.text = item.name

            itemView.play_arrow.visibility =
                if (item.selectionType == SelectionType.SELECTABLE.code) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            itemView.play_category_name.apply {
                visibility =
                    if (item.isSelected && item.selectionType == SelectionType.SELECTABLE.code && item.playCateList?.find { it.isSelected } != null) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

                text = item.playCateList?.find { it.isSelected }?.name
            }

            itemView.isSelected = item.isSelected

            itemView.setOnClickListener {
                if (item.selectionType == SelectionType.SELECTABLE.code) {
                    item.isLocked = when {
                        item.isLocked == null || item.isSelected -> false
                        else -> true
                    }
                }
                playCategoryListener?.onClick(item)
            }
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

class PlayCategoryListener(val clickListener: (item: Play) -> Unit) {
    fun onClick(item: Play) = clickListener(item)
}