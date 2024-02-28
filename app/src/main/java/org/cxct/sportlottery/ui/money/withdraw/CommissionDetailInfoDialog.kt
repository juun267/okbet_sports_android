package org.cxct.sportlottery.ui.money.withdraw

import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogCommissionDetailInfoBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel

class CommissionDetailInfoDialog : BaseDialog<BaseViewModel,DialogCommissionDetailInfoBinding>() {

    init {
        setStyle(R.style.CustomDialogStyle)
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