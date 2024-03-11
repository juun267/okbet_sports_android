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
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.TextUtil

class MoneySubmitDialog(payWayStr: String = "", payMoneyStr: String? = "") : BaseDialog<BaseViewModel,DialogMoneySubmitBinding>() {

    init {
        val bundle = Bundle()
        bundle.putString("payWay", payWayStr)
        bundle.putString("payMoney", payMoneyStr)
        arguments = bundle
    }

    private val payWay by lazy {
        arguments?.getString("payWay")
    }
    private val payMoney by lazy {
        arguments?.getString("payMoney")
    }

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