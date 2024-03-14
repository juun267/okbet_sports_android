package org.cxct.sportlottery.view.dialog

import android.os.Bundle
import android.view.*
import androidx.fragment.app.FragmentManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.databinding.DialogAgeVerifyBinding
import org.cxct.sportlottery.util.KvUtils
import org.cxct.sportlottery.view.dialog.queue.BasePriorityDialog
import org.cxct.sportlottery.view.dialog.queue.PriorityDialog

class AgeVerifyDialog : BaseDialog<BaseViewModel,DialogAgeVerifyBinding>() {

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
        fun newInstance() = AgeVerifyDialog()

        fun buildAgeVerifyDialog(priority: Int, fm: () -> FragmentManager): PriorityDialog? {
            if (!isAgeVerifyNeedShow) {
                return null
            }

            isAgeVerifyNeedShow = false
            return object : BasePriorityDialog<AgeVerifyDialog>() {
                override fun getFragmentManager() = fm.invoke()
                override fun priority() = priority
                override fun createDialog() = AgeVerifyDialog.newInstance()
            }
        }
    }

    override fun onInitView() {
        binding.btnConfirm.setOnClickListener {
            dismiss()
        }
        binding.btnExit.setOnClickListener {
            dismiss()
        }
    }

}
