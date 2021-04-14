package org.cxct.sportlottery.ui.infoCenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.dialog_info_center_detail_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogInfoCenterDetailListBinding
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.ui.base.BaseDialog

class InfoCenterDetailDialog(val data: InfoCenterData) :
    BaseDialog<InfoCenterViewModel>(InfoCenterViewModel::class) {

    init {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) //hide keyboard
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: DialogInfoCenterDetailListBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_info_center_detail_list,
            container,
            false
        )

        binding.apply {
            infoCenterViewModel = this@InfoCenterDetailDialog.viewModel
            lifecycleOwner = this@InfoCenterDetailDialog
            infoCenterData = infoCenterData
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        info_center_detail_confirm.setOnClickListener {
            dismiss()
        }
        txv_title.text = data.title
        txv_detail.text = data.content
    }
}