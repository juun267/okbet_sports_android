package org.cxct.sportlottery.ui.money.withdraw

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_commission_detail_info.*
import org.cxct.sportlottery.R

class CommissionDetailInfoDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View?{
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      var view  = inflater.inflate(R.layout.dialog_commission_detail_info, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
        dialog?.setCanceledOnTouchOutside(true)
    }

    private fun initButton() {
        btn_close.setOnClickListener {
            dismiss()
        }
    }
}