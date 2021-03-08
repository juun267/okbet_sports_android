package org.cxct.sportlottery.ui.game.v3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_sport_type.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.sport.Item

class SportTypeAdapter : RecyclerView.Adapter<SportTypeAdapter.ViewHolder>() {

    var data = listOf<Item>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var sportTypeListener: SportTypeListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, sportTypeListener)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Item, sportTypeListener: SportTypeListener?) {

            setupSportTypeImage(item)

            itemView.sport_type_count.text = item.num.toString()

            itemView.sport_type_text.text = item.name

            itemView.isSelected = item.isSelected

            itemView.setOnClickListener {
                sportTypeListener?.onClick(item)
            }
        }

        private fun setupSportTypeImage(item: Item) {
            when (item.code) {
                SportType.FOOTBALL.code -> {
                    itemView.sport_type_img.setImageResource(R.drawable.selector_gametype_row_ft)
                }
                SportType.BASKETBALL.code -> {
                    itemView.sport_type_img.setImageResource(R.drawable.selector_gametype_row_bk)
                }
                SportType.BADMINTON.code -> {
                    itemView.sport_type_img.setImageResource(R.drawable.selector_gametype_row_bm)
                }
                SportType.TENNIS.code -> {
                    itemView.sport_type_img.setImageResource(R.drawable.selector_gametype_row_tn)
                }
                SportType.VOLLEYBALL.code -> {
                    itemView.sport_type_img.setImageResource(R.drawable.selector_gametype_row_vb)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_sport_type, parent, false)

                return ViewHolder(view)
            }
        }
    }
}

class SportTypeListener(val clickListener: (item: Item) -> Unit) {
    fun onClick(item: Item) = clickListener(item)
}