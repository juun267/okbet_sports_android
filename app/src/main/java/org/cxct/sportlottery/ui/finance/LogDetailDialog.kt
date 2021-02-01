package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.dialog_log_detail.*
import kotlinx.android.synthetic.main.dialog_log_detail.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog

class LogDetailDialog : BaseDialog<FinanceViewModel>(FinanceViewModel::class) {
    init {
        setStyle(R.style.CustomDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_log_detail, container, false).apply {
            setupConfirmButton(this)
        }
    }

    private fun setupConfirmButton(view: View) {
        view.log_detail_confirm.setOnClickListener {
            dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.logDetail.observe(this.viewLifecycleOwner, Observer {
            log_detail_trans_num.text = it.orderNo
            log_detail_time.text = it.operatorTime
            log_detail_type.text = it.type
            log_detail_amount.text = it.amount
            log_detail_status.text = it.status
        })
    }
}