package org.cxct.sportlottery.ui.sport.endcard.dialog

import android.graphics.Color
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.common.extentions.toStringS
import org.cxct.sportlottery.common.extentions.toast
import org.cxct.sportlottery.databinding.DialogEndcardBetBinding
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.sport.endcard.EndCardBetManager
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.ui.sport.endcard.bet.EndCardGameFragment
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.GridItemDecoration
import org.cxct.sportlottery.util.Spanny

class EndCardBetDialog: BaseDialog<EndCardVM, DialogEndcardBetBinding>() {

    companion object{
        fun newInstance(matchId: String,stake: Int)= EndCardBetDialog().apply {
            arguments = Bundle().apply {
                putString("matchId",matchId)
                putInt("stake",stake)
            }
        }
    }
    init {
        marginHorizontal = 12.dp
    }
    private val oddAdapter by lazy { EndCardBetOddAdapter() }
    private val matchId by lazy { arguments?.getString("matchId")!! }
    private val stake by lazy { arguments?.getInt("stake")!! }
    private val gameFragment by lazy { requireParentFragment() as EndCardGameFragment}

    override fun onInitView() {
        isCancelable=false
        initClick()
        initOddList()
        setTips()
        initObservable()
    }

    private fun initClick()=binding.run{
        setOnClickListeners(ivClose){
            gameFragment.showFloatBet()
            dismiss()
        }
        btnAddMore.clickDelay {
            gameFragment.showFloatBet()
            dismiss()
        }
        btnBetting.clickDelay() {
            gameFragment.loading()
            viewModel.addBetLGPCOFL(matchId,EndCardBetManager.getBetOdds(),viewModel.userInfo.value?.nickName?:"",stake)
        }
    }
    private fun initOddList(){
        binding.rvOdd.apply {
            layoutManager = GridLayoutManager(context,5)
            addItemDecoration(GridItemDecoration(6.dp,8.dp, Color.TRANSPARENT,false))
            adapter = oddAdapter
            oddAdapter.setList(EndCardBetManager.getBetOdds())
        }
    }
    private fun setTips(){
        val betMoney =  "$showCurrencySign $stake"
        val oddNames =  "${EndCardBetManager.getBetOdds().joinToString("," )}"
        val totalWin = "$showCurrencySign ${stake*EndCardBetManager.getBetOdds().size}"
        val notice = String.format(getString(R.string.P306),betMoney,oddNames,totalWin)
        binding.tvTips.text = Spanny(notice)
            .findAndSpan(betMoney) { ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.color_6AA4FF)) }
            .findAndSpan(oddNames) { ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.color_6AA4FF)) }
            .findAndSpan(totalWin) { ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.color_6AA4FF)) }
    }
    private fun initObservable(){
        viewModel.addBetResult.observe(this){
            gameFragment.hideLoading()
            gameFragment.clearAllEndCardBet()
            gameFragment.showFloatBet()
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