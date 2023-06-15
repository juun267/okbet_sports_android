package org.cxct.sportlottery.ui.money.withdraw

import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.dialog_commission_info.btn_close
import kotlinx.android.synthetic.main.dialog_deduct.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.withdraw.uwcheck.CheckList
import org.cxct.sportlottery.network.withdraw.uwcheck.UwCheckData
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.Spanny

class DeductDialog(val uwCheckData: UwCheckData, val onConfirm: ()->Unit): BaseDialog<BaseViewModel>(BaseViewModel::class) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.dialog_deduct, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
        dialog?.setCanceledOnTouchOutside(true)
        var deductMoney = "${sConfigData?.systemCurrencySign} ${(uwCheckData.total?.deductMoney?:0).toInt()}"
        var unFinishValidMoney = "${sConfigData?.systemCurrencySign} ${(uwCheckData.total?.unFinishValidAmount?:0).toInt()}"
        val desp = String.format(getString(R.string.P070,unFinishValidMoney,deductMoney))
        val startIndex= desp.lastIndexOf(deductMoney)
        val endIndex = startIndex + deductMoney.length
        Spanny(desp).apply {
            setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.color_0D2245)),startIndex,endIndex,Spanny.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(android.graphics.Typeface.BOLD),startIndex,endIndex,Spanny.SPAN_EXCLUSIVE_EXCLUSIVE)
        }.let {
            tvDescription.text = it
        }
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