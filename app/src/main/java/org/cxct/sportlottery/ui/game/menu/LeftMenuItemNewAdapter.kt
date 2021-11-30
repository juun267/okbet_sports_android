package org.cxct.sportlottery.ui.game.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_left_menu_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType

class LeftMenuItemNewAdapter(
    private val itemSelectedListener: ItemSelectedListener,
    private val sportClickListener: SportClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        TITLE, ITEM, FOOTER
    }

    var dataList: List<MenuItemData> = listOf()

    fun addFooterAndSubmitList(dataList: List<MenuItemData>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val item = dataList[position]
                holder.itemView.apply {
                    img_price.setImageResource(item.imgId)
                    txv_price.text = item.title

                    cl_content.setOnClickListener {
                        sportClickListener.onClickSport(item.gameType)
                    }
                    
                    when (item.isSelected) {
                        0 -> {
                            btn_select.setImageResource(R.drawable.ic_pin_v4)
                            btn_select.setOnClickListener {
                                itemSelectedListener.onSelect(item.gameType, MyFavoriteNotifyType.SPORT_ADD.code)
                                item.isSelected = 1
                                notifyItemChanged(position)
                            }
                        }
                        1 -> {
                            btn_select.setImageResource(R.drawable.ic_pin_selected_v4)
                            btn_select.setOnClickListener {
                                itemSelectedListener.onSelect(item.gameType, MyFavoriteNotifyType.SPORT_REMOVE.code)
                                item.isSelected = 0
                                notifyItemChanged(position)
                            }
                        }
                    }
                }
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
    }

    class ItemSelectedListener(private val itemSelectedListener: (string: String, type: Int) -> Unit) {
        fun onSelect(string: String, type: Int) = itemSelectedListener(string, type)
    }

    class SportClickListener(val clickSportListener: (gameType: String) -> Unit) {
        fun onClickSport(gameType: String) = clickSportListener(gameType)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
