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
            itemView.play_category_name.apply {
                text = item.name

                setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    if (item.playCateList?.size ?: 0 > 1) {
                        when (item.isSelected) {
                            true -> {
                                R.drawable.ic_arrow_blue
                            }
                            false -> {
                                R.drawable.ic_arrow_gray
                            }
                        }
                    } else {
                        0
                    },
                    0
                )
            }

            itemView.isSelected = item.isSelected

            itemView.setOnClickListener {
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