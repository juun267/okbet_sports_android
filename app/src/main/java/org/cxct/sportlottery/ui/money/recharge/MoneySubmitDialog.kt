package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_money_submit.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.TextUtil

class MoneySubmitDialog(
    private val payWay: String,
    private val payMoney: String,
    private val dialogListener: MoneySubmitDialogListener
) : DialogFragment() {

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

    @SuppressLint("SetTextI18n")
    private fun initView() {
        txv_pay_way.text = payWay
        txv_pay_money.text =
            "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(ArithUtil.toMoneyFormat(payMoney.toDouble()).toDouble())}"
    }

    fun initButton() {
        tv_view_log.setOnClickListener { dialogListener.viewLog() }
        tv_service.setOnClickListener {
            dialogListener.contactService()
            dismiss()
        }
    }

    class MoneySubmitDialogListener(
        private val viewLogEvent: () -> Unit,
        private val contactServiceEvent: () -> Unit
    ) {
        fun viewLog() {
            viewLogEvent.invoke()
        }

        fun contactService() {
            contactServiceEvent.invoke()
        }
    }
}