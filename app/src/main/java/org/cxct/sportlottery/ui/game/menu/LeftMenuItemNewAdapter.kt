package org.cxct.sportlottery.ui.game.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import kotlinx.android.synthetic.main.content_left_menu_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType

class LeftMenuItemNewAdapter(
    private val itemSelectedListener: ItemSelectedListener,
    private val sportClickListener: SportClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        TITLE, ITEM, FOOTER
    } //SELECTED_ITEM, UNSELECTED_ITEM

    var dataList: List<MenuItemData> = listOf()

    fun addFooterAndSubmitList(dataList: List<MenuItemData>) {
        this.dataList = dataList

        notifyDataSetChanged()
    }

/*
    private val adapterScope = CoroutineScope(Dispatchers.Default)
    fun addFooterAndSubmitList(unselectedList: List<MenuItemData>) {
        adapterScope.launch {

            val items = listOf(DataItem.Header) +
//                    selectedList.map { DataItem.Item(it) } +
                    unselectedList.map { DataItem.Item(it) } +
                    listOf(DataItem.Footer)

//            Log.e(">>>", "unselectedList = $unselectedList")
            val itemsTest = unselectedList.map { DataItem.Item(it) }

            withContext(Dispatchers.Main) { //update in main ui thread
                submitList(itemsTest)
            }
        }
    }
*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
//                val data = getItem(position) as DataItem.Item
//                holder.bind(data, itemSelectedListener, sportClickListener)
                val item = dataList[position]
                holder.itemView.apply {
                    img_price.setImageResource(item.imgId)
                    txv_price.text = item.title

                    when (item.isSelected) {
                        0 -> {
                            btn_select.setImageResource(R.drawable.ic_pin_v4)
                            cl_content.setOnClickListener {
                                sportClickListener.onClickSport(item.gameType)
//                            visibility = View.VISIBLE
                            }
                            btn_select.setOnClickListener {
                                itemSelectedListener.onSelect(item.gameType, MyFavoriteNotifyType.SPORT_ADD.code)
                                item.isSelected = 1
                                notifyItemChanged(position)
                            }
//                        setVisibility(true)
                        }
                        1 -> {
                            btn_select.setImageResource(R.drawable.ic_pin_selected_v4)
                            btn_select.setOnClickListener {
                                itemSelectedListener.onSelect(item.gameType, MyFavoriteNotifyType.SPORT_REMOVE.code)
                                item.isSelected = 0
                                notifyItemChanged(position)
                            }
//                        cl_content.visibility = View.GONE
//                        setVisibility(false)
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
/*
        fun bind(
            item: MenuItemData,
            itemSelectedListener: ItemSelectedListener,
            sportClickListener: SportClickListener
        ) {
            itemView.apply {
                img_price.setImageResource(item.imgId)
                txv_price.text = item.title

                when (item.isSelected) {
                    0 -> {
                        btn_select.setImageResource(R.drawable.ic_pin_v4)
                        cl_content.setOnClickListener {
                            sportClickListener.onClickSport(item.gameType)
//                            visibility = View.VISIBLE
                        }
                        btn_select.setOnClickListener {
                            itemSelectedListener.onSelect(item.gameType, MyFavoriteNotifyType.SPORT_ADD.code)
                            btn_select.setImageResource(R.drawable.ic_pin_selected_v4)
                            item.isSelected = 1
                        }
//                        setVisibility(true)
                    }
                    1 -> {
                        btn_select.setImageResource(R.drawable.ic_pin_selected_v4)
                        btn_select.setOnClickListener {
                            itemSelectedListener.onSelect(item.gameType, MyFavoriteNotifyType.SPORT_REMOVE.code)
                            btn_select.setImageResource(R.drawable.ic_pin_v4)
                            item.isSelected = 0
                        }
//                        cl_content.visibility = View.GONE
//                        setVisibility(false)
                    }
                }
            }
        }
*/

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

    class ItemSelectedListener(private val itemSelectedListener: (string: String, type: Int) -> Unit) {
        fun onSelect(string: String, type: Int) = itemSelectedListener(string, type)
    }

    class SportClickListener(val clickSportListener: (gameType: String) -> Unit) {
        fun onClickSport(gameType: String) = clickSportListener(gameType)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
/*
    class DiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }
    }
    */
}
/*

sealed class DataItem {

    abstract val item: MenuItemData?

    data class Item(val itemData: MenuItemData) : DataItem() {
        override val item = itemData
    }

    object Header : DataItem() {
        override val item: MenuItemData? = null
    }

    object Footer : DataItem() {
        override val item: MenuItemData? = null
    }

}
*/
