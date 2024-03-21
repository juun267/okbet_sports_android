package org.cxct.sportlottery.ui.infoCenter

import android.os.Bundle
import org.cxct.sportlottery.databinding.DialogInfoCenterDetailListBinding
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.util.DisplayUtil.dp

class InfoCenterDetailDialog: BaseDialog<InfoCenterViewModel,DialogInfoCenterDetailListBinding>() {

    companion object{
        fun newInstance(data: InfoCenterData) = InfoCenterDetailDialog().apply {
            arguments = Bundle().apply {
                putParcelable("data",data)
            }
        }
    }
    init {
        marginHorizontal = 40.dp
    }
    private val data by lazy { requireArguments().getParcelable<InfoCenterData>("data") }
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
