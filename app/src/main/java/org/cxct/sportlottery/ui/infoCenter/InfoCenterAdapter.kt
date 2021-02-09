package org.cxct.sportlottery.ui.infoCenter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.util.TimeUtil

class InfoCenterAdapter(private val clickListener: ItemClickListener) : RecyclerView.Adapter<InfoCenterAdapter.ViewHolder>() {

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
        holder.bind(item,clickListener)
    }

    override fun getItemCount(): Int = data?.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val txvIndex: TextView = itemView.findViewById(R.id.txv_index)
        private val txvTitle: TextView = itemView.findViewById(R.id.txv_title)
        private val txvTime: TextView = itemView.findViewById(R.id.txv_time)
        private val llTitle:LinearLayout =itemView.findViewById(R.id.ll_title)

        fun bind(item: InfoCenterData, clickListener: ItemClickListener) {
            txvIndex.text = (adapterPosition + 1).toString()
            txvTitle.text = item.title
            txvTime.text = item.addDate?.toLong()?.let { TimeUtil.stampToDate(it) }
            llTitle.setOnClickListener {
                clickListener.onClick(item)
            }
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
        data.addAll(data.size - 1, newDataList)
        notifyDataSetChanged()
    }

    class ItemClickListener(private val clickListener: (infoData: InfoCenterData) -> Unit) {
        fun onClick(infoData: InfoCenterData) = clickListener(infoData)
    }

}