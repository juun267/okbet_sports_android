package org.cxct.sportlottery.ui.money.withdraw

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogCommissionInfoBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp

class CommissionInfoDialog: BaseDialog<BaseViewModel,DialogCommissionInfoBinding>() {

    init {
        setStyle(R.style.CustomDialogStyle)
    }

    override fun onInitView() {
        initButton()
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20.dp))
    }


    private fun initButton() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }


}