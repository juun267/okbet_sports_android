package org.cxct.sportlottery.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_redenvelope_success.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.TextUtil

class RedEnvelopeSuccessDialog : BaseDialog<BaseViewModel>(BaseViewModel::class) {
    init {
        setStyle(R.style.CustomDialogStyle)
    }
    companion object {
        const val AMOUNT = "amount"

        @JvmStatic
        fun newInstance(amount: Double) = RedEnvelopeSuccessDialog().apply {
            arguments = Bundle().apply {
                putDouble(AMOUNT, amount)
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_redenvelope_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val amount=arguments?.getDouble(AMOUNT)?:0
        tv_amount.text= BuildConfig.SYSTEM_CURREMCY_SIGN+" "+TextUtil.formatMoney(amount)
        iv_close.setOnClickListener {
            dismiss()
        }
    }


}