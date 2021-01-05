package org.cxct.sportlottery.ui.infoCenter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.InfoCenter.InfoCenterData

class InfoCenterAdapter : RecyclerView.Adapter<InfoCenterAdapter.ViewHolder>() {

    var data = mutableListOf<InfoCenterData>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = data?.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val txvIndex: TextView = itemView.findViewById(R.id.txv_index)
        private val txvTitle: TextView = itemView.findViewById(R.id.txv_title)
        private val txvTime: TextView = itemView.findViewById(R.id.txv_time)

        fun bind(item: InfoCenterData) {
            txvIndex.text = (adapterPosition + 1).toString()
            txvTitle.text = item.title
            txvTime.text = item.addDate
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_infocenter_list, parent, false)
                return ViewHolder(view)
            }
        }
    }

    fun addData(newDataList: MutableList<InfoCenterData>) {
        data.addAll(data.size-1, newDataList)
        notifyDataSetChanged()
    }

    fun filterData() {
        var unreadData = data.filter { it.isRead == 0 }//0未读 1已读
        var readedData = data.filter { it.isRead == 1 }
    }
}