package org.cxct.sportlottery.view.dialog

import android.os.Handler
import android.os.Looper
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogPromotionSuccessBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.view.onClick

class PromotionSuccessDialog : BaseDialog<BaseViewModel,DialogPromotionSuccessBinding>() {
    init {
        setStyle(R.style.FullScreen)
    }

    companion object {
        @JvmStatic
        fun newInstance() = PromotionSuccessDialog()
    }

    override fun onInitView() {
        dialog?.window?.setDimAmount(0f)
        isCancelable =false
        binding.root.onClick { dismissAllowingStateLoss() }
        Handler(Looper.getMainLooper()).postDelayed({ dismissAllowingStateLoss() },2000)
    }
}