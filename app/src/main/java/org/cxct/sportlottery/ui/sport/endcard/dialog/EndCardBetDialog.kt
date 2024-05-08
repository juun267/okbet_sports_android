package org.cxct.sportlottery.ui.sport.endcard.dialog

import android.graphics.Color
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.dialog_transfer_money.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.DialogEndcardBetBinding
import org.cxct.sportlottery.net.sport.data.EndCardBet
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.betList.holder.MAX_BET_VALUE
import org.cxct.sportlottery.ui.sport.endcard.EndCardBetManager
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.ui.sport.endcard.bet.EndCardGameFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
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
    private val oddAdapter by lazy { EndCardBetOddAdapter() }
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
        setOnClickListeners(ivClose){
            gameFragment.showFloatBet()
            dismiss()
        }
        btnAddMore.clickDelay {
            gameFragment.showFloatBet()
            dismiss()
        }
        btnBetting.clickDelay {
            addBet()
        }
    }
    private fun initOddList(){
        binding.rvOdd.apply {
            layoutManager = GridLayoutManager(context,5)
            if(itemDecorationCount==0){
                addItemDecoration(GridItemDecoration(6.dp,8.dp, Color.TRANSPARENT,false))
            }
            adapter = oddAdapter
            oddAdapter.setList(EndCardBetManager.getBetOdds())
            layoutParams.height = if (oddAdapter.itemCount>20) 160.dp else -2
        }
    }
    private fun setTips(){
        if (EndCardBetManager.getBetOdds().isEmpty()) return
        val betMoney =  "$showCurrencySign ${endCardBet.betMoney*EndCardBetManager.getBetOdds().size}"
        val oddNames =  "${EndCardBetManager.getBetOdds().joinToString("," )}"
        val totalLastDigit = endCardBet.lastDigit1+endCardBet.lastDigit2+endCardBet.lastDigit3+endCardBet.lastDigit4
        val totalWin = "$showCurrencySign $totalLastDigit"
        val notice = String.format(getString(R.string.P306),betMoney,oddNames,totalWin)
        binding.tvTips.text = Spanny(notice)
            .findAndSpan(betMoney) { ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.color_6AA4FF)) }
            .findAndSpan(oddNames) { ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.color_6AA4FF)) }
            .findAndSpan(totalWin) { ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.color_6AA4FF)) }
        val tips = "${getString(R.string.KYC055)} : "
        binding.tvPrompt.text = Spanny(tips+getString(R.string.P309))
            .findAndSpan(tips) { ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.color_FFD600)) }
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
    private fun addBet(){
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
                    (requireActivity() as BaseActivity<*,*>).jumpToDeposit()
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
}