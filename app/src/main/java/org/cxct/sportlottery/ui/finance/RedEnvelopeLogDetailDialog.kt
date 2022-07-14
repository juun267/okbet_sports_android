package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_redenvelope_log_detail.*
import kotlinx.android.synthetic.main.dialog_withdraw_log_detail.view.*
import kotlinx.android.synthetic.main.dialog_withdraw_log_detail.wd_log_detail_amount
import kotlinx.android.synthetic.main.dialog_withdraw_log_detail.wd_log_detail_time
import kotlinx.android.synthetic.main.dialog_withdraw_log_detail.wd_log_detail_trans_num
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog

@Deprecated("需求上無須展示")
class RedEnvelopeLogDetailDialog : BaseDialog<FinanceViewModel>(FinanceViewModel::class) {
    init {
        setStyle(R.style.CustomDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_redenvelope_log_detail, container, false).apply {
            setupConfirmButton(this)
        }
    }

    private fun setupConfirmButton(view: View) {
        view.log_detail_confirm.setOnClickListener {
            dismiss()
        }
    }
}