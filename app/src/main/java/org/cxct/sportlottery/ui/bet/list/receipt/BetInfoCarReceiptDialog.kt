package org.cxct.sportlottery.ui.bet.list.receipt

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item_receipt.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item_receipt.view.*
import kotlinx.android.synthetic.main.view_match_receipt_bet.*
import kotlinx.android.synthetic.main.view_match_receipt_bet.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.betReceipt.BetAddResult
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate.Companion.needShowSpread
import org.cxct.sportlottery.network.service.order_settlement.Status
import org.cxct.sportlottery.ui.base.BaseSocketBottomSheetFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*
import java.util.*

class BetInfoCarReceiptDialog(val result: BetAddResult) :
    BaseSocketBottomSheetFragment<GameViewModel>(GameViewModel::class) {

    val mHandler = Handler(Looper.getMainLooper())
    var timer = Timer()
    private var mOrderNo: String? = null

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
                    view.tvLeague.text = leagueName
                    view.tv_team_names.setTeamNames(15, homeName, awayName)
                    view.tv_match_type.tranByPlayCode(playCode, playCateCode, playCateName, rtScore)
                }

                //view.view_match_receipt.setBetReceiptBackground(status)
                view.tv_bet_amount.text = TextUtil.formatMoney(stake ?: 0.0)
                view.tv_order_number.text = if (orderNo.isNullOrEmpty()) "-" else orderNo
                mOrderNo = orderNo
                view.tv_winnable_amount.text = TextUtil.formatMoney(winnable ?: 0.0)
                view.tv_bet_status.setBetReceiptStatus(status)
                if(status == 0){
                    startTimer(
                        (result.receipt.betConfirmTime?.minus(System.currentTimeMillis())) ?: 0,
                        view
                    )
                }
                view.tv_bet_status.setReceiptStatusColor(status)
                //view.tv_receipt_status.setSingleReceiptStatusTips(status)

                if (matchType == MatchType.OUTRIGHT) {
                    tv_team_names.visibility = View.GONE
                }

                val matchOdd = matchOdds?.firstOrNull()

                var currentOddsTypes = oddsType
                if (matchOdd?.odds == matchOdd?.malayOdds || matchType == MatchType.OUTRIGHT || matchType == MatchType.OTHER_OUTRIGHT) {
                    currentOddsTypes = OddsType.EU
                }

                tv_play_content.text = setPlayContent(
                    needShowSpread(matchOdd?.playCateCode) && (matchType != MatchType.OUTRIGHT),
                    matchOdd?.playName,
                    if (matchType != MatchType.OUTRIGHT) matchOdd?.spread else "",
                    TextUtil.formatForOdd(getOdds(matchOdd, oddsType ?: OddsType.EU)),
                    tv_play_content.context.getString(matchOdd?.let { matchOdd ->
                        currentOddsTypes?.let { currentOddsTypes ->
                            getOddTypeRes(
                                matchOdd,
                                currentOddsTypes
                            )
                        }
                    }
                        ?: OddsType.EU.res)
                )
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
        btn_done.text = resources.getString(R.string.complete)
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
                    if (matchOdd.odds == matchOdd.malayOdds || betResult.matchType == MatchType.OUTRIGHT || betResult.matchType == MatchType.OTHER_OUTRIGHT) {
                        currentOddsTypes = OddsType.EU
                    }
                    tv_play_content.text = setPlayContent(
                        needShowSpread(matchOdd.playCateCode) && (betResult.matchType != MatchType.OUTRIGHT),
                        matchOdd.playName,
                        if (betResult.matchType != MatchType.OUTRIGHT) matchOdd.spread else "",
                        TextUtil.formatForOdd(getOdds(matchOdd, currentOddsTypes)),
                        getString(currentOddsTypes.res)
                    )

                }
            }
        }

        viewModel.settlementNotificationMsg.observe(this.viewLifecycleOwner) { event ->
            //TODO 使用getContentIfNotHandled()時, 在GameActivity會一直是null
            event.peekContent().let { message ->
                if (message.orderNo == mOrderNo) {
                    message.status?.let { orderStatus ->
                        if (orderStatus != Status.UN_CHECK.code) {
                            stopTimer()
                        }
                        with(tv_bet_status) {
                            setBetReceiptStatus(orderStatus)
                            setReceiptStatusColor(orderStatus)
                        }
                    }
                }
            }
        }
    }

    private fun setPlayContent(
        isShowSpread: Boolean,
        playName: String?,
        spread: String?,
        formatForOdd: String,
        oddsType: String
    ): Spanned {
        val color_e5e5e5_333333 = MultiLanguagesApplication.getChangeModeColorCode("#333333", "#e5e5e5")
        val color_F75452_b73a20 = MultiLanguagesApplication.getChangeModeColorCode("#B73A20", "#F75452")

        val playNameStr =
            if (!playName.isNullOrEmpty()) "<font color=$color_e5e5e5_333333>$playName</font> " else ""
        val spreadStr =
            if (!spread.isNullOrEmpty() || isShowSpread) "<font color=$color_F75452_b73a20>$spread</font> " else ""

        return HtmlCompat.fromHtml(
            playNameStr +
                    spreadStr +
                    "<font color=$color_e5e5e5_333333>@ $formatForOdd</font> " , HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    private fun setupCurrentMoney(money: Double) {
        //tv_current_money.text = "${TextUtil.formatMoney(money)} ${sConfigData?.systemCurrency}"
    }

    private fun startTimer(startTime: Long, view: View) {
        var timeMillis = startTime.div(1000)
        timer.schedule(object :TimerTask(){
            override fun run() {
                mHandler.post {
                    view.tv_bet_status.text = String.format(view.context.getString(R.string.pending), timeMillis)
                    timeMillis --
                    if(timeMillis < 0 ) stopTimer()
                }
            }
        }, 1000L, 1000L)
    }

    private fun stopTimer() {
       timer.cancel()
    }

}
