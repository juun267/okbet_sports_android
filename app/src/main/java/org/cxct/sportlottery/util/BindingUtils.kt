package org.cxct.sportlottery.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import org.cxct.sportlottery.R

@BindingAdapter("dateTime")
fun TextView.setDateTime(timeStamp: Long?) {
    timeStamp?.let {
        text = TimeUtil.timeStampToDate(timeStamp)
    }
}

@BindingAdapter("date")
fun TextView.setDate(timeStamp: Long?) {
    timeStamp?.let {
        text = TimeUtil.timeStampToDay(timeStamp)
    }
}

@BindingAdapter("gameStatus") //状态 0：未开始，1：比赛中，2：已结束，3：延期，4：已取消
fun TextView.setGameStatus(status: Int?) {
}
@BindingAdapter("status")
fun TextView.setStatus(status: Int?) {
    status?.let {
        text = when (it) {
            0 -> context.getString(R.string.uncheck) //未确认
            1 -> context.getString(R.string.undone) //未结算
            2 -> context.getString(R.string.win_all) //全赢
            3 -> context.getString(R.string.win_half) //赢半
            4 -> context.getString(R.string.lose_all) //全输
            5 -> context.getString(R.string.lose_half) //输半
            6 -> context.getString(R.string.draw) //和
            7 -> context.getString(R.string.canceled) //已取消
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
            1 -> R.color.colorGray
            2 -> R.color.colorGreen
            3 -> R.color.colorRed
            else -> R.color.colorGray
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


@BindingAdapter("profitFormat")
fun TextView.setProfitFormat(money: Double?) {
    money?.let {
        text = if (it > 0.0) {
            "+${TextUtil.format(it)}"
        } else {
            TextUtil.format(it)
        }
    }
}

@BindingAdapter("profitFormatInRecord")
fun TextView.setProfitFormatInRecord(money: Double?) {

    text = when {
        money == null -> {
            "--"
        }
        money > 0.0 -> {
            "+${TextUtil.format(money)}"
        }
        else -> {
            TextUtil.format(money)
        }
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
    "KY" to R.string.third_game_ky,
)

@BindingAdapter("platName")
fun TextView.setPlatName(platCode: String?) {

    platCode?.let {
        text = if (gameNameMap[it]!= null) {
            gameNameMap[it]?.let { it1 -> context.getString(it1) }
        } else {
            platCode
        }
    }
}

@BindingAdapter("oddFormat")
fun TextView.setOddFormat(odd: Double?) {
    odd?.let {
        text = "@${TextUtil.formatForOdd(it)}"
    }
}


@BindingAdapter("moneyColor")
fun TextView.setMoneyColor(profit: Double = 0.0) {

    val color = when {
        profit > 0.0 -> R.color.colorGreen
        profit < 0.0 -> R.color.colorRed
        profit == 0.0 -> R.color.colorGray
        else -> R.color.colorGray
    }

    this.setTextColor(ContextCompat.getColor(context, color))
}

fun EditText.countTextAmount(textAmount: (Int) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(char: CharSequence, start: Int, before: Int, count: Int) {
            if (char.trim().isNotEmpty()) {
                textAmount.invoke(char.length)
            } else {
                textAmount.invoke(0)
            }
        }
    })
}