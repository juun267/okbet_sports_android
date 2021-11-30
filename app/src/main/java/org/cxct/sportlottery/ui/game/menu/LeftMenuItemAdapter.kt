package org.cxct.sportlottery.ui.game.menu

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_left_menu_item.view.*
import org.cxct.sportlottery.R


class LeftMenuItemAdapter(
    private val clickListener: ItemClickListener,
    private val sportClickListener: SportClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var data = mutableListOf<MenuItemData>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun addData(list: MutableList<MenuItemData>) {
        data.addAll(list)
        Log.e(">>>","list size = ${list.size}, data size = ${data.size}")
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

        fun bind(
            item: MenuItemData,
            clickListener: ItemClickListener,
            sportClickListener: SportClickListener
        ) {
            itemView.apply {
                when (item.isSelected) {
                    0 -> {
                        cl_content.apply {
                            setOnClickListener {
                                sportClickListener.onClickSport(item.gameType)
                            }
                            visibility = View.VISIBLE
                        }
                        img_price.setImageResource(item.imgId)
                        txv_price.text = item.title
                        btn_select.setImageResource(R.drawable.ic_pin_v4)
                        btn_select.setOnClickListener {
                            clickListener.onClick(item.gameType)
                        }
                        setVisibility(true)
                    }
                    1 -> {
                        cl_content.visibility = View.GONE
                        setVisibility(false)
                    }
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

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val data = data[position]
                holder.bind(data, clickListener, sportClickListener)
            }
        }
    }

    class ItemClickListener(private val clickListener: (string: String) -> Unit) {
        fun onClick(string: String) = clickListener(string)
    }

    class SportClickListener(val clickSportListener: (gameType: String) -> Unit) {
        fun onClickSport(gameType: String) = clickSportListener(gameType)
    }


}