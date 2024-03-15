package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogMoneySubmitBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.finance.FinanceActivity
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.TextUtil

class MoneySubmitDialog : BaseDialog<BaseViewModel,DialogMoneySubmitBinding>() {

    companion object{
        fun newInstance(payWay: String, payMoney: String)= MoneySubmitDialog().apply {
            arguments = Bundle().apply {
                putString("payWay",payWay)
                putString("payMoney",payMoney)
            }
        }
    }
    init {
        marginHorizontal =30.dp
    }
    private val payWay by lazy { requireArguments().getString("payWay") }
    private val payMoney by lazy { requireArguments().getString("payMoney") }

    override fun onInitView() {
        initView()
        initButton()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        binding.txvPayWay.text = payWay
        binding.txvPayMoney.text = "${sConfigData?.systemCurrencySign} ${
            TextUtil.formatMoney(
                ArithUtil.toMoneyFormat(
                    payMoney?.toDouble()
                ).toDouble()
            )
        }"
    }

    fun initButton() {
        binding.tvViewLog.setOnClickListener {
            activity?.finish()
            startActivity(Intent(activity, FinanceActivity::class.java).apply {
                putExtra(
                    "rechargeViewLog", getString(R.string.record_recharge)
                )
            })
        }
        binding.tvService.setOnClickListener { view ->
            sConfigData?.customerServiceUrl?.let {
                JumpUtil.toExternalWeb(view.context, it)
            }
            dismiss()
        }
    }



}