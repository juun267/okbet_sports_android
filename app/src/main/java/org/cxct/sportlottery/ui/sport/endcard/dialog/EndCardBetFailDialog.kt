package org.cxct.sportlottery.ui.sport.endcard.dialog

import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.databinding.DialogEndcardBetFailBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel

class EndCardBetFailDialog: BaseDialog<BaseViewModel, DialogEndcardBetFailBinding>() {

    override fun onInitView() {
        initClick()
    }
    private fun initClick()=binding.run{
        setOnClickListeners(ivClose,btnConfirm){
            dismiss()
        }
    }
}