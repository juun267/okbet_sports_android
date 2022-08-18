package org.cxct.sportlottery.ui.login.signUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_agreement.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog

@Deprecated("no usage")
class AgreementDialog : BaseDialog<RegisterViewModel>(RegisterViewModel::class) {

    init {
        setStyle(R.style.Common)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_agreement, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupConfirmButton()

    }

    private fun setupConfirmButton() {
        btn_confirm.setOnClickListener {
            dismiss()
        }
    }


}