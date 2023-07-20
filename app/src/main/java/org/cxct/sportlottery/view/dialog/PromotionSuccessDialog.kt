package org.cxct.sportlottery.view.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogPromotionPopupBinding
import org.cxct.sportlottery.databinding.DialogPromotionSuccessBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.view.onClick

class PromotionSuccessDialog : BaseDialog<BaseViewModel>(BaseViewModel::class) {
    init {
        setStyle(R.style.FullScreen)
    }
    lateinit var binding :DialogPromotionSuccessBinding

    companion object {
        @JvmStatic
        fun newInstance() = PromotionSuccessDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding=DialogPromotionSuccessBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setDimAmount(0f)
        isCancelable =true
        binding.root.onClick { dismissAllowingStateLoss() }

    }


}