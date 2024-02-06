package org.cxct.sportlottery.ui.feedback.record

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemFeedbackRecordBinding
import org.cxct.sportlottery.network.feedback.FeedBackRows
import org.cxct.sportlottery.util.setDateChangeLineTime

class FeedbackListAdapter :
    BindingAdapter<FeedBackRows,ItemFeedbackRecordBinding>() {

    override fun onBinding(position: Int, binding: ItemFeedbackRecordBinding, item: FeedBackRows)=binding.run {
        tvTime.setDateChangeLineTime(item.lastFeedbackTime?:item.addTime)
        tvDescription.text = item.content
        tvStatus.text = tvStatus.context.getString(getMsgStatus(item.status))
    }
   private object Status {
       const val NOT_REPLY=0
       const val REPLIED=1
    }

    private fun getMsgStatus(status: Int?): Int {
        return when (status) {
            Status.NOT_REPLY -> R.string.feedback_not_reply_yet
            Status.REPLIED -> R.string.feedback_replied
            else -> R.string.feedback_not_reply_yet
        }
    }

}
