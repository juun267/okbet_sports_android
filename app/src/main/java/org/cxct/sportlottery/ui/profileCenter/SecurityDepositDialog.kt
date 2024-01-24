package org.cxct.sportlottery.ui.profileCenter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import org.cxct.sportlottery.databinding.DialogSecurityDepositBinding

class SecurityDepositDialog : DialogFragment() {

    var depositText: CharSequence = ""
    var daysLeftText: CharSequence = ""
    private val binding by lazy { DialogSecurityDepositBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = binding.root
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initButton()
        dialog?.setCanceledOnTouchOutside(true)
    }

    private fun initViews() {
        binding.tvDeposit.text = depositText
        binding.tvDaysLeft.text = daysLeftText
    }

    private fun initButton() {
        binding.tvConfirm.setOnClickListener { dismiss() }
    }
}