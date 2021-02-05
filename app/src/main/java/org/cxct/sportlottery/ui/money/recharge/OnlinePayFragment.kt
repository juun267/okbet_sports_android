package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.online_pay_fragment.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.MoneyRechCfg
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.CustomImageAdapter
import org.cxct.sportlottery.util.MoneyManager

class OnlinePayFragment : BaseFragment<MoneyRechViewModel>(MoneyRechViewModel::class) {

    companion object {
        private const val TAG = "OnlinePayFragment"
    }

    private var mMoneyPayWay: MoneyPayWayData? = MoneyPayWayData("", "", "", "", 0) //支付類型

    private var mSelectRechCfgs: MoneyRechCfg.RechConfig? = null //選擇的入款帳號

    private val mSpannerList: MutableList<CustomImageAdapter.SelectBank> by lazy {
        mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.online_pay_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPayRoadSpinner()

        initButton()
        initView()
        initObserve()

    }

    private fun initObserve() {
        //充值金額訊息
        viewModel.rechargeOnlineAmountMsg.observe(viewLifecycleOwner, {
            et_recharge_online_amount.setError(it)
        })
    }

    private fun initView() {
        if (mMoneyPayWay?.image != "ic_online_pay") {
            cv_pay_bank.visibility = View.GONE
        } else {
            cv_pay_bank.visibility = View.VISIBLE
        }

        setupTextChangeEvent()
        setupFocusEvent()
    }

    private fun initButton() {
        btn_submit.setOnClickListener {
            val bankCode = mSelectRechCfgs?.banks?.get(sp_pay_bank?.selectedItemPosition ?: 0)?.value
            val depositMoney = if (et_recharge_online_amount.getText().isNotEmpty()) {
                et_recharge_online_amount.getText().toInt()
            } else {
                0
            }
            viewModel.rechargeOnlinePay(requireContext(), mSelectRechCfgs?.id ?: 0, depositMoney, bankCode)
        }
    }

    fun setArguments(moneyPayWay: MoneyPayWayData?): OnlinePayFragment {
        mMoneyPayWay = moneyPayWay
        return this
    }

    private fun initPayRoadSpinner() {
        //支付類型的入款帳號清單
        val rechCfgsList = viewModel.rechargeConfigs.value?.rechCfgs?.filter {
            it.rechType == mMoneyPayWay?.rechType && it.onlineType == mMoneyPayWay?.onlineType && it.pcMobile != 1
        } ?: mutableListOf()

        //產生對應 spinner 選單
        var count = 1

        val payRoadSpannerList = mutableListOf<String>()
        val title = mMoneyPayWay?.title
        if (rechCfgsList.size > 1)
            rechCfgsList.forEach { _ -> payRoadSpannerList.add(title + count++) }
        else
            rechCfgsList.forEach { _ -> payRoadSpannerList.add(title + "") }

        sp_pay_gap.adapter =
            ArrayAdapter(requireContext(), R.layout.spinner_text_item, payRoadSpannerList)

        //選擇入款帳號
        sp_pay_gap.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                try {
                    mSelectRechCfgs = rechCfgsList[position]
                    refreshSelectRechCfgs(mSelectRechCfgs)
                    refreshPayBank(mSelectRechCfgs)
                    clearFocus()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        //default 選擇第一個不為 null 的入款帳號資料
        if (sp_pay_gap.count > 0)
            sp_pay_gap.setSelection(0)
        else
            mSelectRechCfgs = null
    }

    //依據選擇的支付渠道，刷新UI
    @SuppressLint("SetTextI18n")
    private fun refreshSelectRechCfgs(selectRechCfgs: MoneyRechCfg.RechConfig?) {
        //手續費率/贈送費率 <0是手續費 >0是贈送費率 PM還在討論
        txv_rebate.text = "${selectRechCfgs?.rebateFee.toString()}%"
//        et_recharge_online_amount.hint = String.format(
//            getString(R.string.edt_hint_online_pay_money),
//            "${selectRechCfgs?.minMoney}",
//            "${selectRechCfgs?.maxMoney}"
//        )
        tv_hint.text = mSelectRechCfgs?.remark
    }

    private fun refreshPayBank(rechCfgsList: MoneyRechCfg.RechConfig?) {

        rechCfgsList?.banks?.forEach {
            val data =
                CustomImageAdapter.SelectBank(
                    it.bankName,
                    MoneyManager.getBankIconByBankName(it.bankName.toString())
                )
            mSpannerList.add(data)
        }
        sp_pay_bank.adapter = CustomImageAdapter(context, mSpannerList)
        sp_pay_bank.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //do nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                clearFocus()
            }

        }

    }

    private fun setupTextChangeEvent() {
        viewModel.apply {
            //充值金額
            et_recharge_online_amount.afterTextChanged { checkRcgOnlineAmount(it, mSelectRechCfgs) }
        }
    }

    private fun setupFocusEvent() {
        et_recharge_online_amount.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                viewModel.checkRcgOnlineAmount(et_recharge_online_amount.getText(), mSelectRechCfgs)
        }
    }


}