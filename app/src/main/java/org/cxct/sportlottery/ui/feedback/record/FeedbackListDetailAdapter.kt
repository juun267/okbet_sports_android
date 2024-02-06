package org.cxct.sportlottery.ui.feedback.record

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.feedback.FeedBackRows
import org.cxct.sportlottery.util.TimeUtil

class FeedbackListDetailAdapter(var userId: Int) :
    BaseMultiItemQuickAdapter<FeedBackRows, BaseViewHolder>() {

    init {
        addItemType(0,R.layout.content_feedback_record_detail_rv_left)
        addItemType(userId,R.layout.content_feedback_record_detail_rv_right)
    }
    override fun convert(holder: BaseViewHolder, item: FeedBackRows) {
       if(holder.itemViewType==0){
           holder.setText(R.id.txv_reply,item.content)
           holder.setText(R.id.txv_reply_time,TimeUtil.getDateFormat12(item.addTime?:0))
       }else{
           holder.setText(R.id.txv_reply,item.content)
           holder.setText(R.id.txv_reply_time,TimeUtil.getDateFormat12(item.addTime?:0))
       }
    }

}