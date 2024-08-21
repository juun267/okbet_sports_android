package org.cxct.sportlottery.util

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.network.bet.add.betReceipt.BetResult
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.util.TimeUtil.DM_HM_FORMAT
import org.cxct.sportlottery.util.TimeUtil.MD_FORMAT
import org.cxct.sportlottery.util.TimeUtil.YMD_FORMAT
import org.cxct.sportlottery.util.TimeUtil.YMD_FORMAT_2
import org.cxct.sportlottery.util.TimeUtil.YMD_HMS_FORMAT
import org.cxct.sportlottery.util.TimeUtil.YMD_HMS_FORMAT_CHANGE_LINE

@BindingAdapter("dateTime")
fun TextView.setDateTime(timeStamp: Long?) {
    text = TimeUtil.timeFormat(timeStamp, YMD_HMS_FORMAT)
}

@BindingAdapter("dateChangeLineTime")
fun TextView.setDateChangeLineTime(timeStamp: Long?) {
    text = TimeUtil.timeFormat(timeStamp, YMD_HMS_FORMAT_CHANGE_LINE)
}

@BindingAdapter("date")
fun TextView.setDate(timeStamp: Long?) {
    text = TimeUtil.timeFormat(timeStamp, YMD_FORMAT)
}


@BindingAdapter("date2")
fun TextView.setDate2(timeStamp: Long?) {
    text = TimeUtil.timeFormat(timeStamp, YMD_FORMAT_2)
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
    text = TimeUtil.timeFormat(timeStamp, DM_HM_FORMAT)
}

@SuppressLint("SetTextI18n")
@BindingAdapter(value = ["gameType", "playCateName"], requireAll = true)
fun TextView.setGameTypeWithPlayCate(gameType: String?, playCateName: String?) {
    if (gameType == null) {
        visibility = View.GONE
        return
    }
    text = "${
        when (gameType) {
            GameType.FT.key -> context.getString(GameType.FT.string)
            GameType.BK.key -> context.getString(GameType.BK.string)
            GameType.TN.key -> context.getString(GameType.TN.string)
            GameType.VB.key -> context.getString(GameType.VB.string)
            GameType.BM.key -> context.getString(GameType.BM.string)
            GameType.TT.key -> context.getString(GameType.TT.string)
            GameType.BX.key -> context.getString(GameType.BX.string)
            GameType.CB.key -> context.getString(GameType.CB.string)
            GameType.CK.key -> context.getString(GameType.CK.string)
            GameType.BB.key -> context.getString(GameType.BB.string)
            GameType.RB.key -> context.getString(GameType.RB.string)
            GameType.AFT.key -> context.getString(GameType.AFT.string)
            GameType.MR.key -> context.getString(GameType.MR.string)
            GameType.GF.key -> context.getString(GameType.GF.string)
            GameType.ES.key -> context.getString(GameType.ES.string)
            else -> ""
        }
    } $playCateName"

}

@BindingAdapter("parlayType")
fun TextView.setPlayCateName(parlayType: String?) {

    parlayType?.let {
        text = when (LanguageManager.getSelectLanguage(MultiLanguagesApplication.appContext)) {
            LanguageManager.Language.ZH -> TextUtil.getParlayShowName(this.context, it)
            else -> TextUtil.getParlayShowName(this.context, it)
        }
    }
}

//@BindingAdapter("dayOfWeek")
//fun TextView.setWeekDay(timeStamp: Long?) {
//    text = TimeUtil.setupDayOfWeek(context, timeStamp)
//}

@BindingAdapter("dayOfWeek")
fun TextView.setWeekDay(date: String?) {
    text = context.getString(TimeUtil.setupDayOfWeek(date))
}

@BindingAdapter("betMaximumLimit")
fun TextView.setBetMaximumLimit(max: Long) {
    text = TextUtil.formatBetQuota(max)
}

fun View.setBetReceiptBackground(status: Int?) {
    background = when (status) {
        7 -> ContextCompat.getDrawable(context, R.color.color_141414_f3f3f3)
        else -> ContextCompat.getDrawable(context, R.color.color_191919_FCFCFC)
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
        else -> {
//            if (parlayNum == 1) {
//                itemData.stake?.let { TextUtil.formatMoney(it) }
//            } else {
//                itemData.stake?.let { "${TextUtil.formatMoney(it)} * $parlayNum" }
//            }
            val number = parlayNum ?: 1
            itemData.stake?.let { TextUtil.formatForOdd(it * number) }
        }
    }
}

@BindingAdapter("betDetailStatusColor")
fun TextView.setBetDetailStatusColor(status: Int?) {
    status?.let {
        val color = when (it) {
            7 -> {
                R.color.color_E23434_E23434
            }
            else -> {
                R.color.color_9BB3D9_535D76
            }
        }
        this.setTextColor(ContextCompat.getColor(context, color))
    }
}

/**
 * 根據注單狀態顯示文字
 * @param cancelBy 取消触发来源 0: 自动, 1: 手动
 */
@BindingAdapter(value = ["betReceiptStatus", "betResultCancelBy"], requireAll = false) //状态 0：未开始，1：比赛中，2：已结束，3：延期，4：已取消
fun TextView.setBetReceiptStatus(status: Int?, cancelBy: String? = null) {
    text = when (status) {
        0 -> String.format(context.getString(R.string.pending), " ")
        1 -> context.getString(R.string.bet_info_add_bet_success)
        2 -> context.getString(R.string.win)
        3 -> context.getString(R.string.win_half)
        4 -> context.getString(R.string.lose)
        5 -> context.getString(R.string.lose_half)
        6 -> context.getString(R.string.settled)
        7 -> {
            when (cancelBy) {
                "0" -> context.getString(R.string.cancel_auto)
                "1" -> context.getString(R.string.cancel_manual)
                else -> context.getString(R.string.N417)
            }
        }
        8 -> context.getString(R.string.B104)
        9 -> context.getString(R.string.B105)
        else -> context.getString(R.string.confirmed)
    }
}
@BindingAdapter(value = ["betReceiptStatus", "betResultCancelBy"], requireAll = false) //状态 0：未开始，1：比赛中，2：已结束，3：延期，4：已取消
fun TextView.setBetReceiptStatus2(status: Int?, cancelBy: String? = null) {
    text = when (status) {
        0 -> context.getString(R.string.log_state_processing)
        1 -> context.getString(R.string.bet_info_add_bet_success)
        2 ,3-> context.getString(R.string.win)
        4,5 -> context.getString(R.string.lose)
        6 -> context.getString(R.string.draw)
        7 -> {
            when (cancelBy) {
                "0" -> context.getString(R.string.cancel_auto)
                "1" -> context.getString(R.string.P124)
                else -> context.getString(R.string.P124)
            }
        }
        8 -> context.getString(R.string.B104)
        9 -> context.getString(R.string.B105)
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
        val drawableRes: Int
        val color = when (it) {
            7 -> {
                drawableRes = R.drawable.ic_bet_lock_tip
                R.color.color_E23434_E23434
            }
            else -> {
                drawableRes = R.drawable.ic_bet_check_tip
                R.color.color_1D9F51_1D9F51
            }
        }
        this.setTextColor(ContextCompat.getColor(context, color))
        this.setStartDrawable(drawableRes)
        this.setTextViewDrawableColor(color)
    }
}

fun TextView.setStartDrawable(@DrawableRes id: Int = 0) {
    this.setCompoundDrawablesWithIntrinsicBounds(id, 0, 0, 0)
}

fun TextView.setTextViewDrawableColor(colorRes: Int) {
    for (drawable in this.compoundDrawables) {
        if (drawable != null) {
            drawable.colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(this.context, colorRes),
                PorterDuff.Mode.SRC_IN
            )
        }
    }
}


@BindingAdapter("gameStatusColor") //状态 1-处理中;2-成功;3-失败
fun TextView.setGameStatusColor(status: Int?) {
    status?.let {
        val color = when (it) {
            0, 1, 2, 3 -> R.color.color_317FFF_0760D4
            else -> R.color.color_E44438_e44438
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
            2 -> context.getString(R.string.win) //全赢
            3 -> context.getString(R.string.win_half) //赢半
            4 -> context.getString(R.string.lose) //全输
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
            0, 1, 6, 7 -> R.color.color_AEAEAE_404040
            2, 3 -> R.color.color_08dc6e_08dc6e
            else -> R.color.color_E44438_e44438
        }

        this.setTextColor(ContextCompat.getColor(context, color))
    }
}


@BindingAdapter("recordStatus") //状态 1-处理中;2-成功;3-失败
fun TextView.setRecordStatus(status: Int?) {
    status?.let {
        text = when (it) {
            1,4 -> context.getString(R.string.log_state_processing)
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
            1 -> R.color.color_909090_666666
            2 -> R.color.color_08dc6e_08dc6e
            3 -> R.color.color_E44438_e44438
            else -> R.color.color_909090_666666
        }
        this.setTextColor(ContextCompat.getColor(context, color))
    }
}

@BindingAdapter("moneyFormat")
fun TextView.setMoneyFormat(money: Double?) {
    text = if (money == null) "-" else TextUtil.formatMoney(money)
}


@BindingAdapter(value = ["profitFormat", "profitTotal"], requireAll = false)
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


@BindingAdapter("platName")
fun TextView.setPlatName(platCode: String?) {
    platCode?.let { code ->
        text = when (code) {
            "CG" -> context.getString(R.string.plat_money)
            else -> platCode
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
        profit > 0.0 -> R.color.color_1D9F51_1D9F51
        profit < 0.0 -> R.color.color_DB6372
        profit == 0.0 -> R.color.color_9BB3D9_535D76
        else -> R.color.color_9BB3D9_535D76
    }

    this.setTextColor(ContextCompat.getColor(context, color))
}


@BindingAdapter("moneyColorWhite")
fun TextView.setMoneyColorWhite(profit: Double = 0.0) {

    val color = when {
        profit >= 0.0 -> R.color.color_FFFFFF
        profit < 0.0 -> R.color.colorRedLight
        else -> R.color.color_FFFFFF
    }

    this.setTextColor(ContextCompat.getColor(context, color))
}


//需顯示計時器 -> [1:第一节, 2:第二节, 6:上半场, 7:下半场, 13:第一节, 14:第二节, 15:第三节, 16:第四节, 106:加时赛上半场, 107:加时赛下半场]
//31 半场状态不显示时间
fun needCountStatus(status: Int?, leagueTime: Int?): Boolean {
    if (leagueTime != null) {
        return (status ?: 0 < 99||listOf(106,107).contains(status)) && status != 31&& leagueTime > 0
    } else {
        return false
    }
//    return (status ?: 0) < 99 && status != 31
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

fun TextView.setTextTypeFace(type: Int) {
    apply {
        typeface = Typeface.create(this.typeface, type)
    }
}

fun TextView.setGameType_MatchType_PlayCateName_OddsType(gameType: String?, matchType: String?, playCateName: String?, oddsType: String?) {
    val oddsTypeStr = when (oddsType) {
        OddsType.HK.code -> "【" + context.getString(OddsType.HK.res) + "】"
        OddsType.MYS.code -> "【" + context.getString(OddsType.MYS.res) + "】"
        OddsType.IDN.code -> "【" + context.getString(OddsType.IDN.res) + "】"
        else -> "【" + context.getString(OddsType.EU.res) + "】"
    }
    text = if (matchType != null) {
        //篮球 滚球 全场让分【欧洲盘】
        "${GameType.getGameTypeString(context, gameType)} ${context.getString(MatchType.getMatchTypeStringRes(matchType))} $playCateName$oddsTypeStr"
    } else {
        "${GameType.getGameTypeString(context, gameType)} $playCateName$oddsTypeStr"
    }
}

fun TextView.setTeamsNameWithVS(
    homeName: String?,
    awayName: String?
) {
    val color_9BB3D9_535D76 = MultiLanguagesApplication.getChangeModeColorCode("#535D76", "#9BB3D9")
    val color_6C7BA8_A7B2C4 = MultiLanguagesApplication.getChangeModeColorCode("#A7B2C4", "#6C7BA8")

    val homeNameStr = if (!homeName.isNullOrEmpty()) "<font color=$color_9BB3D9_535D76>$homeName</font> " else ""
    val awayNameStr = if (!awayName.isNullOrEmpty()) "<font color=$color_9BB3D9_535D76>$awayName</font> " else ""
    val vsStr = "<font color=$color_6C7BA8_A7B2C4> ${context.getString(R.string.verse_upper)} </font> "

    text = HtmlCompat.fromHtml(homeNameStr + vsStr + awayNameStr, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun TextView.setPlayContent(
    playName: String?,
    spread: String?,
    formatForOdd: String?
) {
    val color_9BB3D9_535D76 = MultiLanguagesApplication.getChangeModeColorCode("#535D76", "#9BB3D9")
    val color_025BE8_025BE8 = MultiLanguagesApplication.getChangeModeColorCode("#025BE8", "#025BE8")

    val playNameStr = if (!playName.isNullOrEmpty()) "<font color=$color_9BB3D9_535D76>${playName} </font> " else ""
    val spreadStr = if (!spread.isNullOrEmpty() && playName != spread) "<font color=$color_9BB3D9_535D76>$spread</font> " else ""

    text = HtmlCompat.fromHtml(
        playNameStr +
                spreadStr +
                "<font color=$color_025BE8_025BE8>@ <b>$formatForOdd</b></font> ",
        HtmlCompat.FROM_HTML_MODE_LEGACY
    )
}

fun TextView.setPlayItem(playName: String?,
                         spread: String?){
//    val playNameStr = if (!playName.isNullOrEmpty()) "<font color=$color_9BB3D9_535D76>${playName} </font> " else ""
//    val spreadStr = if (!spread.isNullOrEmpty() && playName != spread) "<font color=$color_9BB3D9_535D76>$spread</font> " else ""
//
}

fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>,linkColor: Int? = null) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    links.forEachIndexed { index, link ->
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                //預設為一組Pair (如為多組Pair，第一組維持原文字色碼)
                if (links.size > 1 && index == 0) {
                    textPaint.color = textPaint.color
                } else {
                    textPaint.color = linkColor ?: textPaint.linkColor
                }
                textPaint.isUnderlineText = false
            }

            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.onClick(view)
            }
        }
        startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
        if (startIndexOfLink == -1) return@forEachIndexed // todo if you want to verify your texts contains links text
        spannableString.setSpan(
            clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
    this.highlightColor = Color.TRANSPARENT //設定點擊links的背景色
}
