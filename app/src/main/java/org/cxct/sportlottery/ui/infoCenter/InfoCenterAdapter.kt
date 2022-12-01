package org.cxct.sportlottery.ui.infoCenter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.util.setDateTime

class InfoCenterAdapter(private val clickListener: ItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        ITEM, FOOTER
    }

    var data = mutableListOf<InfoCenterData>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.FOOTER.ordinal -> NoDataViewHolder.from(parent)
            else -> ItemViewHolder.from(parent)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            (data.size) -> ItemType.FOOTER.ordinal
            else -> ItemType.ITEM.ordinal
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val item = data[position]
                holder.bind(item, clickListener)
            }
            is NoDataViewHolder -> {
            }
        }
    }

    fun removeItem(bean: InfoCenterData) {
        val position = data.indexOf(bean)
        if (position >= 0) {
            data.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int = data.size + 1

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val txvIndex: TextView = itemView.findViewById(R.id.txv_index)
        private val txvTitle: TextView = itemView.findViewById(R.id.txv_title)
        private val txvTime: TextView = itemView.findViewById(R.id.txv_time)
        private val llTitle: LinearLayout = itemView.findViewById(R.id.ll_title)

        fun bind(item: InfoCenterData, clickListener: ItemClickListener) {
            txvIndex.text = (adapterPosition + 1).toString()
            txvTitle.text = item.title
            txvTime.setDateTime(item.addDate?.toLong())
            llTitle.setOnClickListener {
                clickListener.onClick(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.content_infocenter_list, parent, false)
                return ItemViewHolder(view)
            }
        }
    }

    class NoDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(parent: ViewGroup) = NoDataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_footer_no_data, parent, false))
        }
    }

    fun addData(newDataList: List<InfoCenterData>) {
        data.addAll(data.size - 1, newDataList)
        notifyDataSetChanged()
    }

    class ItemClickListener(private val clickListener: (infoData: InfoCenterData) -> Unit) {
        fun onClick(infoData: InfoCenterData) = clickListener(infoData)
    }

}