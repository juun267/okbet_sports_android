package org.cxct.sportlottery.ui.game.betList.receipt

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_bet_receipt.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.TextUtil


/**
 * A simple [Fragment] subclass.
 * Use the [BetReceiptFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BetReceiptFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val betStatusCancelledCode = 7

    private var betResultData: Receipt? = null

    private var betParlayList: List<ParlayOdd>? = null

    private var betReceiptDiffAdapter: BetReceiptDiffAdapter? = null

    companion object {
        @JvmStatic
        fun newInstance(betResultData: Receipt?, betParlayList: List<ParlayOdd>) = BetReceiptFragment().apply {
            this.betResultData = betResultData
            this.betParlayList = betParlayList
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bet_receipt, container, false).apply {
            initData()
        }
    }

    private fun initData() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()
        initView()
    }

    private fun initObserver() {
        viewModel.userMoney.observe(viewLifecycleOwner) {
            it.let { money -> tv_balance.text = TextUtil.formatMoney(money ?: 0.0) }
        }

        viewModel.oddsType.observe(viewLifecycleOwner) {
            betReceiptDiffAdapter?.oddsType = it
        }
    }

    private fun initView() {
        tv_currency.text = sConfigData?.systemCurrency
        setupTotalValue()

        initButton()
        initRecyclerView()
        setupReceiptStatusTips()
    }

    enum class BetStatus(val value: Int) {
        CANCELED(7)
    }

    private fun setupTotalValue() {
        var betCount = 0
        betResultData?.singleBets?.forEach {
            if (it.status != BetStatus.CANCELED.value) betCount += (it.num ?: 0)
        }

        betResultData?.parlayBets?.forEach {
            if (it.status != BetStatus.CANCELED.value) betCount += (it.num ?: 0)
        }

        tv_all_bet_count.text = betCount.toString()
        (context ?: requireContext()).apply {
            tv_total_bet_amount.text = "${TextUtil.formatMoney(betResultData?.totalStake?: 0.0)} ${sConfigData?.systemCurrency}"
            tv_total_winnable_amount.text =
                "${TextUtil.formatMoney(betResultData?.totalWinnable ?: 0.0)} ${sConfigData?.systemCurrency}"
        }
    }

    private fun initButton() {
        btn_complete.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun initRecyclerView() {
        rv_bet_receipt.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
//                ContextCompat.getDrawable(
//                    context ?: requireContext(),
//                    R.drawable.divider_color_white8
//                )?.let {
//                    setDrawable(it)
//                }
//            })

            betReceiptDiffAdapter = BetReceiptDiffAdapter().apply {
                betResultData?.apply {
                    submit(
                        betResultData?.singleBets ?: listOf(),
                        betResultData?.parlayBets ?: listOf(),
                        this@BetReceiptFragment.betParlayList ?: listOf(),
                        betResultData?.betConfirmTime ?: 0
                    )
                }
                interfaceStatusChangeListener = object :
                    BetReceiptDiffAdapter.InterfaceStatusChangeListener{
                    override fun onChange() {
                        setReceiptStatus7()
                    }
                }
            }

            adapter = betReceiptDiffAdapter
        }
    }

    private fun setupReceiptStatusTips() {
        //全部都失敗才會顯示投注失敗
        val hasBetSuccess = betResultData?.singleBets?.find { it.status != betStatusCancelledCode } != null
        val hasParlaySuccess = betResultData?.parlayBets?.find { it.status != betStatusCancelledCode } != null
//        tv_already_bet_complete.apply {
//            when (hasBetSuccess || hasParlaySuccess) {
//                true -> {
//                    text = getString(R.string.bet_succeeded)
//                    setTextColor(ContextCompat.getColor(context, R.color.colorBlue))
//                }
//                false -> {
//                    text = getString(R.string.bet_fail)
//                    setTextColor(ContextCompat.getColor(context, R.color.colorRed))
//                }
//            }
//        }

        btn_complete.apply {
            text = when (hasBetSuccess || hasParlaySuccess) {
                true -> {
                    getString(R.string.complete)
                }
                false -> {
                    getString(R.string.bet_fail_btn)
                }
            }
        }
        btn_complete.setTextColor(ContextCompat.getColor(btn_complete.context,R.color.white))
    }

    private fun setReceiptStatus7() {
        btn_complete.text = getString(R.string.bet_fail_btn)
        btn_complete.setTextColor(
            ContextCompat.getColor(
                btn_complete.context,
                R.color.white
            )
        )
    }
}