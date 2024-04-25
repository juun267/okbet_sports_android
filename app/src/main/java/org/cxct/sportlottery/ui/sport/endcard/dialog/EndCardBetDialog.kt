package org.cxct.sportlottery.ui.sport.endcard.dialog

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.common.extentions.showPromptDialog
import org.cxct.sportlottery.databinding.DialogEndcardBetBinding
import org.cxct.sportlottery.net.sport.data.EndCardBet
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.betList.holder.MAX_BET_VALUE
import org.cxct.sportlottery.ui.sport.endcard.EndCardBetManager
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.ui.sport.endcard.bet.EndCardGameFragment
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.GridItemDecoration
import org.cxct.sportlottery.util.NetworkUtil
import org.cxct.sportlottery.util.Spanny
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.view.dialog.BetBalanceDialog
import org.cxct.sportlottery.view.dialog.ToGcashDialog
import timber.log.Timber

class EndCardBetDialog: BaseDialog<EndCardVM, DialogEndcardBetBinding>() {

    companion object{
        fun newInstance(endCardBet: EndCardBet)= EndCardBetDialog().apply {
            arguments = Bundle().apply {
                putParcelable("endCardBet",endCardBet)
            }
        }
    }
    init {
        marginHorizontal = 12.dp
    }
    private val oddAdapter by lazy { EndCardBetOddAdapter().apply {
        setOnItemClickListener { adapter, view, position ->
            val item = getItem(position)
            if (deleteOddsId==item){
                if(EndCardClearTipDialog.isNeedShow()){
                    EndCardClearTipDialog.newInstance(item).show(childFragmentManager)
                }else{
                    removeAt(position)
                    (requireParentFragment() as EndCardGameFragment).removeEndCardBet(item)
                }
            }else{
                setDeleteId(item)
            }
        }
    } }
    private val endCardBet by lazy { arguments?.getParcelable<EndCardBet>("endCardBet")!! }
    private val gameFragment by lazy { requireParentFragment() as EndCardGameFragment}

    override fun onInitView() {
        isCancelable=false
        initClick()
        initOddList()
        setTips()
        initObservable()
    }

    private fun initClick()=binding.run{
        btnBetting.text = "${root.context.getString(R.string.betting)} ${endCardBet.betMoney}"
        setOnClickListeners(linClose,linCollapse){
            gameFragment.showFloatBet()
            dismiss()
        }
        linDelete.setOnClickListener{
            clearAll()
        }
        btnAddMore.clickDelay {
            gameFragment.showFloatBet()
            dismiss()
        }
        btnBetting.clickDelay {
            addBet()
        }
    }
    private fun initOddList()=binding.run{
        rvOdd.apply {
            layoutManager = GridLayoutManager(context,5)
            if(itemDecorationCount==0){
                addItemDecoration(GridItemDecoration(6.dp,8.dp, Color.TRANSPARENT,false))
            }
            adapter = oddAdapter
            oddAdapter.setList(EndCardBetManager.getBetOdds())
            layoutParams.height = if (oddAdapter.itemCount>20) 160.dp else -2
        }
        if (EndCardDeleteGuideDialog.needShow()){
            rvOdd.post {
                val globalRect = Rect()
                val location = IntArray(2)
                rvOdd.getChildAt(0).getLocationOnScreen(location)
                globalRect.left = location[0]
                globalRect.top = location[1]
                EndCardDeleteGuideDialog.newInstance(globalRect).show(childFragmentManager)
            }
        }

        val onlyOne = EndCardBetManager.getBetOdds().size==1
        linClose.isVisible = onlyOne
        linCollapse.isVisible = !onlyOne
        linDelete.isVisible = !onlyOne
        tvCount.isVisible = !onlyOne
        tvCount.text = EndCardBetManager.getBetOdds().size.toString()
    }
    private fun setTips(){
        if (EndCardBetManager.getBetOdds().isEmpty()) return
        val betMoney =  "$showCurrencySign ${endCardBet.betMoney*EndCardBetManager.getBetOdds().size}"
        val oddNames =  "${EndCardBetManager.getBetOdds().joinToString("," )}"
        val totalLastDigit = endCardBet.lastDigit1+endCardBet.lastDigit2+endCardBet.lastDigit3+endCardBet.lastDigit4
        val totalWin = "$showCurrencySign $totalLastDigit"
        val notice = String.format(getString(R.string.P306),betMoney,oddNames,totalWin)
        binding.tvTips.text = Spanny(notice)
            .findAndSpan(betMoney) { ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.color_FFD600)) }
            .findAndSpan(oddNames) { ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.color_00E701)) }
            .findAndSpan(totalWin) { ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.color_FFD600)) }
        val tips = "${getString(R.string.KYC055)} : "
        binding.tvPrompt.text = Spanny(tips+getString(R.string.P309))
            .findAndSpan(tips) { ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.color_00E701)) }
    }
    private fun initObservable(){
        viewModel.addBetResult.observe(this){
            gameFragment.hideLoading()
            gameFragment.clearAllEndCardBet()
            gameFragment.reload()
            EndCardBetSuccessDialog.newInstance(it).show(parentFragmentManager)
            dismiss()
        }
        viewModel.addFaildResult.observe(this){
            gameFragment.hideLoading()
            EndCardBetFailDialog.newInstance(it).show(parentFragmentManager)
            dismiss()
        }
    }
    fun addBet(){
        if (!NetworkUtil.isAvailable(requireContext())) {
            requireActivity().showPromptDialog(
                getString(R.string.prompt), getString(R.string.message_network_no_connect)
            ) {}
            return
        }
        var maxBetMoney = MAX_BET_VALUE.toString()
        var minBetMoney = "0"
        val totalBetAmount = EndCardBetManager.getBetOdds().size* endCardBet.betMoney
        if (totalBetAmount < 0) {
            Timber.w("totalBetAmount isEmpty")
            return
        }
        Timber.d("maxBetMoney:$maxBetMoney minBetMoney:$minBetMoney totalBetAmount:$totalBetAmount")
        if (totalBetAmount > (viewModel.userMoney.value ?: 0.0)) {
            //换成弹框提示 去充值
            val dialog= BetBalanceDialog(requireContext())
            dialog.showDialog{
                //跳转充值
                ToGcashDialog.showByClick{
                    gameFragment.loading()
                    viewModel.checkRechargeKYCVerify()
                }
            }
        } else if (totalBetAmount > maxBetMoney.toDouble()) {
            ToastUtil.showToast(requireContext(), R.string.N989)
        } else if (totalBetAmount < minBetMoney.toDouble()) {
            ToastUtil.showToast(requireContext(), R.string.N990)
        } else {
            gameFragment.loading()
            viewModel.addBetLGPCOFL(endCardBet.matchId,EndCardBetManager.getBetOdds(),viewModel.userInfo.value?.nickName?:"",endCardBet.betMoney)
        }
    }
    fun updateOddList(){
        if (EndCardBetManager.getBetOdds().size==0){
            dismiss()
        }else{
            initOddList()
            setTips()
        }
    }
    fun clearAll(){
        gameFragment.clearAllEndCardBet()
        dismiss()
    }
    fun removeItem(oddsId: String){
        oddAdapter.removeItem(oddsId)
        (requireParentFragment() as EndCardGameFragment).removeEndCardBet(oddsId)
    }

}