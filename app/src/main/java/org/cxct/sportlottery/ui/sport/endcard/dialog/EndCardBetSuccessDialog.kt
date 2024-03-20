package org.cxct.sportlottery.ui.sport.endcard.dialog

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.databinding.DialogEndcardBetSuccessBinding
import org.cxct.sportlottery.network.bet.add.betReceipt.MatchOdd
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.sport.endcard.EndCardActivity
import org.cxct.sportlottery.ui.sport.endcard.record.EndCardRecordOddAdapter
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import java.util.*

class EndCardBetSuccessDialog: BaseDialog<BaseViewModel,DialogEndcardBetSuccessBinding>() {

    companion object{
        fun newInstance(receipt: Receipt)=EndCardBetSuccessDialog().apply {
            arguments = Bundle().apply {
                putParcelable("receipt",receipt)
            }
        }
    }
    init {
        marginHorizontal = 12.dp
    }
    private val oddAdapter by lazy { EndCardRecordOddAdapter() }
    private val receipt by lazy { arguments?.getParcelable<Receipt>("receipt")!! }
    private var matchOdd: MatchOdd?=null

    override fun onInitView() {
        initClick()
        matchOdd = receipt.singleBets?.firstOrNull()?.matchOdds?.firstOrNull()
        matchOdd?.let {
            initOddList()
            binding.tvHomeName.text = it.homeName
            binding.tvAwayName.text = it.awayName
            binding.tvBettingTime.text = TimeUtil.timeFormat(it.startTime, TimeUtil.DMY_HM_FORMAT)
            binding.tvBet.text = "$showCurrencySign ${TextUtil.formatMoney(receipt.userPlayAmount?:0,2)}"
            binding.tvBetAmount.text = "$showCurrencySign ${TextUtil.formatMoney(receipt.totalStake?:0,2)}"
            binding.tvOrderNumber.text = receipt.singleBets?.firstOrNull()?.orderNo
        }
    }
    private fun initClick()=binding.run{
        setOnClickListeners(ivClose,btnConfirm){
            dismiss()
        }
        tvOddDetail.setOnClickListener {
            (requireActivity() as EndCardActivity).showBetRecord(1)
        }
    }
    private fun initOddList(){
        val multiCode = matchOdd?.multiCode?: listOf()
        binding.tvOddDetail.isVisible = multiCode.size >5
        binding.rvOdd.apply {
            layoutManager = LinearLayoutManager(context,RecyclerView.HORIZONTAL,false)
            addItemDecoration(SpaceItemDecoration(context,R.dimen.margin_4))
            adapter = oddAdapter
            oddAdapter.setList(multiCode.map { it.playName })
        }
    }
}