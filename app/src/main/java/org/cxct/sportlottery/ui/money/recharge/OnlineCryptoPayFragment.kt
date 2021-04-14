package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.crypto_pay_fragment.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_bank_card.*
import kotlinx.android.synthetic.main.edittext_login.view.*
import kotlinx.android.synthetic.main.online_crypto_pay_fragment.*
import kotlinx.android.synthetic.main.online_crypto_pay_fragment.btn_submit
import kotlinx.android.synthetic.main.online_crypto_pay_fragment.cv_account
import kotlinx.android.synthetic.main.online_crypto_pay_fragment.cv_currency
import kotlinx.android.synthetic.main.online_crypto_pay_fragment.et_recharge_account
import kotlinx.android.synthetic.main.online_crypto_pay_fragment.tv_fee_amount
import kotlinx.android.synthetic.main.online_crypto_pay_fragment.tv_fee_rate
import kotlinx.android.synthetic.main.online_crypto_pay_fragment.tv_rate
import kotlinx.android.synthetic.main.online_crypto_pay_fragment.tv_recharge_money
import kotlinx.android.synthetic.main.online_crypto_pay_fragment.txv_account
import kotlinx.android.synthetic.main.online_crypto_pay_fragment.txv_currency
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.MoneyRechCfg
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.CustomImageAdapter
import org.cxct.sportlottery.ui.login.LoginEditText
import kotlin.math.abs

class OnlineCryptoPayFragment : BaseFragment<MoneyRechViewModel>(MoneyRechViewModel::class) {

    private var mMoneyPayWay: MoneyPayWayData? = MoneyPayWayData("", "", "", "", 0) //支付類型

    private var mSelectRechCfgs: MoneyRechCfg.RechConfig? = null //選擇的入款帳號

    private var rechCfgsList: List<MoneyRechCfg.RechConfig> = mutableListOf()

    //幣種
    private lateinit var currencyBottomSheet: BottomSheetDialog
    private val mCurrencyBottomSheetList = mutableListOf<CustomImageAdapter.SelectBank>()
    private lateinit var currencyBtsAdapter: BankBtsAdapter

    //充值帳戶
    private lateinit var accountBottomSheet: BottomSheetDialog
    private val mAccountBottomSheetList = mutableListOf<CustomImageAdapter.SelectBank>()
    private lateinit var accountBtsAdapter: BankBtsAdapter
    private val mapOfAccount = hashMapOf<String?, List<String?>>()

    private var currencyPosition = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.online_crypto_pay_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBottomSheet()
        initView()
        initButton()
        initObserve()
        setPayBankBottomSheet()
    }
    //幣種選項
    private fun initBottomSheet() {
        rechCfgsList = (viewModel.rechargeConfigs.value?.rechCfgs?.filter {
            it.rechType == mMoneyPayWay?.rechType && it.onlineType ==  mMoneyPayWay?.onlineType
        } ?: mutableListOf()) as MutableList<MoneyRechCfg.RechConfig>

        //幣種
        if (mMoneyPayWay?.rechType == "onlinePayment") {
            rechCfgsList.forEach {
                val selectCurrency = CustomImageAdapter.SelectBank(
                    it.prodName.toString(),
                    null
                )
                mCurrencyBottomSheetList.add(selectCurrency)

                val selectAccount = CustomImageAdapter.SelectBank(
                    it.payeeName.toString(),
                    null
                )
                mAccountBottomSheetList.add(selectAccount)
            }
        }

        //篩選充值帳號
        val prodNameList = mutableListOf<String?>()//KEY值
        rechCfgsList.distinctBy {
            it.prodName
        }.forEach {
            prodNameList.add(it.prodName)
        }

        prodNameList.forEach { prodName ->
            var accountList = mutableListOf<String?>()
            rechCfgsList.forEach {
                if (prodName == it.prodName) {
                    accountList.add(it.payeeName)
                }
            }
            mapOfAccount[prodName] = accountList
        }

        rechCfgsList[0].prodName?.let { getAccountBottomSheetList(it) }

    }


    private fun initObserve() {
        //充值個數訊息
        viewModel.rechargeAccountMsg.observe(viewLifecycleOwner, {
            et_recharge_account.setError(it)
        })

        //在線充值成功
        viewModel.onlinePaySubmit.observe(this.viewLifecycleOwner, {
            et_recharge_account.setText("")
        })
    }

    private fun initView() {
        setupTextChangeEvent()
        setupFocusEvent()
        getMoney()
        updateMoneyRange()
        refreshCurrencyType(0)

        tv_recharge_money.text = String.format(resources.getString(R.string.txv_recharge_money), "0")
    }

    private fun initButton() {
        btn_submit.setOnClickListener {
            val depositMoney = if (et_recharge_account.getText().isNotEmpty()) {
                et_recharge_account.getText().toInt()
            } else {
                0
            }

            var payee = txv_currency.text.toString()
            var payeeName = txv_account.text.toString()
            viewModel.rechargeOnlinePay(requireContext(), mSelectRechCfgs, depositMoney, payee, payeeName)
        }

        cv_currency.setOnClickListener {
            currencyBottomSheet.show()
        }

        cv_account.setOnClickListener {
            accountBottomSheet.show()
        }
    }

    fun setArguments(moneyPayWay: MoneyPayWayData?): OnlineCryptoPayFragment {
        mMoneyPayWay = moneyPayWay
        return this
    }

    private fun getAccountBottomSheetList(prodName: String) {
        mAccountBottomSheetList.clear()
        mapOfAccount[prodName]?.forEach {
            val selectAccount = CustomImageAdapter.SelectBank(
                it.toString(),
                null
            )
            mAccountBottomSheetList.add(selectAccount)
        }
    }

    private fun setPayBankBottomSheet() {
        try {
            //支付渠道
            val contentView: ViewGroup? =
                activity?.window?.decorView?.findViewById(android.R.id.content)
            val bottomSheetView =
                layoutInflater.inflate(R.layout.dialog_bottom_sheet_bank_card, contentView, false)
            currencyBottomSheet = BottomSheetDialog(this.requireContext())
            currencyBottomSheet.apply {
                setContentView(bottomSheetView)
                currencyBtsAdapter = BankBtsAdapter(
                    lv_bank_item.context,
                    mCurrencyBottomSheetList,
                    BankBtsAdapter.BankAdapterListener { _, position ->
                        currencyPosition = position
                        refreshCurrencyType(position)
                        resetEvent()
                        dismiss()
                    })
                lv_bank_item.adapter = currencyBtsAdapter
                currencyBottomSheet.btn_close.setOnClickListener {
                    this.dismiss()
                }
            }
            //支付帳號
            val accountContentView: ViewGroup? =
                activity?.window?.decorView?.findViewById(android.R.id.content)
            val accountBottomSheetView =
                layoutInflater.inflate(
                    R.layout.dialog_bottom_sheet_bank_card,
                    accountContentView,
                    false
                )
            accountBottomSheet = BottomSheetDialog(this.requireContext())
            accountBottomSheet.apply {
                setContentView(accountBottomSheetView)
                accountBtsAdapter = BankBtsAdapter(
                    lv_bank_item.context,
                    mAccountBottomSheetList,
                    BankBtsAdapter.BankAdapterListener { _, position ->
                        refreshAccount(position)
                        resetEvent()
                        dismiss()
                    })
                lv_bank_item.adapter = accountBtsAdapter
                accountBottomSheet.btn_close.setOnClickListener {
                    this.dismiss()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun refreshCurrencyType(position: Int) {
        if (rechCfgsList.isNotEmpty()) {
            mSelectRechCfgs = rechCfgsList[position]
            refreshSelectRechCfgs(mSelectRechCfgs)
            getMoney()
            updateMoneyRange()
            //更新充值帳號
            getAccountBottomSheetList(mSelectRechCfgs?.prodName ?: "")
            refreshAccount(0)

            txv_currency.text = mSelectRechCfgs?.prodName ?: ""
        } else {
            mSelectRechCfgs = null
        }
    }

    //依據選擇的支付渠道，刷新UI
    @SuppressLint("SetTextI18n")
    private fun refreshSelectRechCfgs(selectRechCfgs: MoneyRechCfg.RechConfig?) {
        try {
            //匯率
            tv_rate.text = String.format(
                getString(R.string.hint_rate),
                selectRechCfgs?.exchangeRate.toString()
            )
            //手續費率/返利
            tv_fee_rate.visibility = View.VISIBLE
            if (selectRechCfgs?.rebateFee ?: 0.0 > 0.0) { //返利
                tv_fee_rate.text = String.format(getString(R.string.hint_feeback_rate), selectRechCfgs?.rebateFee.toString()) + "%"
            } else {
                tv_fee_rate.text = String.format(getString(R.string.hint_fee_rate), abs(selectRechCfgs?.rebateFee ?: 0.0).toString()) + "%"
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //修改hint
    private fun updateMoneyRange() {
        et_recharge_account.setHint(
            String.format(
                getString(R.string.edt_hint_crypto_pay_count),
                mSelectRechCfgs?.minMoney,
                mSelectRechCfgs?.maxMoney
            )
        )
    }

    private fun refreshAccount(position: Int) {
        if (mAccountBottomSheetList.size > 0) {
            txv_account.text = mAccountBottomSheetList[position].bankName
        }
    }

    private fun setupTextChangeEvent() {
        viewModel.apply {
            //充值個數
            et_recharge_account.afterTextChanged {
                tv_fee_amount.visibility = View.VISIBLE
                checkRechargeAccount(it, mSelectRechCfgs)
                if (it.isEmpty() || it.isBlank()) {
                    tv_recharge_money.text =
                        String.format(resources.getString(R.string.txv_recharge_money), "0")
                    if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) {
                        tv_fee_amount.text =
                            String.format(getString(R.string.hint_feeback_amount), "0")
                    } else {
                        tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount), "0")
                    }
                    tv_fee_amount.text =
                        String.format(resources.getString(R.string.txv_recharge_money), "0")
                } else {
                    //充值金額
                    tv_recharge_money.text = String.format(
                        resources.getString(R.string.txv_recharge_money),
                        it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0)
                    )
                    //返利/手續費金額
                    if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) { //返利/手續費金額
                        tv_fee_amount.text =
                            String.format(getString(R.string.hint_feeback_amount), (it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0)).times(mSelectRechCfgs?.rebateFee?:0.0))
                    } else {
                        tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount), abs(it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0)).times(mSelectRechCfgs?.rebateFee?:0.0))
                    }
                }
            }
        }
    }

    private fun setupFocusEvent() {
        viewModel.apply {
            //充值個數
            setupEditTextFocusEvent(et_recharge_account) { checkRechargeAccount(it, mSelectRechCfgs) }
        }
    }

    private fun setupEditTextFocusEvent(customEditText: LoginEditText, event: (String) -> Unit) {
        customEditText.setEditTextOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                event.invoke(customEditText.et_input.text.toString())
        }
    }

    //取得餘額
    private fun getMoney() {
        viewModel.getMoney()
    }

    private fun resetEvent() {
        clearFocus()
        et_recharge_account.setText("")
    }
}