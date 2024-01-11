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
import org.cxct.sportlottery.util.setDateChangeLineTime

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

        return  ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val item = data[position]
                holder.bind(item, clickListener)
            }
            is NoDataViewHolder -> {
                holder.bind(isFinalPage)
            }
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
       return ItemType.ITEM.ordinal
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        private val llContent: LinearLayout = itemView.findViewById(R.id.ll_content)

        enum class Status(val status: Int) {
            NOT_REPLY(0),
            REPLIED(1)
        }

        private fun getMsgStatus(status: Int?): Int {
            return when (status) {
                Status.NOT_REPLY.status -> R.string.feedback_not_reply_yet
                Status.REPLIED.status -> R.string.feedback_replied
                else -> R.string.feedback_not_reply_yet
            }
        }

        fun bind(item: FeedBackRows, clickListener: ItemClickListener) {
            tvTime.setDateChangeLineTime(item.lastFeedbackTime)
            tvDescription.text = item.content
            llContent.setOnClickListener {
                clickListener.onClick(item)
            }
            tvStatus.text = tvStatus.context.getString(getMsgStatus(item.status))
            //取消变色设置
         /*   val statusColor = if (item.status == 0) R.color.color_909090_666666 else R.color.color_08dc6e_08dc6e
            tvStatus.setTextColor(ContextCompat.getColor(tvStatus.context, statusColor))*/
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

}
