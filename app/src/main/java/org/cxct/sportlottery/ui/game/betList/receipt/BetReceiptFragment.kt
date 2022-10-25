package org.cxct.sportlottery.ui.game.betList.receipt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_bet_receipt.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.BetInfoRepository.oddsType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.observe
import timber.log.Timber


/**
 * @app_destination 注單收據
 */
class BetReceiptFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

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
        viewModel.oddChange.observe(viewLifecycleOwner) {
            if (it) {
                showOddChange("")
                updateOdd(betResultData)
            }
        }

        viewModel.settlementNotificationMsg.observe(viewLifecycleOwner) { event ->
            //TODO 此處若使用getContentIfNotHandled(), 於GameActivity時此處會一直取得null
            event.peekContent().let { sportBet ->
                var needUpdate = false

                //單注單
                betResultData?.singleBets?.find { betResult ->
                    betResult.orderNo == sportBet.orderNo
                }?.let { targetBetResult ->
                    if (targetBetResult.status != sportBet.status) {
                        needUpdate = true
                        targetBetResult.status = sportBet.status
                    }
                }

                //串關單
                betResultData?.parlayBets?.find { parlayBetResult ->
                    parlayBetResult.orderNo == sportBet.orderNo
                }?.let { targetParlayBetResult ->
                    if (targetParlayBetResult.status != sportBet.status) {
                        needUpdate = true
                        targetParlayBetResult.status = sportBet.status
                    }
                }

                if (needUpdate)
                    betReceiptDiffAdapter?.submit(
                        betResultData?.singleBets ?: listOf(),
                        betResultData?.parlayBets ?: listOf(),
                        this@BetReceiptFragment.betParlayList ?: listOf(),
                        betResultData?.betConfirmTime ?: 0
                    )
            }
        }
        //投注結果
        viewModel.betAddResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled().let { result ->
//                showReceipt = result != null
                result?.let { resultNotNull ->
                    if (resultNotNull.success) {
                        //检查是否有item注单下载失败
                        betResultData = resultNotNull.receipt
                        betReceiptDiffAdapter?.apply {
                            betResultData?.apply {
                                submit(
                                    betResultData?.singleBets ?: listOf(),
                                    betResultData?.parlayBets ?: listOf(),
                                    this@BetReceiptFragment.betParlayList ?: listOf(),
                                    betResultData?.betConfirmTime ?: 0
                                )
                            }
                        }

//                        setBetLoadingVisibility(false, keepShowingBetLoading = true)
                        //多筆和單筆投注單，下注成功後的行為不同
//                        if (isMultiBet) {
                        //多筆的是直接 replace fragment
//                        viewModel.betInfoList.removeObservers(this.viewLifecycleOwner)
////                        } else {
////                            //單筆的要關掉再顯示 dialog
////                        }
//                        betResultListener?.onBetResult(
//                            resultNotNull.receipt,
//                            betParlayList ?: listOf(),
//                            true
//                        )
//                        refreshAllAmount()
//                        showOddChangeWarn = false
//                        btn_bet.isOddsChanged = false
//                        showHideWarn()
//                        if (result.receipt?.singleBets?.any { singleBet -> singleBet.status == 0 } == true || result.receipt?.parlayBets?.any { parlayBet -> parlayBet.status == 0 } == true) {
//                            mHandler.removeMessages(BetListFragment.BET_CONFIRM_TIPS)
//                            mHandler.sendMessage(Message().apply {
//                                what = BetListFragment.BET_CONFIRM_TIPS
//                            })
//                        }
                    } else {
//                        setBetLoadingVisibility(false)
                        showErrorPromptDialog(getString(R.string.prompt), resultNotNull.msg) {}
                    }
                }
            }
        }

    }

    private fun initView() {
        tv_currency.text = sConfigData?.systemCurrencySign
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
            tv_total_bet_amount.text = "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoneyFourthDecimal(betResultData?.totalStake?: 0.0)}"
            tv_total_winnable_amount.text =
                "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoneyFourthDecimal(betResultData?.totalWinnable ?: 0.0)}"
        }

        //顯示注單收據的數量
        tv_bet_list_count.text = betCount.toString()
    }

    private fun initButton() {
        btn_complete.setOnClickListener {
            if (btn_cancel.isVisible) {
                addBet()
            } else {
                activity?.onBackPressed()
            }
        }
        btn_cancel.setOnClickListener {
            activity?.onBackPressed()
        }
        cl_title.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun initRecyclerView() {
        rv_bet_receipt.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
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
                    BetReceiptDiffAdapter.InterfaceStatusChangeListener {
                    override fun onChange(cancelBy: String) {
                        showOddChange(cancelBy)
                    }
                }
            }

            adapter = betReceiptDiffAdapter
        }
    }

    private fun setupReceiptStatusTips() {
        //全部都失敗才會顯示投注失敗
        val hasBetSuccess = betResultData?.singleBets?.find { it.status != BetStatus.CANCELED.value } != null
        val hasParlaySuccess = betResultData?.parlayBets?.find { it.status != BetStatus.CANCELED.value } != null
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
                    getString(R.string.btn_sure)
                }
                false -> {
                    getString(R.string.bet_fail_btn)
                }
            }
        }
//        btn_complete.setTextColor(ContextCompat.getColor(btn_complete.context,R.color.white))
    }

    /**
     * 取消触发来源 0: 自动, 1: 手动,其他：赔率变化
     */
    private fun showOddChange(cancelBy: String) {
        btn_cancel.isVisible = true
        btn_complete.text = getString(R.string.accept_current_odd_to_bet)
        btn_complete.setTextColor(
            ContextCompat.getColor(
                btn_complete.context,
                R.color.white
            )
        )
    }

    private fun addBet() {
        var tabPosition = if (betResultData?.parlayBets.isNullOrEmpty()) 0 else 1
        var betList = viewModel.betInfoList.value?.peekContent() ?: mutableListOf()
        val betListFilter =
            betList.filter { it.matchOdd.status == org.cxct.sportlottery.enum.BetStatus.ACTIVATED.code }

        val parlayList =
            if (betList.size == betListFilter.size) betParlayList
                ?: mutableListOf() else mutableListOf()

        val tempParlayList = mutableListOf<ParlayOdd>()
        parlayList.forEach {
            tempParlayList.add(it.copy())
        }
        betParlayList = tempParlayList

        //只取得對應tab內的totalBetAmount
        val totalBetAmount = if (tabPosition == 0) {
            betListFilter.sumOf { it.realAmount }
        } else {
            parlayList.sumOf { it.betAmount * it.num }
        }
//        val totalBetAmount =
//            betListFilter.sumByDouble { it.realAmount } + (parlayList.sumByDouble { it.betAmount * it.num })

        if (totalBetAmount.toString().isEmpty()) {
            Timber.w("totalBetAmount isEmpty")
            return
        }

        //下注總金額大於用戶餘額，提示餘額不足
        if (totalBetAmount > (viewModel.userMoney.value ?: 0.0)) {
            showErrorPromptDialog(
                getString(R.string.prompt),
                getString(R.string.bet_info_bet_balance_insufficient)
            ) {}
            return
        }

        viewModel.addBetList(
            betList,
            parlayList,
            oddsType,
            tabPosition
        )
    }

    fun updateOdd(betResultData: Receipt?) {
        betResultData?.singleBets?.forEach {
            it.matchOdds?.firstOrNull()?.let { matchOdd ->
                BetInfoRepository?.betInfoList.value?.peekContent()?.forEach { betInfoListData ->
                    if (matchOdd.oddsId == betInfoListData.matchOdd.oddsId) {
                        matchOdd.odds?.let { betInfoListData.matchOdd.odds = it }
                        matchOdd.hkOdds?.let { betInfoListData.matchOdd.hkOdds = it }
                        matchOdd.indoOdds?.let { betInfoListData.matchOdd.indoOdds = it }
                        matchOdd.malayOdds?.let { betInfoListData.matchOdd.malayOdds = it }
                    }
                }
            }
        }
        betResultData?.parlayBets?.forEach {
            it.matchOdds?.forEach { matchOdd ->
                BetInfoRepository?.betInfoList.value?.peekContent()?.forEach { betInfoListData ->
                    if (matchOdd.oddsId == betInfoListData.matchOdd.oddsId) {
                        matchOdd.odds?.let { betInfoListData.matchOdd.odds = it }
                        matchOdd.hkOdds?.let { betInfoListData.matchOdd.hkOdds = it }
                        matchOdd.indoOdds?.let { betInfoListData.matchOdd.indoOdds = it }
                        matchOdd.malayOdds?.let { betInfoListData.matchOdd.malayOdds = it }
                    }
                }
            }
        }
        BetInfoRepository.notifyBetInfoChanged()
    }
}