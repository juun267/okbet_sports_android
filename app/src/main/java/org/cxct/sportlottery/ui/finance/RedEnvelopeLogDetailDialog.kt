package org.cxct.sportlottery.ui.finance

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_redenvelope_log_detail.view.*
import kotlinx.android.synthetic.main.dialog_withdraw_log_detail.view.log_detail_confirm
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog

@Deprecated("需求上無須展示")
class RedEnvelopeLogDetailDialog : BaseDialog<FinanceViewModel>(FinanceViewModel::class) {
    init {
        setStyle(R.style.CustomDialogStyle)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_redenvelope_log_detail, container, false).apply {
            setupConfirmButton(this)
            this.wd_log_detail_type_title.text = "${getString(R.string.tran_type)}："
            this.wd_log_detail_time_subtitle.text = "${getString(R.string.text_account_history_time)}："
        }
    }


    private fun setupConfirmButton(view: View) {
        view.log_detail_confirm.setOnClickListener {
            dismiss()
        }
    }
}