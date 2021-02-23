package org.cxct.sportlottery.ui.main.next

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_qp_sub_rv_item.view.*
import org.cxct.sportlottery.R

class DzSubRvAdapter: RecyclerView.Adapter<DzSubRvAdapter.ItemViewHolder>() {

    var dataList = listOf<Int>() //TODO Cheryl: change to gameData
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        val layout = LayoutInflater.from(viewGroup.context).inflate(R.layout.content_qp_sub_rv_item, viewGroup, false)
        return ItemViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val data = dataList[position]
        holder.bind(data)
    }

    override fun getItemCount(): Int {
        Log.e(">>>", "dataList.size = ${dataList.size} ")
        return dataList.size
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: Int) {
            itemView.apply {
                Log.e(">>>", "item = ${context.getString(data)} ")
                tv_game_name.text = context.getString(data)
//                iv_game_icon.setImageResource() //TODO Cheryl: set game icon url from gameData
            }
        }

    }

}
