package org.cxct.sportlottery.ui.sport.endcard.dialog

import android.graphics.Color
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.DialogEndcardWinTipBinding
import org.cxct.sportlottery.net.sport.data.EndCardBet
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.sport.endcard.EndCardBetManager
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.ui.sport.endcard.bet.EndCardGameFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp

class EndCardWinTipDialog: BaseDialog<EndCardVM, DialogEndcardWinTipBinding>() {

    companion object{
        fun newInstance(endCardBet: EndCardBet)= EndCardWinTipDialog().apply {
            arguments = Bundle().apply {
                putParcelable("endCardBet",endCardBet)
            }
        }
    }
    init {
        marginHorizontal = 12.dp
    }
    private val endCardBet by lazy { arguments?.getParcelable<EndCardBet>("endCardBet")!! }
    private val gameFragment by lazy { requireParentFragment() as EndCardGameFragment}

    override fun onInitView() {
        isCancelable=false
        initClick()
        initQuarter()
        setTips()
        initObservable()
    }

    private fun initClick()=binding.run{
        btnConfirm.clickDelay {
            dismiss()
        }
    }
    private fun initQuarter()=binding.run{
         tvQuarter1.text = " - $showCurrencySign ${endCardBet.lastDigit1}"
         tvQuarter2.text = " - $showCurrencySign ${endCardBet.lastDigit2}"
         tvQuarter3.text = " - $showCurrencySign ${endCardBet.lastDigit3}"
         tvQuarter4.text = " - $showCurrencySign ${endCardBet.lastDigit4}"
        binding.tvPrompt.text = Spanny(getString(R.string.P353))
            .findAndSpan(getString(R.string.prex_note)) { ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.color_00E701)) }
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
}