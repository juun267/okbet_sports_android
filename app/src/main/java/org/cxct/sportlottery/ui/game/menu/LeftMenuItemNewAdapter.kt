package org.cxct.sportlottery.ui.game.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView

import kotlinx.android.synthetic.main.content_left_menu_item.view.*
import kotlinx.android.synthetic.main.content_left_menu_item_header.view.*
import org.cxct.sportlottery.R

class LeftMenuItemNewAdapter(
    private val clickListener: ItemClickListener,
    private val sportClickListener: LeftMenuItemAdapter.SportClickListener,
    private val inPlayClickListener: InPlayClickListener,
    private val premiumOddsClickListener: PremiumOddsClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var data = mutableListOf<MenuItemData>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    enum class ItemType {
        ITEM, HEADER
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) ItemType.HEADER.ordinal
        else ItemType.ITEM.ordinal
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            ItemType.HEADER.ordinal -> HeaderViewHolder.from(parent)
            else -> ItemViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val data = data[position]
                holder.bind(data, clickListener, sportClickListener)
            }
            is HeaderViewHolder -> {
                holder.bind(inPlayClickListener, premiumOddsClickListener)
            }
        }
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
            sportClickListener: LeftMenuItemAdapter.SportClickListener
        ) {
            itemView.apply {
                when (item.isSelected) {
                    0 -> {
                        cl_content.visibility = View.GONE
                        setVisibility(false)
                    }
                    1 -> {
                        cl_content.apply {
                            setOnClickListener {
                                sportClickListener.onClickSport(item.gameType)
                            }
                            visibility = View.VISIBLE
                        }
                        img_price.setImageResource(item.imgId)
                        txv_price.text = item.title
                        btn_select.setImageResource(R.drawable.ic_pin_selected_v4)
                        btn_select.setOnClickListener {
                            clickListener.onClick(item.gameType)
                        }
                        setVisibility(true)
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

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_left_menu_item_header, parent, false)
                return HeaderViewHolder(view)
            }
        }

        fun bind(
            inPlayClickListener: InPlayClickListener,
            premiumOddsClickListener: PremiumOddsClickListener
        ) {
            itemView.apply {
                ct_inplay.setOnClickListener {
                    inPlayClickListener.onClick()
                }
                ct_premium_odds.setOnClickListener {
                    premiumOddsClickListener.onClick()
                }
            }
        }
    }

    class ItemClickListener(private val clickListener: (string: String) -> Unit) {
        fun onClick(string: String) = clickListener(string)
    }

    class PremiumOddsClickListener(private val clickListener: () -> Unit) {
        fun onClick() = clickListener()
    }

    class InPlayClickListener(private val clickListener: () -> Unit) {
        fun onClick() = clickListener()
    }
}