package org.cxct.sportlottery.ui.common.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.view.ViewGroup
import org.cxct.sportlottery.databinding.DialogServiceBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.JumpUtil

class ServiceDialog : BaseDialog<BaseViewModel,DialogServiceBinding>() {

    override fun onInitView() {
        initView()
        initServiceEvent()
    }
    private fun initView() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), 48.dp))
    }

    private fun initServiceEvent() {
        binding.frameService1.setOnClickListener {
            JumpUtil.toExternalWeb(context ?: requireContext(), sConfigData?.customerServiceUrl)
            dismiss()
        }

        binding.frameService2.setOnClickListener {
            JumpUtil.toExternalWeb(context ?: requireContext(), sConfigData?.customerServiceUrl2)
            dismiss()
        }
    }


}