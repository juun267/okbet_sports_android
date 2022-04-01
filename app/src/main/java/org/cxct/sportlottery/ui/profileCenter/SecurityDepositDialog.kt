package org.cxct.sportlottery.ui.profileCenter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_security_deposit.*
import org.cxct.sportlottery.R

class SecurityDepositDialog : DialogFragment() {

    var depositText: CharSequence = ""
    var daysLeftText: CharSequence = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.dialog_security_deposit, container, false)
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
        tvDeposit.text = depositText
        tvDaysLeft.text = daysLeftText
    }

    private fun initButton() {
        tvConfirm.setOnClickListener { dismiss() }
    }
}