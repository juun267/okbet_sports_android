package org.cxct.sportlottery.ui.betList.receipt

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_bet_receipt.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.toStringS
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentBetReceiptBinding
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.betList.BetListViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.BetPlayCateFunction.isEndScoreType
import org.cxct.sportlottery.util.BetsFailedReasonUtil
import org.cxct.sportlottery.util.TextUtil
import timber.log.Timber


/**
 * @app_destination 注單收據
 */
class BetReceiptFragment :
    BaseSocketFragment<BetListViewModel,FragmentBetReceiptBinding>() {

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

    override fun onInitView(view: View) {
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
            val sportBet = event.peekContent()
            var needUpdate = false

            val singleBets = betResultData?.singleBets ?: listOf()
            val parlayBets = betResultData?.parlayBets ?: listOf()

            //單注單
            singleBets.find { betResult ->
                betResult.orderNo == sportBet.orderNo
            }?.let { targetBetResult ->
                if (targetBetResult.status != sportBet.status) {
                    needUpdate = true
                    targetBetResult.status = sportBet.status
                }
            }

            //串關單
            parlayBets.find { parlayBetResult ->
                parlayBetResult.orderNo == sportBet.orderNo
            }?.let { targetParlayBetResult ->
                if (targetParlayBetResult.status != sportBet.status) {
                    needUpdate = true
                    targetParlayBetResult.status = sportBet.status
                }
            }

            if (needUpdate) {
                betReceiptDiffAdapter?.submit(
                singleBets,
                parlayBets,
                this@BetReceiptFragment.betParlayList ?: listOf(),
                betResultData?.betConfirmTime ?: 0
                )
                setupReceiptStatusTips()
                lin_result_status_processing?.gone()
                updateBetResultStatus(Pair(sportBet.status==7,sportBet.cancelReason))
            }
        }

        //投注結果
        viewModel.betAddResult.observe(this.viewLifecycleOwner) {
            hideLoading()
            //不管成功与否刷新当前金额
            viewModel.getMoneyAndTransferOut()
            val result = it.getContentIfNotHandled() ?: return@observe

            if (!result.success) {
//              setBetLoadingVisibility(false)
                showErrorPromptDialog(getString(R.string.prompt), result.msg) { }
                return@observe
            }

            //检查是否有item注单下载失败
            betResultData = result.receipt
            setupTotalValue()
            betResultData?.let {
                betReceiptDiffAdapter?.submit(
                    it.singleBets ?: listOf(),
                    it.parlayBets ?: listOf(),
                    this@BetReceiptFragment.betParlayList ?: listOf(),
                    it.betConfirmTime ?: 0
                )
            }
        }
    }

    private fun initView() {
        tv_currency.text = "(${showCurrencySign})"
        setupTotalValue()

        initButton()
        initRecyclerView()
        setupReceiptStatusTips()
    }

    @SuppressLint("SetTextI18n")
    private fun setupTotalValue() {
        var betCount = 0
        var totalCount = 0
        betResultData?.singleBets?.forEach {
            if (!it.isFailed()) {
                betCount += (it.num ?: 0)
            }
            totalCount += (it.num ?: 0)
        }

        betResultData?.parlayBets?.forEach {
            if (!it.isFailed()) {
                betCount += (it.num ?: 0)
            }
            totalCount += (it.num ?: 0)
        }

        tv_all_bet_count.text = betCount.toString()
        tv_total_bet_amount.text = "${sConfigData?.systemCurrencySign} ${
            TextUtil.formatMoneyFourthDecimal(betResultData?.totalStake ?: 0.0)
        }"
        tv_total_winnable_amount.text = "${sConfigData?.systemCurrencySign} ${
            TextUtil.formatMoneyFourthDecimal(
                betResultData?.totalWinnable ?: 0.0
            )
        }"

        //顯示注單收據的數量
        if (BetInfoRepository.currentBetType == 0) {
            val firstMatchOdds = betResultData?.singleBets?.firstOrNull()?.matchOdds?.firstOrNull()
            val bkEndScore= firstMatchOdds?.playCateCode.isEndScoreType()
            if (bkEndScore){
                viewBall.visible()
                tv_bet_list_count.visible()
                tv_bet_list_count.text = firstMatchOdds?.multiCode?.size?.toStringS("0")
            } else {
                viewBall.gone()
                tv_bet_list_count.gone()
            }
        } else {
            viewBall.visible()
            tv_bet_list_count.visible()
            tv_bet_list_count.text = totalCount.toString()
        }
    }

    private fun initButton() {

        btn_complete.setOnClickListener {
            //清空购物车 ，下注其他盘口
            BetInfoRepository.clear()
            activity?.onBackPressed()
        }

        btnLastStep.setOnClickListener {
            if (btnLastStep.text == getText(R.string.commission_detail)) {
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
        val layoutMana = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv_bet_receipt.layoutManager = layoutMana
        layoutMana.stackFromEnd = true

        betReceiptDiffAdapter = BetReceiptDiffAdapter().apply {
            betResultData?.let {
                submit(
                    it.singleBets ?: listOf(),
                    it.parlayBets ?: listOf(),
                    this@BetReceiptFragment.betParlayList ?: listOf(),
                    it.betConfirmTime ?: 0
                )
            }

            interfaceStatusChangeListener = object : BetReceiptDiffAdapter.InterfaceStatusChangeListener {
                override fun onChange(cancelBy: String?) {
                    if (cancelBy.isNullOrEmpty())
                        return
                    updateBetResultStatus(Pair(true, cancelBy))
                }
            }

            refreshBetStatusFunction = { time ->
                lin_result_status_processing?.visible()
                lin_result_status?.gone()
                tvBetProcessingStatus?.text = getString(R.string.str_in_play_bet_confirmed) + "（${time}S）"
            }

            refreshBetStatusFinishFunction = {
                tvBetProcessingStatus?.text = getString(R.string.str_in_play_bet_confirmed)+ " "
                //倒计时结束后，不处理
//                lin_result_status_processing?.gone()
//                lin_result_status?.visible()
            }
        }

        rv_bet_receipt.adapter = betReceiptDiffAdapter
    }

    private fun setupReceiptStatusTips() {
        //全部都失敗才會顯示投注失敗
        val hasBetSuccess = betResultData?.singleBets?.find { !it.isFailed() } != null
        val hasParlaySuccess = betResultData?.parlayBets?.find { !it.isFailed() } != null
        btn_complete.text = if (hasBetSuccess || hasParlaySuccess) {
            getString(R.string.btn_sure)
        } else{
            getString(R.string.bet_fail_btn)
        }
    }


    fun updateBetResultStatus(betFailed: Pair<Boolean, String?>) {
        lin_result_status.isVisible = true
        //下注其他盘口
        btn_complete.text = getString(R.string.str_bet_other_game)
        btn_complete.setTextColor(Color.WHITE)
//        Timber.d("投注成功或失败: ${betFailed.first}")

        Timber.d("滑动位置:${betReceiptDiffAdapter?.items?.size?.minus(1) ?: 0}")
        binding.rvBetReceipt.postDelayed({
            binding.rvBetReceipt.scrollToPosition(betReceiptDiffAdapter?.items?.size?.minus(1) ?: 0)
        }, 100)

        if (betFailed.first) {
            //投注失败
//            lin_result_status.setBackgroundResource(R.color.color_E23434)
            lin_result_status.background =
                AppCompatResources.getDrawable(requireContext(), R.drawable.drawable_bet_failure)
            iv_result_status.setImageResource(R.drawable.ic_bet_failure)
            tv_result_status.setTextColor(requireContext().getColor(R.color.color_ff0000))
            if (betFailed.second.isNullOrEmpty()){
                tv_result_status.text = getString(R.string.N417)
                btnLastStep.text = getString(R.string.commission_detail)
                btnLastStep.setTextColor(resources.getColor(R.color.color_14366B, null))
                btnLastStep.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.bg_radius_8_check_bet, null)
            }else{
                tv_result_status.text = BetsFailedReasonUtil.getFailedReasonByCode(betFailed.second)
                btnLastStep.text = getString(R.string.str_return_last_step)
                btnLastStep.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(),R.drawable.ic_bet_recept_back),null,null,null)
                btnLastStep.setTextColor(resources.getColor(R.color.color_025BE8, null))
                btnLastStep.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.bg_radius_8_bet_last_step, null)
            }
        } else {
            //投注成功
            lin_result_status.background =
                AppCompatResources.getDrawable(requireContext(), R.drawable.drawable_bet_successful)

            iv_result_status.setImageResource(R.drawable.ic_bet_successful)
            tv_result_status.setTextColor(requireContext().getColor(R.color.color_1CD219))

            tv_result_status.text = getString(R.string.your_bet_order_success)
            btnLastStep.text = getString(R.string.commission_detail)

            btnLastStep.setTextColor(resources.getColor(R.color.color_14366B, null))
            btnLastStep.background =
                ResourcesCompat.getDrawable(resources, R.drawable.bg_radius_8_check_bet, null)

        }
    }

    override fun showLoading() {
        btn_complete.isVisible = false
        lin_loading.isVisible = true
    }

    override fun hideLoading() {
        btn_complete.isVisible = true
        lin_loading.isVisible = false
    }
}