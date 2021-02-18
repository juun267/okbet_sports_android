package org.cxct.sportlottery.ui.bet.record.search.result

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.bet.record.search.timeStampToDate
import org.cxct.sportlottery.util.TextUtil

@BindingAdapter("dateTime")
fun TextView.setDateTime(timeStamp: Long?) {
    timeStamp?.let {
        text = timeStampToDate(timeStamp)
    }
}

@BindingAdapter("gameStatus") //状态 0：未开始，1：比赛中，2：已结束，3：延期，4：已取消
fun TextView.setGameStatus(status: Int?) {
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


@BindingAdapter("recordStatus") //状态 1-处理中;2-成功;3-失败
fun TextView.setRecordStatus(status: Int?) {
    status?.let {
        text = when (it) {
            1 -> context.getString(R.string.recharge_state_processing)
            2 -> context.getString(R.string.recharge_state_success)
            3 -> context.getString(R.string.recharge_state_failed)
            else -> ""
        }
    }
}

@BindingAdapter("recordStatusColor") //状态 1-处理中;2-成功;3-失败
fun TextView.setRecordStatusColor(status: Int?) {
    status?.let {
        val color = when (it) {
            1 -> R.color.gray3
            2 -> R.color.green
            3 -> R.color.red
            else -> R.color.gray3
        }

        this.setTextColor(ContextCompat.getColor(context, color))
    }
}


@BindingAdapter("moneyFormat")
fun TextView.setMoneyFormat(money: Double?) {
    money?.let {
        text = TextUtil.format(it)
    }
}

@BindingAdapter("moneyFormat")
fun TextView.setMoneyFormat(money: Long?) {
    money?.let {
        text = TextUtil.format(it)
    }
}

val gameNameMap: Map<String?, Int> = mapOf(
    "CG" to R.string.plat_money,
    "DF" to R.string.third_game_df,
    "SBTY" to R.string.third_game_sbty,
    "AG" to R.string.third_game_ag,
    "IBO" to R.string.third_game_ibo,
    "CQ9" to R.string.third_game_cq9,
    "CGCP" to R.string.third_game_cgcp,
    "OGPLUS" to R.string.third_game_ogplus,
    "CR" to R.string.third_game_cr,
)

@BindingAdapter("platName")
fun TextView.setPlatName(platCode: String?) {

    platCode?.let {
        text = gameNameMap[it]?.let { stringId -> context.getString(stringId) }
    }
}

