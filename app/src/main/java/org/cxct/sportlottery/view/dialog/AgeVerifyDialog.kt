package org.cxct.sportlottery.view.dialog

import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.databinding.DialogAgeVerifyBinding
import org.cxct.sportlottery.util.KvUtils

class AgeVerifyDialog(val onConfirm: ()->Unit,val onExit: ()->Unit) : BaseDialog<BaseViewModel,DialogAgeVerifyBinding>() {

    init {
        setStyle(R.style.FullScreen)
    }
    companion object{
         var isAgeVerifyNeedShow :Boolean = true
             get() = KvUtils.decodeBooleanTure("isAgeVerifyNeedShow",true)
             set(value) {
                field = value
                KvUtils.put("isAgeVerifyNeedShow",value)
             }
    }

    override fun onInitView() {
        binding.btnConfirm.setOnClickListener {
            dismiss()
            onConfirm.invoke()
        }
        binding.btnExit.setOnClickListener {
            dismiss()
            onExit.invoke()
        }
    }

}
