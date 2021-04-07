package org.cxct.sportlottery.ui.feedback.record

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.feedback.FeedBackRows

class FeedbackListAdapter(var context: Context, private val clickListener: ItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        ITEM, NO_DATA
    }

    var data = mutableListOf<FeedBackRows>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isFinalPage: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.NO_DATA.ordinal -> NoDataViewHolder.from(parent)
            else -> ViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                holder.tvStatus.text = data[position].type?.let { getMsgStatus(it) }
                val item = data[position]
                holder.bind(item, clickListener)
            }
            is NoDataViewHolder -> {
                holder.bind(isFinalPage)
            }
        }
    }

    override fun getItemCount(): Int = data.size + 1

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            (data.size) -> {
                ItemType.NO_DATA.ordinal
            }
            else -> ItemType.ITEM.ordinal
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        private val llContent: LinearLayout = itemView.findViewById(R.id.ll_content)

        fun bind(item: FeedBackRows, clickListener: ItemClickListener) {
            tvTime.text = item.lastFeedbackTime?.toString()
            tvDescription.text = item.content
            llContent.setOnClickListener {
                clickListener.onClick(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_feedback_record_list_rv, parent, false)
                return ViewHolder(view)
            }
        }
    }

    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(isFinalPage: Boolean) {
            itemView.visibility = if (isFinalPage) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        companion object {
            fun from(parent: ViewGroup) =
                NoDataViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_footer_no_data, parent, false)
                )
        }
    }

    class ItemClickListener(private val clickListener: (feedBackRows: FeedBackRows) -> Unit) {
        fun onClick(feedBackRows: FeedBackRows) = clickListener(feedBackRows)
    }

    private fun getMsgStatus(status: Int): String {
        return when (status) {
            0 -> context.getString(R.string.feedback_not_reply_yet)
            1 -> context.getString(R.string.feedback_replied)
            else -> ""
        }
    }
}
