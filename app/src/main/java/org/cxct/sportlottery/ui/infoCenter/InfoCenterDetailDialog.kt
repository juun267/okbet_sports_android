package org.cxct.sportlottery.ui.infoCenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.databinding.DialogInfoCenterDetailListBinding
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.ui.base.BaseDialog

class InfoCenterDetailDialog: BaseDialog<InfoCenterViewModel>(InfoCenterViewModel::class) {


    private var data: InfoCenterData? = null
        get() {
            if (field == null) {
                field = arguments?.getParcelable("data")
            }
            return field
        }

    private val binding by lazy { DialogInfoCenterDetailListBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.apply {
            infoCenterViewModel = this@InfoCenterDetailDialog.viewModel
            lifecycleOwner = this@InfoCenterDetailDialog
            infoCenterData = infoCenterData
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.infoCenterDetailConfirm.setOnClickListener {
            dismiss()
        }

        data?.let {
            binding.txvTitle.text = it.title
            binding.txvDetail.text = it.content
        }
    }
}
