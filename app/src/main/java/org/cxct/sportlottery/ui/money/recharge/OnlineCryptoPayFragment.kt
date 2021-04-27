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
import org.cxct.sportlottery.network.common.RechType
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.MoneyRechCfg
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.CustomImageAdapter
import org.cxct.sportlottery.ui.login.LoginEditText
import org.cxct.sportlottery.util.ArithUtil
import java.util.ArrayList
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

    private var CurrentCurrency = ""

    private var filterRechCfgsList = HashMap<String?,ArrayList<MoneyRechCfg.RechConfig>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.online_crypto_pay_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBottomSheetData()
        initView()
        initButton()
        initObserve()
        setCurrencyBottomSheet()
        setAccountBottomSheet()
    }
    //幣種選項
    private fun initBottomSheetData() {
        rechCfgsList = (viewModel.rechargeConfigs.value?.rechCfgs?.filter {
            it.rechType == mMoneyPayWay?.rechType && it.onlineType ==  mMoneyPayWay?.onlineType
        } ?: mutableListOf()) as MutableList<MoneyRechCfg.RechConfig>

        //幣種
        if (mMoneyPayWay?.rechType == RechType.ONLINEPAYMENT.code) {
            filterRechCfgsList =
                rechCfgsList.groupBy { it.prodName } as HashMap<String?, ArrayList<MoneyRechCfg.RechConfig>>
            filterRechCfgsList.forEach {
                val selectCurrency = CustomImageAdapter.SelectBank(
                    it.key.toString(),
                    null
                )
                mCurrencyBottomSheetList.add(selectCurrency)
            }
        }
        //預設幣種
        CurrentCurrency = filterRechCfgsList.keys.first().toString()

        rechCfgsList[0].prodName?.let { getAccountBottomSheetList(it) }

    }


    private fun initObserve() {
        //充值個數訊息
        viewModel.rechargeAccountMsg.observe(viewLifecycleOwner, {
            et_recharge_account.setError(it)
        })

        //在線充值成功
        viewModel.onlinePayCryptoResult.observe(this.viewLifecycleOwner, {
            resetEvent()
        })
    }

    private fun initView() {
        setupTextChangeEvent()
        setupFocusEvent()
        getMoney()
        updateMoneyRange()
        refreshCurrencyType(CurrentCurrency)

        tv_recharge_money.text = String.format(resources.getString(R.string.txv_recharge_money), "0.000")

        if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0)  //返利
            tv_fee_amount.text = String.format(getString(R.string.hint_feeback_amount), "0.000")
        else
            tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount), "0.000")
    }

    private fun initButton() {
        btn_submit.setOnClickListener {
            val depositMoney = if (et_recharge_account.getText().isNotEmpty()) {
                et_recharge_account.getText()
            } else {
                ""
            }

            val payee = txv_currency.text.toString()
            val payeeName = txv_account.text.toString()
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
        filterRechCfgsList[prodName]?.forEach {
            val selectAccount = CustomImageAdapter.SelectBank(
                it.payeeName.toString(),
                null
            )
            mAccountBottomSheetList.add(selectAccount)
        }
        setAccountBottomSheet()
    }

    @SuppressLint("CutPasteId")
    private fun setCurrencyBottomSheet() {
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
                    BankBtsAdapter.BankAdapterListener { bankCard, _ ->
                        CurrentCurrency = bankCard.bankName.toString()
                        refreshCurrencyType(CurrentCurrency)
                        resetEvent()
                        dismiss()
                    })
                lv_bank_item.adapter = currencyBtsAdapter
                tv_game_type_title.text = String.format(resources.getString(R.string.title_choose_currency))
                currencyBottomSheet.btn_close.setOnClickListener {
                    this.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setAccountBottomSheet() {
        try {
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
                tv_game_type_title.text =
                    String.format(resources.getString(R.string.title_choose_recharge_account))
                accountBottomSheet.btn_close.setOnClickListener {
                    this.dismiss()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun refreshCurrencyType(currency: String) {
        if (filterRechCfgsList[currency]?.size ?: 0 > 0) {
            mSelectRechCfgs=filterRechCfgsList[currency]?.get(0)//預設帶入第一筆帳戶資料
            getMoney()
            updateMoneyRange()
            //更新充值帳號
            getAccountBottomSheetList(mSelectRechCfgs?.prodName ?: "")
            refreshAccount(0)//預設帶入第一筆帳戶資料

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
                ArithUtil.toMoneyFormat(selectRechCfgs?.exchangeRate)
            )

            //手續費率/返利
            when{
                selectRechCfgs?.rebateFee ==  0.0 || selectRechCfgs?.rebateFee == null -> {
                    tv_fee_rate.text = String.format(getString(R.string.hint_fee_rate), "0.000") + "%"
                    tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount), "0.000")
                }
                selectRechCfgs.rebateFee ?: 0.0 > 0.0 ->{
                    tv_fee_rate.text = String.format(getString(R.string.hint_feeback_rate),  ArithUtil.toMoneyFormat(selectRechCfgs?.rebateFee?.times(100))) + "%"
                }
                else ->{
                    tv_fee_rate.text = String.format(getString(R.string.hint_fee_rate),  ArithUtil.toMoneyFormat(ArithUtil.mul(abs(selectRechCfgs?.rebateFee ?: 0.0), 100.0))) + "%"
                }
            }

            //備註
            tv_hint.text = selectRechCfgs?.remark

            //充值個數限制
            updateMoneyRange()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //修改hint
    private fun updateMoneyRange() {
        et_recharge_account.setHint(
            String.format(
                getString(R.string.edt_hint_crypto_pay_count),
                mSelectRechCfgs?.minMoney?.toInt(),
                mSelectRechCfgs?.maxMoney?.toInt()
            )
        )
    }

    private fun refreshAccount(position: Int) {
        if (mAccountBottomSheetList.size > 0) {
            txv_account.text = mAccountBottomSheetList[position].bankName
        }
        mSelectRechCfgs=filterRechCfgsList[CurrentCurrency]?.get(position)
        refreshSelectRechCfgs(mSelectRechCfgs)
    }

    private fun setupTextChangeEvent() {
        viewModel.apply {
            //充值個數
            et_recharge_account.afterTextChanged {
                if(it.startsWith("0") && it.length>1){
                    et_recharge_account.setText(et_recharge_account.getText().replace("0",""))
                    et_recharge_account.setCursor()
                    return@afterTextChanged
                }

                if(et_recharge_account.getText().length > 6){
                    et_recharge_account.setText(et_recharge_account.getText().substring(0,6))
                    et_recharge_account.setCursor()
                    return@afterTextChanged
                }

                checkRechargeAccount(it, mSelectRechCfgs)
                if (it.isEmpty() || it.isBlank()) {
                    tv_recharge_money.text =
                        String.format(resources.getString(R.string.txv_recharge_money), "0.000")
                    if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) {
                        tv_fee_amount.text =
                            String.format(getString(R.string.hint_feeback_amount), "0.000")
                    } else {
                        tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount), "0.000")
                    }
                } else {
                    //充值金額
                    tv_recharge_money.text = String.format(
                        resources.getString(R.string.txv_recharge_money),
                        ArithUtil.toMoneyFormat(it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0))
                    )
                    //返利/手續費金額
                    if (mSelectRechCfgs?.rebateFee ?: 0.0 > 0.0) { //返利/手續費金額
                        tv_fee_amount.text =
                            String.format(getString(R.string.hint_feeback_amount), ArithUtil.toMoneyFormat((it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0)).times(mSelectRechCfgs?.rebateFee?:0.0)))
                    } else {
                        tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount), ArithUtil.toMoneyFormat(abs(it.toLong().times(mSelectRechCfgs?.exchangeRate ?: 1.0).times(mSelectRechCfgs?.rebateFee?:0.0))))
                    }

                    if(mSelectRechCfgs?.rebateFee == 0.0 || mSelectRechCfgs?.rebateFee == null){
                        tv_fee_rate.text = String.format(getString(R.string.hint_fee_rate), "0.000") + "%"
                        tv_fee_amount.text = String.format(getString(R.string.hint_fee_amount), "0.000")
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
        viewModel.clearnRechargeStatus()
    }
}