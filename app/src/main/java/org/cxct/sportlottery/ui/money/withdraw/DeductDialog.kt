package org.cxct.sportlottery.ui.money.withdraw

import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogDeductBinding
import org.cxct.sportlottery.network.withdraw.uwcheck.UwCheckData
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.Spanny

class DeductDialog: BaseDialog<BaseViewModel, DialogDeductBinding>() {

    companion object{
        fun newInstance(uwCheckData: UwCheckData)=DeductDialog().apply {
            arguments = Bundle().apply {
                putParcelable("uwCheckData",uwCheckData)
            }
        }
    }
    init {
        marginHorizontal = 40.dp
    }
    private val uwCheckData by lazy { requireArguments().getParcelable<UwCheckData>("uwCheckData")!! }

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
            when(val parentFragment=requireParentFragment()){
                is WithdrawFragment-> parentFragment.addWithdraw()
                is BetStationFragment-> parentFragment.addWithdraw()
            }
        }
        btnCancel.setOnClickListener {
            dismiss()
        }
        btnClose.setOnClickListener {
            dismiss()
        }
    }
}