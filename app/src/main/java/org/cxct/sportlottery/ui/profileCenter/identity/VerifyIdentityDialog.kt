package org.cxct.sportlottery.ui.profileCenter.identity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import org.cxct.sportlottery.databinding.DialogVerifyIdentityBinding
import org.cxct.sportlottery.util.jumpToKYC
import org.cxct.sportlottery.util.setServiceClick

class VerifyIdentityDialog: DialogFragment() {

    private val binding by lazy { DialogVerifyIdentityBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCustomDialogStyle()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
    }

    private fun initButton()=binding.run {
        btnClose.setOnClickListener { dismiss() }
        btnService.root.setServiceClick(childFragmentManager)
        btnCheck.setOnClickListener {
            requireActivity().jumpToKYC()
            dismiss()
        }
    }

    private fun setCustomDialogStyle() {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setGravity(Gravity.CENTER)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        dialog?.setCanceledOnTouchOutside(false)
    }

}