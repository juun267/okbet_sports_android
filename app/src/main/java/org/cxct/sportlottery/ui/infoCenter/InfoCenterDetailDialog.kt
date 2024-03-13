package org.cxct.sportlottery.ui.infoCenter

import org.cxct.sportlottery.databinding.DialogInfoCenterDetailListBinding
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.util.DisplayUtil.dp

class InfoCenterDetailDialog: BaseDialog<InfoCenterViewModel,DialogInfoCenterDetailListBinding>() {

    init {
        marginHorizontal = 40.dp
    }
    private var data: InfoCenterData? = null
        get() {
            if (field == null) {
                field = arguments?.getParcelable("data")
            }
            return field
        }

    override fun onInitView() {
        binding.infoCenterDetailConfirm.setOnClickListener {
            dismiss()
        }

        data?.let {
            binding.txvTitle.text = it.title
            binding.txvDetail.text = it.content
        }
    }

}
