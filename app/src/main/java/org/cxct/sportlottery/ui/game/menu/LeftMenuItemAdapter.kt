package org.cxct.sportlottery.ui.game.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_left_menu_item.view.*
import org.cxct.sportlottery.R


class LeftMenuItemAdapter(private val clickListener: ItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private enum class ViewType { ITEM, FOOTER }

    var data = listOf<MenuItemData>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_left_menu_item, parent, false)
                return ItemViewHolder(view)
            }
        }

        fun bind(item: MenuItemData, clickListener: ItemClickListener) {
            when (item.isSelected) {
                0 -> {
                    itemView.cl_content.visibility = View.VISIBLE
                    itemView.img_price.setImageResource(item.imgId)
                    itemView.txv_price.text = item.title
                    itemView.btn_select.setImageResource(R.drawable.ic_pin_v4)
                    itemView.btn_select.setOnClickListener {
                        clickListener.onClick(item.sportType)
                    }
                    setVisibility(true)
                }
                1 -> {
                    itemView.cl_content.visibility = View.GONE
                    setVisibility(false)
                }
            }
        }

        private fun setVisibility(visible: Boolean) {
            val param = itemView.layoutParams as RecyclerView.LayoutParams
            if (visible) {
                param.height = LinearLayout.LayoutParams.WRAP_CONTENT
                param.width = LinearLayout.LayoutParams.MATCH_PARENT
                itemView.visibility = View.VISIBLE
            } else {
                itemView.visibility = View.GONE
                param.height = 0
                param.width = 0
            }
            itemView.layoutParams = param
        }
    }

    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun from(parent: ViewGroup): FooterViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_footer_line, parent, false)
                return FooterViewHolder(view)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (position) {
            data.size -> ViewType.FOOTER.ordinal
            else -> ViewType.ITEM.ordinal
        }
    }

    override fun getItemCount(): Int {
        return if(data.isNotEmpty()){
            data.size + 1
        }else{
            data.size
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.ITEM.ordinal -> ItemViewHolder.from(parent)
            ViewType.FOOTER.ordinal -> FooterViewHolder.from(parent)
            else -> FooterViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val data = data[position]
                holder.bind(data, clickListener)
            }
        }
    }

    class ItemClickListener(private val clickListener: (string: String) -> Unit) {
        fun onClick(string: String) = clickListener(string)
    }


}