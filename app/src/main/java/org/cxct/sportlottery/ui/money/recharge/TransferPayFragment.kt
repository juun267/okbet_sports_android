package org.cxct.sportlottery.ui.money.recharge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.transfer_pay_fragment.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MoneyType
import org.cxct.sportlottery.network.money.MoneyAddRequest
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.MoneyRechCfg
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.CustomImageAdapter
import org.cxct.sportlottery.util.MoneyManager.getBankAccountIcon
import org.cxct.sportlottery.util.MoneyManager.getBankIcon
import org.cxct.sportlottery.util.MoneyManager.getBankIconByBankName
import org.cxct.sportlottery.util.TimeUtil
import java.util.*

class TransferPayFragment : BaseFragment<MoneyRechViewModel>(MoneyRechViewModel::class) {

    companion object {
        private const val TAG = "TransferPayFragment"
    }

    private var mMoneyPayWay: MoneyPayWayData? = MoneyPayWayData("", "", "", "", 0) //支付類型

    private var mSelectRechCfgs: MoneyRechCfg.RechConfig? = null //選擇的入款帳號

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.transfer_pay_fragment, container, false)
    }

    fun setArguments(moneyPayWay: MoneyPayWayData?): TransferPayFragment {
        mMoneyPayWay = moneyPayWay
        return this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPayAccountSpinner()
        initButton()
//        initTimePicker()
    }

    private fun initButton() {
        btn_submit.setOnClickListener {
            var moneyAddRequest: MoneyAddRequest = MoneyAddRequest(
                rechCfgId = mSelectRechCfgs?.id ?: 0,
                bankCode = "",
                depositMoney = et_money.text.toString().toInt(),
                payer = et_bank_account.text.toString(),
                payerName = et_name.text.toString(),
                payerBankName = "aaa",
                payerInfo = "payerInfo",
                depositDate = Date().time
            )
            viewModel.rechargeAdd(moneyAddRequest)
        }
    }

    //入款帳號選單
    private fun initPayAccountSpinner() {
        //支付類型的入款帳號清單
        var rechCfgsList = viewModel.rechargeConfigs.value?.rechCfgs?.filter {
            it.rechType == mMoneyPayWay?.rechType
        } ?: mutableListOf()

        //產生對應 spinner 選單
        var count = 1

        val spannerList = mutableListOf<CustomImageAdapter.SelectBank>()
        if (mMoneyPayWay?.rechType == "bankTransfer") //銀行卡轉帳 顯示銀行名稱，不用加排序數字
            rechCfgsList.forEach {
                val selectBank = CustomImageAdapter.SelectBank(
                    it.rechName.toString(),
                    getBankIconByBankName(it.rechName.toString())
                )
                spannerList.add(selectBank)
            }
        else {
            val title = mMoneyPayWay?.title

            if (rechCfgsList.size > 1)
                rechCfgsList.forEach { it ->
                    val selectBank =
                        CustomImageAdapter.SelectBank(
                            title + count++,
                            getBankAccountIcon(it.rechType ?: "")
                        )
                    spannerList.add(selectBank)
                }
            else
                rechCfgsList.forEach { it ->
                    val selectBank =
                        CustomImageAdapter.SelectBank(
                            title + "",
                            getBankAccountIcon(it.rechType ?: "")
                        )
                    spannerList.add(selectBank)
                }
        }
        sp_pay_account.adapter = CustomImageAdapter(context, spannerList)

        //選擇入款帳號
        sp_pay_account.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        //default 選擇第一個不為 null 的入款帳號資料
        if (sp_pay_account.count > 0)
            sp_pay_account.setSelection(0)
        else
            mSelectRechCfgs = null
    }

    //依據選擇的支付渠道，刷新UI
    private fun refreshSelectRechCfgs(selectRechCfgs: MoneyRechCfg.RechConfig?) {
        //姓名
        tv_name.text = selectRechCfgs?.payeeName

        //帳號
        tv_account.text = selectRechCfgs?.payee

        //地址QR code
        Glide.with(this).load(selectRechCfgs?.qrCode).into(iv_address)

        //備註
        tv_remark.text = selectRechCfgs?.remark

        //銀行卡轉帳 UI 特別處理
        if (mMoneyPayWay?.rechType == "bankTransfer") {
            ll_qr.visibility = View.GONE
            ll_address.visibility = View.VISIBLE
            cv_wx_id.visibility = View.GONE

        } else {
            ll_qr.visibility = View.VISIBLE
            ll_address.visibility = View.GONE

        }
        when (mMoneyPayWay?.rechType) {
            MoneyType.BANK.code, MoneyType.CTF.code -> {
                ll_qr.visibility = View.GONE
                ll_address.visibility = View.VISIBLE
                cv_wx_id.visibility = View.GONE
                cv_nickname.visibility = View.GONE
                cv_bank_account.visibility = View.VISIBLE
                cv_name.visibility = View.VISIBLE
            }
            MoneyType.WX.code -> {
                ll_qr.visibility = View.VISIBLE
                ll_address.visibility = View.GONE
                cv_wx_id.visibility = View.VISIBLE
                cv_nickname.visibility = View.GONE
                cv_bank_account.visibility = View.GONE
                cv_name.visibility = View.GONE

            }
            MoneyType.ALI.code -> {
                ll_qr.visibility = View.VISIBLE
                ll_address.visibility = View.GONE
                cv_wx_id.visibility = View.GONE
                cv_nickname.visibility = View.VISIBLE
                cv_bank_account.visibility = View.GONE
                cv_name.visibility = View.VISIBLE
            }
        }

        //存款時間
        et_wallet_money.text = TimeUtil.stampToDate(Date().time)

//        //存款金額提示
//        val maxMoney = ArithUtil.toMoneyFormat(MoneyManager.getMaxMoney(selectRechCfgs))
//        val minMoney = ArithUtil.toMoneyFormat(MoneyManager.getMinMoney(selectRechCfgs))
//        tv_pay_amount_desc.text = String.format(getString(R.string.deposit_amount_range), minMoney, maxMoney)
//
//        //存款金額
//        val editTextTools = EditTextTools(edit_pay_amount, 15, 0) //輸入到小數點下第二位
//        editTextTools.setEnableComma(true) //開啟千分位符號顯示功能
//        //解決hint太長凸出輸入框問題
//        var hintText = String.format(getString(R.string.limit_lower_100_upper_5000), minMoney, maxMoney)
//        val span = SpannableString(hintText)
//        span.setSpan(RelativeSizeSpan(0.9f), 0, hintText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//        edit_pay_amount.hint = span
//
//        //帳號信息
//        if (!selectRechCfgs?.pageDesc.isNullOrEmpty()) {
//            edit_account_info.hint = selectRechCfgs?.pageDesc
//        } else {
//            if (selectRechCfgs?.rechType == "alipay" || selectRechCfgs?.rechType == "cft") {
//                edit_account_info.hint = getString(R.string.please_enter_account_nickname)
//            } else {
//                edit_account_info.hint = getString(R.string.please_enter_account_name)
//            }
//        }
//
//        //認證姓名
//        if (selectRechCfgs?.rechType == "cft") {
//            block_certified_name.visibility = View.VISIBLE
//            line_certified_name.visibility = View.VISIBLE
//        } else {
//            block_certified_name.visibility = View.GONE
//            line_certified_name.visibility = View.GONE
//        }
//        when (selectRechCfgs?.rechType) {
//            "alipay" -> edit_certified_name.hint = getString(R.string.fill_in_alipay_real_name)
//            "cft" -> edit_certified_name.hint = getString(R.string.fill_in_CFT_real_name)
//            else -> edit_certified_name.hint = getString(R.string.please_enter_account_name)
//        }
//

//
//        //手續費率/贈送費率 <0是手續費 >0是贈送費率
//        when {
//            selectRechCfgs?.rebateFee ?: 0.0 > 0.0 -> {
//                line_rate.visibility = View.VISIBLE
//                block_rate.visibility = View.VISIBLE
//                tv_rate_title.text = getString(R.string.gift_money)
//                tv_rate.text = "0"
//            }
//            selectRechCfgs?.rebateFee ?: 0.0 < 0.0 -> {
//                line_rate.visibility = View.VISIBLE
//                block_rate.visibility = View.VISIBLE
//                tv_rate_title.text = getString(R.string.fees)
//                tv_rate.text = "0"
//            }
//            else -> {
//                line_rate.visibility = View.GONE
//                block_rate.visibility = View.GONE
//            }
//        }
//        setFeeRateText(edit_pay_amount.text)
    }


}