package org.cxct.sportlottery.ui.money.withdraw

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_commission_info.*
import kotlinx.android.synthetic.main.dialog_commission_info.btn_close
import kotlinx.android.synthetic.main.dialog_deduct.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.withdraw.uwcheck.CheckList
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil

class DeductDialog(val checkList: CheckList, val onConfirm: ()->Unit): DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.dialog_deduct, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), 40.dp))
        tvDescription.text = String.format(getString(R.string.P070,"${checkList.unFinishValidAmount ?: 0}","${checkList.deductMoney ?: 0}"))
        progressBar.progress = (checkList.finishValidAmount?:0).toInt()
        progressBar.max = (checkList.validCheckAmount?:0).toInt()
        tvCurrent.text = "${sConfigData?.systemCurrencySign} ${checkList.finishValidAmount?:0}"
        tvTotal.text = "${sConfigData?.systemCurrencySign} ${checkList.validCheckAmount?:0}"
    }

    private fun initButton() {
        btnConfirm.setOnClickListener {
            dismiss()
            onConfirm.invoke()
        }
        btnCancel.setOnClickListener {
            dismiss()
        }
        btn_close.setOnClickListener {
            dismiss()
        }
    }
}