package org.cxct.sportlottery.ui.sport.endcard.dialog

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.databinding.DialogEndcardBetSuccessBinding
import org.cxct.sportlottery.network.bet.add.betReceipt.BetAddResult
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.sport.endcard.EndCardActivity
import org.cxct.sportlottery.ui.sport.endcard.record.EndCardRecordOddAdapter
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.SpaceItemDecoration

class EndCardBetSuccessDialog: BaseDialog<BaseViewModel,DialogEndcardBetSuccessBinding>() {

    init {
        marginHorizontal = 12.dp
    }
    private val oddAdapter by lazy { EndCardRecordOddAdapter() }
    private val betResult by lazy { arguments?.getParcelable<Row>("betResult")!! }

    override fun onInitView() {
        initClick()
        initOddList()
    }
    private fun initClick()=binding.run{
        setOnClickListeners(ivClose,btnConfirm){
            dismiss()
        }
        tvOddDetail.setOnClickListener {
            (requireActivity() as EndCardActivity).showRecordDetail(betResult)
        }
    }
    private fun initOddList(){
        binding.rvOdd.apply {
            layoutManager = LinearLayoutManager(context,RecyclerView.HORIZONTAL,false)
            addItemDecoration(SpaceItemDecoration(context,R.dimen.margin_4))
            adapter = oddAdapter
            oddAdapter.setList(listOf("","","",""))
        }
    }
}