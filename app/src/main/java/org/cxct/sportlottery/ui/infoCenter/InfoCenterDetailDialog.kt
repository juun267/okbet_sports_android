package org.cxct.sportlottery.ui.infoCenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.databinding.DialogInfoCenterDetailListBinding
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.ui.base.BaseDialog

class InfoCenterDetailDialog: BaseDialog<InfoCenterViewModel,DialogInfoCenterDetailListBinding>() {


    private var data: InfoCenterData? = null
        get() {
            if (field == null) {
                field = arguments?.getParcelable("data")
            }
            return field
        }

    override fun onInitView() {
        binding.apply {
            infoCenterViewModel = this@InfoCenterDetailDialog.viewModel
            lifecycleOwner = this@InfoCenterDetailDialog
            infoCenterData = infoCenterData
        }
        binding.infoCenterDetailConfirm.setOnClickListener {
            dismiss()
        }

        data?.let {
            binding.txvTitle.text = it.title
            binding.txvDetail.text = it.content
        }
    }

}
