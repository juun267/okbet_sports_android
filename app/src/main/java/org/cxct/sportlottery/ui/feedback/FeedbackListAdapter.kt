package org.cxct.sportlottery.ui.feedback

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

        //        val tvType: TextView = itemView.findViewById(R.id.tv_type)
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
                        .inflate(R.layout.item_finance_no_data, parent, false)
                )
        }
    }

    class ItemClickListener(private val clickListener: (feedBackRows: FeedBackRows) -> Unit) {
        fun onClick(feedBackRows: FeedBackRows) = clickListener(feedBackRows)
    }

    fun getMsgType(type: Int): String {
        return when (type) {
            0 -> context.getString(R.string.feedback_type_recharge_problem)
            1 -> context.getString(R.string.feedback_type_withdraw_problem)
            2 -> context.getString(R.string.feedback_type_other_problem)
            3 -> context.getString(R.string.feedback_submit_enter)
            4 -> context.getString(R.string.feedback_type_complaint)
            5 -> context.getString(R.string.feedback_type_service_reply)
            6 -> context.getString(R.string.feedback_type_user_reply)
            10 -> context.getString(R.string.feedback_type_recharge_problem)
            else -> ""
        }
    }

    fun getMsgStatus(status: Int): String {
        return when (status) {
            0 -> context.getString(R.string.feedback_unreplied)
            1 -> context.getString(R.string.feedback_replied)
            else -> ""
        }
    }
}
