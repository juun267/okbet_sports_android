package org.cxct.sportlottery.ui.money.withdraw

import org.cxct.sportlottery.databinding.DialogCommissionInfoBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp

class CommissionInfoDialog: BaseDialog<BaseViewModel,DialogCommissionInfoBinding>() {

    init {
        marginHorizontal = 20.dp
    }

    override fun onInitView() {
        initButton()
        isCancelable  = true
    }


    private fun initButton() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }


}