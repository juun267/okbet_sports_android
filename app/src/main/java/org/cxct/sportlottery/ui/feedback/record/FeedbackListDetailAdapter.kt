package org.cxct.sportlottery.ui.feedback.record

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.feedback.FeedBackRows
import org.cxct.sportlottery.util.TimeUtil

class FeedbackListDetailAdapter(var userId: Long) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        MINE_DATA, SERVICE_REPAY
    }

    var data = mutableListOf<FeedBackRows>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var iconUrl: String ?= null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        return when (data[position].userId?.toLong() == userId) {
            true -> ItemType.MINE_DATA.ordinal
            false -> ItemType.SERVICE_REPAY.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.MINE_DATA.ordinal -> MineViewHolder.from(parent, iconUrl)
            ItemType.SERVICE_REPAY.ordinal -> ServiceViewHolder.from(parent)
            else -> ServiceViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        when (holder) {
            is MineViewHolder -> {
                holder.bind(item)
            }
            is ServiceViewHolder -> {
                holder.bind(item)
            }
        }
    }

    override fun getItemCount(): Int = data.size

    class MineViewHolder(itemView: View, val iconUrl: String?) : RecyclerView.ViewHolder(itemView) {


        private val txvReply: TextView = itemView.findViewById(R.id.txv_reply)
        private val txvReplyTime: TextView = itemView.findViewById(R.id.txv_reply_time)

        fun bind(item: FeedBackRows) {
            txvReply.text = item.content
            txvReplyTime.text = TimeUtil.stampToDateHMS(item.addTime ?: 0)


        }

        companion object {
            fun from(parent: ViewGroup, iconUrl: String?): MineViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.content_feedback_record_detail_rv_right, parent, false)
                return MineViewHolder(view, iconUrl)
            }
        }
    }

    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val txvReply: TextView = itemView.findViewById(R.id.txv_reply)
        private val txvReplyTime: TextView = itemView.findViewById(R.id.txv_reply_time)

        fun bind(item: FeedBackRows) {
            txvReply.text = item.content
            txvReplyTime.text = TimeUtil.stampToDateHMS(item.addTime ?: 0)
        }

        companion object {
            fun from(parent: ViewGroup): ServiceViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_feedback_record_detail_rv_left, parent, false)
                return ServiceViewHolder(view)
            }
        }
    }

}