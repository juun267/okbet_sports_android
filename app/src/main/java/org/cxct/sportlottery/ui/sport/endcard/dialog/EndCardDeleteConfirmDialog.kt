package org.cxct.sportlottery.ui.sport.endcard.dialog

import org.cxct.sportlottery.databinding.DialogEndcardDeleteConfirmBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.sport.endcard.bet.EndCardGameFragment
import org.cxct.sportlottery.util.DisplayUtil.dp

class EndCardDeleteConfirmDialog: BaseDialog<BaseViewModel, DialogEndcardDeleteConfirmBinding>() {

    companion object{
        fun newInstance()= EndCardDeleteConfirmDialog()
    }
    init {
        marginHorizontal = 12.dp
    }

    override fun onInitView() {
        initClick()
    }
    private fun initClick()=binding.run{
        ivClose.setOnClickListener {
            dismiss()
        }
        btnBet.setOnClickListener {
            dismiss()
            (requireParentFragment() as EndCardBetDialog).addBet()
        }
        btnConfirm.setOnClickListener {
            dismiss()
            (requireParentFragment() as EndCardBetDialog).clearAll()
        }
    }
}