package org.cxct.sportlottery.util

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.betReceipt.BetResult
import org.cxct.sportlottery.network.common.GameMatchStatus
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.repository.KEY_DISCOUNT
import org.cxct.sportlottery.repository.NAME_LOGIN
import org.cxct.sportlottery.repository.OLD_DISCOUNT
import org.cxct.sportlottery.util.ArithUtil.round
import org.cxct.sportlottery.util.TimeUtil.MD_FORMAT
import org.cxct.sportlottery.util.TimeUtil.MD_HMS_FORMAT
import org.cxct.sportlottery.util.TimeUtil.YMD_FORMAT
import org.cxct.sportlottery.util.TimeUtil.YMD_HMS_FORMAT
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.pow

@BindingAdapter("dateTime")
fun TextView.setDateTime(timeStamp: Long?) {
    text = TimeUtil.timeFormat(timeStamp, YMD_HMS_FORMAT)
}

@BindingAdapter("date")
fun TextView.setDate(timeStamp: Long?) {
    text = TimeUtil.timeFormat(timeStamp, YMD_FORMAT)
}

@BindingAdapter("dateNoYear")
fun TextView.setDateNoYear(timeStamp: Long?) {
    text = TimeUtil.timeFormat(timeStamp, MD_FORMAT)
}

@BindingAdapter("dateNoYear")
fun TextView.setDateNoYear(date: String?) {
    text = TimeUtil.dateToDateFormat(date, MD_FORMAT)
}

@BindingAdapter("dateTimeNoYear")
fun TextView.setDateTimeNoYear(timeStamp: Long?) {
    text = TimeUtil.timeFormat(timeStamp, MD_HMS_FORMAT)
}


@BindingAdapter("gameType")
fun TextView.setGameType(gameType: String?) {
    text = when (gameType) {
        GameType.FT.key -> context.getString(GameType.FT.string)
        GameType.BK.key -> context.getString(GameType.BK.string)
        GameType.TN.key -> context.getString(GameType.TN.string)
        GameType.VB.key -> context.getString(GameType.VB.string)
        else -> ""
    }
}

@BindingAdapter("parlayType")
fun TextView.setPlayCateName(parlayType: String?) {

    parlayType?.let {
        text = when (LanguageManager.getSelectLanguage(MultiLanguagesApplication.appContext)) {
            LanguageManager.Language.ZH -> TextUtil.replaceParlayByC(it)
            else -> it
        }
    }
}

@BindingAdapter("dayOfWeek")
fun TextView.setWeekDay(timeStamp: Long?) {
    text = context.getString(TimeUtil.setupDayOfWeek(timeStamp))
}

@BindingAdapter("dayOfWeek")
fun TextView.setWeekDay(date: String?) {
    text = context.getString(TimeUtil.setupDayOfWeek(date))
}

@BindingAdapter("gameStatus") //状态 0：未开始，1：比赛中，2：已结束，3：延期，4：已取消
fun TextView.setGameStatus(status: Int?) {
    text = when (status) {
        0 -> context.getString(R.string.not_start_yet)
        1 -> context.getString(R.string.game_playing)
        2 -> context.getString(R.string.ended)
        3 -> context.getString(R.string.suspend)
        4 -> context.getString(R.string.canceled)
        else -> ""
    }
}

@BindingAdapter("betMaximumLimit")
fun TextView.setBetMaximumLimit(max: Int) {
    text = TextUtil.formatBetQuota(max)
}

fun View.setBetReceiptBackground(status: Int?) {
    background = when (status) {
        7 -> ContextCompat.getDrawable(context, R.color.colorWhite2)
        else -> ContextCompat.getDrawable(context, R.color.colorWhite)
    }
}

fun TextView.setBetReceiptAmount(itemData: BetResult) {
    text = when (itemData.status) {
        7 -> "0"
        else -> itemData.stake?.let { TextUtil.formatBetQuota(it) }
    }
}


fun TextView.setBetParlayReceiptAmount(itemData: BetResult, parlayNum: Int?) {
    text = when (itemData.status) {
        else -> if(parlayNum == 1){
            itemData.stake?.let { TextUtil.formatBetQuota(it) }
        }else{
            itemData.stake?.let { "${TextUtil.formatBetQuota(it)} * $parlayNum" }
        }
    }
}

@BindingAdapter("betReceiptStatus") //状态 0：未开始，1：比赛中，2：已结束，3：延期，4：已取消
fun TextView.setBetReceiptStatus(status: Int?) {
    text = when (status) {
        7 -> context.getString(R.string.bet_canceled)
        else -> context.getString(R.string.confirmed)
    }
}

@BindingAdapter("statusVisibility") //状态 0：未开始，1：比赛中，2：已结束，3：延期，4：已取消
fun TextView.setStatusVisibility(status: Int?) {
    visibility = when (status) {
        7 -> View.VISIBLE
        else -> View.GONE
    }
}


@BindingAdapter("hideByStatus") //状态 0：未开始，1：比赛中，2：已结束，3：延期，4：已取消
fun TextView.setHideByStatus(status: Int?) {
    visibility = when (status) {
        7 -> View.GONE
        else -> View.VISIBLE
    }
}

@BindingAdapter("receiptStatusColor") //状态 1-处理中;2-成功;3-失败
fun TextView.setReceiptStatusColor(status: Int?) {
    status?.let {
        val color = when (it) {
            7 -> R.color.colorRed
            else -> R.color.colorBlue
        }
        this.setTextColor(ContextCompat.getColor(context, color))
    }
}

fun TextView.setSingleReceiptStatusTips(status: Int?) {

    status?.let { statusNotNull ->
        text = when (statusNotNull) {
            7 -> context.getString(R.string.bet_fail)
            else -> context.getString(R.string.bet_succeeded)
        }

        setTextColor(
            when (statusNotNull) {
                7 -> ContextCompat.getColor(context, R.color.colorRed)
                else -> ContextCompat.getColor(context, R.color.colorBlue)
            }
        )
    }
}

@BindingAdapter("gameStatusColor") //状态 1-处理中;2-成功;3-失败
fun TextView.setGameStatusColor(status: Int?) {
    status?.let {
        val color = when (it) {
            0, 1, 2, 3 -> R.color.colorBlue
            else -> R.color.colorRed
        }
        this.setTextColor(ContextCompat.getColor(context, color))
    }
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

//状态 0：未确认，1：未结算，2：赢，3：赢半，4：输，5：输半，6：和，7：已取消
@BindingAdapter("betStatus", "betStatusMoney")
fun TextView.setBetStatusMoney(status: Int?, money: Double?) {
    status?.let {
        text = when (status) {
            0, 1, 7 -> context.getString(R.string.nothing)
            2, 3 -> "+${TextUtil.formatMoney(money ?: 0.0)}"
            4, 5 -> TextUtil.formatMoney(money ?: 0.0)
            else -> context.getString(R.string.draw_or_cancel)
        }

        val color = when (status) {
            0, 1, 6, 7 -> R.color.colorGrayDark
            2, 3 -> R.color.colorGreen
            else -> R.color.colorRed
        }

        this.setTextColor(ContextCompat.getColor(context, color))
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

fun Double.roundToSecond(): Double {
    return DecimalFormatUtil().doNumberFormatToDouble(this, "#.##", RoundingMode.CEILING)
}

@BindingAdapter("moneyFormat")
fun TextView.setMoneyFormat(money: Double?) {
    text = if (money == null) "-" else TextUtil.formatMoney(money)
}


@BindingAdapter(value = ["bind:profitFormat", "bind:profitTotal"], requireAll = false)
fun TextView.setProfitFormat(money: Double?, isTotal: Boolean? = false) {
    money?.let {
        text = if (it > 0.0) {
            if (isTotal == true)
                TextUtil.format(it)
            else
                "+${TextUtil.format(it)}"
        } else {
            TextUtil.format(it)
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
    "VNCP" to R.string.third_game_vncp,
    "LEG" to R.string.third_game_leg,
)

@BindingAdapter("platName")
fun TextView.setPlatName(platCode: String?) {

    platCode?.let {
        text = if (gameNameMap[it] != null) {
            gameNameMap[it]?.let { it1 -> context.getString(it1) }
        } else {
            platCode
        }
    }
}

@BindingAdapter("oddFormat")
fun TextView.setOddFormat(odd: Double?) {
    odd?.let {
        text = TextUtil.formatForOdd(it)
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


@BindingAdapter("moneyColorWhite")
fun TextView.setMoneyColorWhite(profit: Double = 0.0) {

    val color = when {
        profit >= 0.0 -> R.color.colorWhite
        profit < 0.0 -> R.color.colorRedLight
        else -> R.color.colorWhite
    }

    this.setTextColor(ContextCompat.getColor(context, color))
}


//需顯示計時器 -> [1:第一节, 2:第二节, 6:上半场, 7:下半场, 13:第一节, 14:第二节, 15:第三节, 16:第四节, 106:加时赛上半场, 107:加时赛下半场]
fun needCountStatus(status: Int?): Boolean {
    return status == GameMatchStatus.SECTION_ONE.value
            || status == GameMatchStatus.SECTION_TWO.value
            || status == GameMatchStatus.FIRST_HALF.value
            || status == GameMatchStatus.SECOND_HALF.value
            || status == GameMatchStatus.SECTION_ONE_2.value
            || status == GameMatchStatus.SECTION_TWO_2.value
            || status == GameMatchStatus.SECTION_THREE.value
            || status == GameMatchStatus.FOURTH_QUARTER.value
            || status == GameMatchStatus.OVERTIME_FIRST_HALF.value
            || status == GameMatchStatus.OVERTIME_SECOND_HALF.value
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

fun OddsChangeEvent.addOddDiscount(context: Context?): OddsChangeEvent {
    val discount = context?.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)?.getFloat(KEY_DISCOUNT, 1f) ?: 1f
    this.odds?.let { oddTypeSocketMap ->
        oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
            oddTypeSocketMapEntry.value.onEach { odd ->
                odd?.odds = odd?.odds?.div(OLD_DISCOUNT)?.times(discount.toDouble())?.roundToSecond()
                odd?.hkOdds = odd?.hkOdds?.div(OLD_DISCOUNT)?.times(discount.toDouble())?.roundToSecond()
            }
        }
    }

//    OLD_DISCOUNT = discount
    return this
}