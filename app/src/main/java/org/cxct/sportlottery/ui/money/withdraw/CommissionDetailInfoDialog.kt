package org.cxct.sportlottery.ui.money.withdraw

import org.cxct.sportlottery.databinding.DialogCommissionDetailInfoBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp

class CommissionDetailInfoDialog : BaseDialog<BaseViewModel,DialogCommissionDetailInfoBinding>() {

    init {
        marginHorizontal = 20.dp
    }
    override fun onInitView() {
        initButton()
    }

    private fun initButton() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }
}