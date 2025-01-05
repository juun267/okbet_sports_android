package org.cxct.sportlottery.ui.login

import android.view.Gravity
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogAgreeTcBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity

class AgreeTCDialog: BaseDialog<BaseViewModel,DialogAgreeTcBinding>() {

    companion object{
        fun newInstance() = AgreeTCDialog()
    }

    override fun onInitView() {
        setLayoutParams()
        initView()
    }
    private fun initView() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }
        binding.btnAgree.setOnClickListener {
            dismiss()
            (requireActivity() as? LoginOKActivity)?.agreeTC()
        }
        binding.btnCancel.setOnClickListener {
           dismiss()
        }
    }
    private fun setLayoutParams() {
        dialog?.window?.let { window->
            val lp = window.attributes
            lp.width= ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.gravity = Gravity.BOTTOM
        }
    }
}