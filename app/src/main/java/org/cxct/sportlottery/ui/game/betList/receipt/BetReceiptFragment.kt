package org.cxct.sportlottery.ui.game.betList.receipt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_bet_receipt.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MoneyEvent
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.BetsFailedReasonUtil
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.TextUtil
import org.greenrobot.eventbus.EventBus
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
        fun newInstance(betResultData: Receipt?, betParlayList: List<ParlayOdd>) =
            BetReceiptFragment().apply {
                this.betResultData = betResultData
                this.betParlayList = betParlayList
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
        viewModel.betFailed.observe(viewLifecycleOwner) {
            updateBetResultStatus(it)
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

                if (needUpdate) betReceiptDiffAdapter?.submit(
                    betResultData?.singleBets ?: listOf(),
                    betResultData?.parlayBets ?: listOf(),
                    this@BetReceiptFragment.betParlayList ?: listOf(),
                    betResultData?.betConfirmTime ?: 0
                )
            }
        }
        //投注結果
        viewModel.betAddResult.observe(this.viewLifecycleOwner) {
            hideLoading()
            it.getContentIfNotHandled().let { result ->
//                showReceipt = result != null
                result?.let { resultNotNull ->
                    if (resultNotNull.success) {
                        //检查是否有item注单下载失败
                        betResultData = resultNotNull.receipt
                        setupTotalValue()
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

//                       var oddsChange=betResultData?.singleBets?.any { it.status==7 } == true || betResultData?.parlayBets?.any { it.status==7 } == true
//                        updateBetResultStatus(oddsChange)
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
                //不管成功与否刷新当前金额
                viewModel.getMoney()
                EventBus.getDefault().post(MoneyEvent(true))
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
        var totalCount = 0
        betResultData?.singleBets?.forEach {
            if (it.status != BetStatus.CANCELED.value) betCount += (it.num ?: 0)
            totalCount += (it.num ?: 0)
        }

        betResultData?.parlayBets?.forEach {
            if (it.status != BetStatus.CANCELED.value) betCount += (it.num ?: 0)
            totalCount += (it.num ?: 0)
        }

        tv_all_bet_count.text = betCount.toString()
        (context ?: requireContext()).apply {
            tv_total_bet_amount.text = "${sConfigData?.systemCurrencySign} ${
                TextUtil.formatMoneyFourthDecimal(betResultData?.totalStake ?: 0.0)
            }"
            tv_total_winnable_amount.text = "${sConfigData?.systemCurrencySign} ${
                TextUtil.formatMoneyFourthDecimal(
                    betResultData?.totalWinnable ?: 0.0
                )
            }"
        }

        //顯示注單收據的數量
        tv_bet_list_count.text = totalCount.toString()
    }

    private fun initButton() {
        btn_complete.setOnClickListener {
            //清空购物车 ，下注其他盘口
            BetInfoRepository.clear()
            activity?.onBackPressed()
        }

        btnLastStep.setOnClickListener {
            if (viewModel.betFailed.value?.first == false) {
                //投注成功 ， 查看注单
                activity?.let {
                    it.supportFragmentManager.beginTransaction().remove(this@BetReceiptFragment)
                        .commitAllowingStateLoss()
                    if (it !is MainTabActivity) {
                        it.finish()
                    }
                }
                MainTabActivity.activityInstance?.jumpToBetInfo(1)
            } else {
                //投注失败，返回上一步
                activity?.onBackPressed()
                if (activity is MainTabActivity) {
                    (activity as MainTabActivity).showBetListPage()
                }
            }
        }
        cl_title.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun initRecyclerView() {
        rv_bet_receipt.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            LogUtil.toJson(betResultData?.singleBets)
            betReceiptDiffAdapter = BetReceiptDiffAdapter().apply {
                betResultData?.apply {
                    submit(
                        betResultData?.singleBets ?: listOf(),
                        betResultData?.parlayBets ?: listOf(),
                        this@BetReceiptFragment.betParlayList ?: listOf(),
                        betResultData?.betConfirmTime ?: 0
                    )
                }
                interfaceStatusChangeListener =
                    object : BetReceiptDiffAdapter.InterfaceStatusChangeListener {
                        override fun onChange(cancelBy: String?) {
                            val firstBetFailed = cancelBy?.isNotEmpty() ?: true
                            val pair = Pair(firstBetFailed, cancelBy)
                            Timber.d("betFirst:${firstBetFailed} betSecond:$cancelBy")
                            updateBetResultStatus(pair)
                        }
                    }
            }

            adapter = betReceiptDiffAdapter
        }
    }

    private fun setupReceiptStatusTips() {
        //全部都失敗才會顯示投注失敗
        val hasBetSuccess =
            betResultData?.singleBets?.find { it.status != BetStatus.CANCELED.value } != null
        val hasParlaySuccess =
            betResultData?.parlayBets?.find { it.status != BetStatus.CANCELED.value } != null
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


    fun updateBetResultStatus(betFailed: Pair<Boolean, String?>) {
        lin_result_status.isVisible = true
        //下注其他盘口
        btn_complete.text = getString(R.string.str_bet_other_game)
        btn_complete.setTextColor(
            ContextCompat.getColor(
                btn_complete.context, R.color.white
            )
        )
        Timber.d("投注成功或失败: ${betFailed.first}")
        if (betFailed.first) {
            //投注失败
            lin_result_status.setBackgroundResource(R.color.color_E23434)
            iv_result_status.setImageResource(R.drawable.ic_fail_white)
            tv_result_status.text = if (betFailed.second.isNullOrEmpty()) {
                getString(R.string.your_bet_order_fail)
            } else {
                BetsFailedReasonUtil.getFailedReasonByCode(betFailed.second)
            }
            btnLastStep.text = getString(R.string.str_return_last_step)
            btnLastStep.setTextColor(resources.getColor(R.color.color_025BE8, null))
            btnLastStep.background =
                ResourcesCompat.getDrawable(resources, R.drawable.bg_radius_8_bet_last_step, null)
        } else {
            //投注成功
            lin_result_status.setBackgroundResource(R.color.color_1EB65B)
            iv_result_status.setImageResource(R.drawable.ic_success_white)
            tv_result_status.text = getString(R.string.your_bet_order_success)
            btnLastStep.text = getString(R.string.str_check_bets)
            btnLastStep.setTextColor(resources.getColor(R.color.color_414655, null))
            btnLastStep.background =
                ResourcesCompat.getDrawable(resources, R.drawable.bg_radius_8_check_bet, null)

        }
    }

    fun showLoading() {
        btn_complete.isVisible = false
        lin_loading.isVisible = true
    }

    override fun hideLoading() {
        btn_complete.isVisible = true
        lin_loading.isVisible = false
    }
}