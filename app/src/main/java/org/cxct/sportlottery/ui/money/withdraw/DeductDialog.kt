package org.cxct.sportlottery.ui.money.withdraw

import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogDeductBinding
import org.cxct.sportlottery.network.withdraw.uwcheck.UwCheckData
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.Spanny

class DeductDialog(private val uwCheckData: UwCheckData, private val onConfirm: ()->Unit): BaseDialog<BaseViewModel, DialogDeductBinding>() {

    override fun onInitView() {
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
            binding.tvDescription.text = it
        }
    }

    private fun initButton()=binding.run {
        btnConfirm.setOnClickListener {
            dismiss()
            onConfirm.invoke()
        }
        btnCancel.setOnClickListener {
            dismiss()
        }
        btnClose.setOnClickListener {
            dismiss()
        }
    }
}