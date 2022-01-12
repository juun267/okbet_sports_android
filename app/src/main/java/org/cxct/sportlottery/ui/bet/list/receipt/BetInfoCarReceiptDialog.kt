package org.cxct.sportlottery.ui.bet.list.receipt

import android.os.Bundle
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item_receipt.*
import kotlinx.android.synthetic.main.item_match_receipt.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item_receipt.view.*
import kotlinx.android.synthetic.main.item_match_receipt.view.*
import kotlinx.android.synthetic.main.view_match_receipt_bet.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.add.betReceipt.BetAddResult
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate.Companion.needShowSpread
import org.cxct.sportlottery.ui.base.BaseSocketBottomSheetFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*

class BetInfoCarReceiptDialog(val result: BetAddResult) :
    BaseSocketBottomSheetFragment<GameViewModel>(GameViewModel::class) {

    init {
        setStyle(STYLE_NORMAL, R.style.LightBackgroundBottomSheet)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LayoutInflater.from(context)
            .inflate(R.layout.dialog_bottom_sheet_betinfo_item_receipt, container, false).apply {
                initView(this)
            }
    }

    private fun initView(view: View) {
        view.apply {
            result.receipt?.singleBets?.firstOrNull()?.apply {
                matchOdds?.firstOrNull()?.apply {
                    tv_league.text = leagueName
                    tv_team_home.text = homeName
                    tv_team_away.text = awayName
                    tv_match_type.text = playCateName
                }

                view.view_match_receipt.setBetReceiptBackground(status)
                tv_bet_amount.text = TextUtil.formatMoney(stake ?: 0.0)
                tv_order_number.text = if (orderNo.isNullOrEmpty()) "-" else orderNo
                tv_winnable_amount.text = TextUtil.formatMoney(winnable ?: 0.0)
                tv_bet_status.setBetReceiptStatus(status)
                tv_bet_status.setReceiptStatusColor(status)
                tv_receipt_status.setSingleReceiptStatusTips(status)

                if (matchType == MatchType.OUTRIGHT) {
                    tv_team_home.visibility = View.GONE
                    tv_verse.visibility = View.GONE
                    tv_team_away.visibility = View.GONE
                }

                val matchOdd = matchOdds?.firstOrNull()

                var currentOddsTypes = oddsType
                if (matchType == MatchType.PARLAY) {
                    currentOddsTypes = OddsType.EU
                }
                matchOdd?.apply {
                    tv_play_content.text = setSpannedString(
                        needShowSpread(matchOdd.playCateCode) && (matchType != MatchType.OUTRIGHT),
                        matchOdd.playName,
                        matchOdd.spread,
                        TextUtil.formatForOdd(getOdds(matchOdd, currentOddsTypes ?: OddsType.EU)),
                        getString(currentOddsTypes?.res ?: OddsType.EU.res)
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getCurrentMoney()
        initOnclick()
        initObserver()
    }

    private fun initOnclick() {
        btn_done.setOnClickListener {
            this@BetInfoCarReceiptDialog.dismiss()
        }
    }

    private fun getCurrentMoney() {
        viewModel.getMoney()
    }

    private fun initObserver() {

        viewModel.userMoney.observe(this.viewLifecycleOwner) {
            it?.let { money -> setupCurrentMoney(money) }
        }

        viewModel.oddsType.observe(viewLifecycleOwner) { oddType ->

            result.receipt?.singleBets?.firstOrNull()?.let { betResult ->
                betResult.matchOdds?.firstOrNull()?.let { matchOdd ->
                    var currentOddsTypes = oddType
                    if (betResult.matchType == MatchType.PARLAY) {
                        currentOddsTypes = OddsType.EU
                    }
                    tv_play_content.text = setSpannedString(
                        needShowSpread(matchOdd.playCateCode) && (betResult.matchType != MatchType.OUTRIGHT),
                        matchOdd.playName,
                        matchOdd.spread,
                        TextUtil.formatForOdd(getOdds(matchOdd, currentOddsTypes)),
                        getString(currentOddsTypes.res)
                    )

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
        val playNameStr =
            if (!playName.isNullOrEmpty()) "<font color=#333333>$playName</font> " else ""
        val spreadStr =
            if (!spread.isNullOrEmpty() || isShowSpread) "<font color=#B73A20>$spread</font> " else ""

        return HtmlCompat.fromHtml(
            playNameStr +
                    spreadStr +
                    "<font color=#333333>@ $formatForOdd</font> " +
                    "<font color=#666666>(${oddsType})</font>", HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    private fun setupCurrentMoney(money: Double) {
        tv_current_money.text =
            getString(R.string.bet_info_current_rmb, TextUtil.formatMoney(money))
    }

}
