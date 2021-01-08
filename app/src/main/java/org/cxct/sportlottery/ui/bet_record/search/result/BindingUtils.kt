package org.cxct.sportlottery.ui.bet_record.search.result

import android.widget.TextView
import androidx.databinding.BindingAdapter
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.ui.bet_record.search.timeStampToDate

@BindingAdapter("dateTime")
fun TextView.setDateTime(timeStamp: Long?) {
    timeStamp?.let {
        text = timeStampToDate(timeStamp)
    }
}
@BindingAdapter("status") //状态 0：未开始，1：比赛中，2：已结束，3：延期，4：已取消
fun TextView.setStatus(status: Int?) {
    status?.let {
        text = when (it) {
            0 -> context.getString(R.string.not_start_yet)
            1 -> context.getString(R.string.game_playing)
            2 -> context.getString(R.string.ended)
            3 -> context.getString(R.string.suspend)
            4 -> context.getString(R.string.canceled)
            else -> ""
        }
    }
}