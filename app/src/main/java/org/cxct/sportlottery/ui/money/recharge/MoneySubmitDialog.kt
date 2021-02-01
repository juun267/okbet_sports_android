package org.cxct.sportlottery.ui.money.recharge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_money_submit.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.MoneyAddRequest
import org.cxct.sportlottery.ui.base.BaseDialog

class MoneySubmitDialog(payWay: String, payMoney: String) : BaseDialog<MoneyRechViewModel>(MoneyRechViewModel::class) {
    val _payWay = payWay
    val _payMoney = payMoney

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_money_submit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initButton()
    }

    private fun initView() {
        txv_pay_way.text = _payWay
        txv_pay_money.text = _payMoney
    }
    fun initButton(){
        img_close.setOnClickListener {
            dismiss()
        }
    }
}