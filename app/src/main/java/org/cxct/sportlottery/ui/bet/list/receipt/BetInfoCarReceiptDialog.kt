package org.cxct.sportlottery.ui.bet.list.receipt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item_receipt.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item_receipt.view.*
import kotlinx.android.synthetic.main.item_match_receipt.view.*
import kotlinx.android.synthetic.main.view_match_receipt_bet.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.betReceipt.BetAddResult
import org.cxct.sportlottery.ui.base.BaseSocketBottomSheetFragment
import org.cxct.sportlottery.ui.game.GameViewModel
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
                    tv_play_name.text = playName
                    tv_match_odd.setOddFormat(odds)
                    tv_league.text = leagueName
                    tv_team_home.text = homeName
                    tv_spread.text = spread
                    tv_team_away.text = awayName
                    tv_match_type.text = playCateName
                }

                view.view_match_receipt.setBetReceiptBackground(status)
                tv_bet_amount.setBetReceiptAmount(this)
                tv_order_number.text = if (orderNo.isNullOrEmpty()) "-" else orderNo
                tv_winnable_amount.setMoneyFormat(winnable)
                tv_bet_status.setBetReceiptStatus(status)
                tv_bet_status.setReceiptStatusColor(status)
                tv_receipt_status.setSingleReceiptStatusTips(status)
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

        viewModel.userMoney.observe(this.viewLifecycleOwner, {
            it?.let { money -> setupCurrentMoney(money) }
        })
    }


    private fun setupCurrentMoney(money: Double) {
        tv_current_money.text =
            getString(R.string.bet_info_current_rmb, TextUtil.formatMoney(money))
    }

}
