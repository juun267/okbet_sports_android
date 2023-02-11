package org.cxct.sportlottery.ui.game.betList.receipt

import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_match_receipt.view.*
import kotlinx.android.synthetic.main.view_match_receipt_bet.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.extentions.gone
import org.cxct.sportlottery.extentions.visible
import org.cxct.sportlottery.network.bet.add.betReceipt.BetResult
import org.cxct.sportlottery.network.bet.add.betReceipt.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*

class SingleViewHolder private constructor(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    companion object {
        fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(viewGroup.context)
            val view = layoutInflater.inflate(R.layout.item_match_receipt, viewGroup, false)
            return SingleViewHolder(view)
        }
    }

    // 投注失败，当前盘口赔率返回的值通过odds字段返回的。其余赔率字段为：null
    fun getOdds(matchOdd: MatchOdd, oddsType: OddsType): Double {
        return when (oddsType) {
            OddsType.EU -> matchOdd?.odds ?: 0.0
            OddsType.HK -> matchOdd?.hkOdds ?: matchOdd?.odds ?: 0.0
            //Martin
            OddsType.MYS -> matchOdd?.malayOdds ?: matchOdd?.odds ?: 0.0
            OddsType.IDN -> matchOdd?.indoOdds ?: matchOdd?.odds ?: 0.0
        }
    }


    fun bind(betConfirmTime: Long? = 0,itemData: BetResult, oddsType: OddsType, interfaceStatusChangeListener: BetReceiptDiffAdapter.InterfaceStatusChangeListener?, position: Int) {
        itemView.apply {
            top_space.visibility = if (position == 0) View.VISIBLE else View.GONE

            val currencySign = sConfigData?.systemCurrencySign
            tv_winnable_amount_title.text =
                context.getString(R.string.bet_receipt_win_quota_with_sign) + "："
            tv_bet_amount_title.text =
                context.getString(R.string.bet_receipt_bet_quota_with_sign) + "："

            itemData.apply {
                matchOdds?.firstOrNull()?.apply {
                    val formatForOdd =
                        if (this.playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage(
                            getOdds(this, oddsType ?: OddsType.EU) - 1
                        ) else TextUtil.formatForOdd(
                            getOdds(this, oddsType)
                        )
                    tv_play_content.text = setSpannedString(
                        PlayCate.needShowSpread(playCateCode) && (matchType != MatchType.OUTRIGHT),
                        playName,
                        if (matchType != MatchType.OUTRIGHT) spread else "",
                        formatForOdd,
                        context.getString(getOddTypeRes(this, oddsType))
                    )
                    tv_odds.text = "@ $formatForOdd"

                    tv_league.text = leagueName
                    tv_team_names.setTeamNames(15, homeName, awayName)
                    tv_match_type.tranByPlayCode(playCode, playCateCode, playCateName, rtScore)
                }

                tv_bet_amount.text = "$currencySign${TextUtil.formatForOdd(itemData.stake ?: 0.0)}"
                tv_winnable_amount.text = "$currencySign${TextUtil.formatForOdd(winnable ?: 0.0)}"
//                tv_order_number.text = if (orderNo.isNullOrEmpty()) "-" else orderNo

                if (orderNo.isNullOrEmpty()) {
                    llcOrder.gone()
                } else {
                    llcOrder.visible()
                    tvBetOrder.text = if (orderNo.isEmpty()) "-" else ":${orderNo}"
                    tvBetTime.text = TimeUtil.timeFormat(betConfirmTime, "yyyy-MM-dd HH:mm:ss")
                }

                if (status != 0)
                    tv_bet_status_single.setBetReceiptStatus(status)

                //"status": 7 顯示賠率已改變
                if (status == 7)
                    interfaceStatusChangeListener?.onChange(code)

                tv_bet_status_single.setReceiptStatusColor(status)

                if (matchType == MatchType.OUTRIGHT) {
                    tv_team_names.visibility = View.GONE
                }
            }
        }
    }

    private fun setSpannedString(
        isShowSpread: Boolean,
        playName: String?,
        spread: String?,
        formatForOdd: String,
        oddsType: String
    ): Spanned {
        val color_FFFFFF_414655 =
            MultiLanguagesApplication.getChangeModeColorCode("#414655", "#FFFFFF")
//            val color_e5e5e5_333333 = MultiLanguagesApplication.getChangeModeColorCode("#333333", "#e5e5e5")
//            val color_F75452_b73a20 = MultiLanguagesApplication.getChangeModeColorCode("#B73A20", "#F75452")

        val playNameStr =
            if (!playName.isNullOrEmpty()) "<font color=$color_FFFFFF_414655>$playName</font> " else ""
        val spreadStr =
            if (!spread.isNullOrEmpty() || isShowSpread) "<font color=$color_FFFFFF_414655>$spread</font> " else ""

//            return HtmlCompat.fromHtml(
//                playNameStr +
//                        spreadStr +
//                        "<font color=$color_e5e5e5_333333>@ $formatForOdd</font> ", HtmlCompat.FROM_HTML_MODE_LEGACY
//            )
        return HtmlCompat.fromHtml(playNameStr + spreadStr, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}