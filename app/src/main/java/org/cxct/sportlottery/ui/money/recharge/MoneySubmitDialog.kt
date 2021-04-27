package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_money_submit.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.util.ArithUtil

class MoneySubmitDialog(payWay: String, payMoney: String, private val dialogListener: MoneySubmitDialogListener)  : DialogFragment() {
    val _payWay = payWay
    val _payMoney = payMoney

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        dialog?.setCanceledOnTouchOutside(true)
        return inflater.inflate(R.layout.dialog_money_submit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initButton()
    }

    private fun initView() {
        txv_pay_way.text = _payWay
        txv_pay_money.text = "${ArithUtil.toMoneyFormat(_payMoney.toDouble())} ${getString(R.string.currency)}"
    }

    fun initButton() {
        img_close.setOnClickListener {
            dismiss()
        }
        tv_view_log.setOnClickListener { dialogListener.viewLog() }
        tv_service.setOnClickListener {
            dialogListener.contactService()
            dismiss()
        }
    }

    class MoneySubmitDialogListener(private val viewLogEvent: () -> Unit, private val contactServiceEvent: () -> Unit) {
        fun viewLog() {
            viewLogEvent.invoke()
        }

        fun contactService() {
            contactServiceEvent.invoke()
        }
    }
}