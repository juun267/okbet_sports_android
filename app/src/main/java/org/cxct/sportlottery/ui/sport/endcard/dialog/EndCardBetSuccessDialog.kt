package org.cxct.sportlottery.ui.sport.endcard.dialog

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.databinding.DialogEndcardBetSuccessBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.sport.endcard.EndCardActivity
import org.cxct.sportlottery.ui.sport.endcard.record.EndCardRecordOddAdapter
import org.cxct.sportlottery.util.SpaceItemDecoration

class EndCardBetSuccessDialog: BaseDialog<BaseViewModel,DialogEndcardBetSuccessBinding>() {

    private val oddAdapter by lazy { EndCardRecordOddAdapter() }

    override fun onInitView() {
        initClick()
        initOddList()
    }
    private fun initClick()=binding.run{
        setOnClickListeners(ivClose,btnConfirm){
            dismiss()
        }
        tvOddDetail.setOnClickListener {
            (requireActivity() as EndCardActivity).showRecordDetail("100")
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