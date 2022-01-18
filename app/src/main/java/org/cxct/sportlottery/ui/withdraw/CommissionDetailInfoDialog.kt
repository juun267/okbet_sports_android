package org.cxct.sportlottery.ui.withdraw

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.dialog_commission_detail_info.*
import org.cxct.sportlottery.R

class CommissionDetailInfoDialog(context: Context) : AlertDialog(context) {

    private var layout = layoutInflater.inflate(R.layout.dialog_commission_detail_info, null)

    init {
        this.window?.setBackgroundDrawableResource(android.R.color.transparent)
        this.setCanceledOnTouchOutside(true)
        setView(layout)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initButton()
    }

    private fun initButton() {
        btn_close.setOnClickListener {
            dismiss()
        }
    }
}