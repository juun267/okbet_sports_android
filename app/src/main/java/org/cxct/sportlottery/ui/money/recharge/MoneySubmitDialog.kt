package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_money_submit.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.finance.FinanceActivity
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.TextUtil
import timber.log.Timber

class MoneySubmitDialog() : DialogFragment() {

    private val payWay by lazy {
        arguments?.getString("payWay")
    }
    private val payMoney by lazy {
        arguments?.getString("payMoney")
    }

    constructor(
        payWay: String? = "",
        payMoney: String? = "",
    ) : this() {
        val bundle = Bundle()
        bundle.putString("payWay", payWay)
        bundle.putString("payMoney", payMoney)
        arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
        txv_pay_money.text = "${sConfigData?.systemCurrencySign} ${
            TextUtil.formatMoney(
                ArithUtil.toMoneyFormat(
                    payMoney?.toDouble()
                ).toDouble()
            )
        }"
    }

    fun initButton() {
        tv_view_log.setOnClickListener {
            activity?.finish()
            startActivity(Intent(activity, FinanceActivity::class.java).apply {
                putExtra(
                    "rechargeViewLog", getString(R.string.record_recharge)
                )
            })
        }
        tv_service.setOnClickListener {
            kotlin.runCatching { Uri.parse(sConfigData?.customerServiceUrl) }.getOrNull()?.let {
                startActivity(Intent(Intent.ACTION_VIEW).setData(it))
            }
            dismiss()
        }
    }

}